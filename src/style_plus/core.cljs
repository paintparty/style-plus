(ns style-plus.core
  (:require
   [stylefy.core :as stylefy :refer [use-style]]))

(def int-vals
  #{:font-weight :order :opacity :flex-grow :flex-shrink :z-index :grid-row :grid-row-start :grid-row-end :grid-column :grid-column-start :grid-column-end :columns :column-count :counter-increment :counter-reset :counter-set })

;:left and :right (used with @page rule) have been removed from this check to avoid clash with standard css props
(def psuedo-classes #{:active :any-link :blank :checked :current :default :defined :disabled :drop :empty :enabled :first :first-child :first-of-type :fullscreen :future :focus :focus-visible :focus-within :host :hover :indeterminate :in-range :invalid :last-child :last-of-type :link :local-link :only-child :only-of-type :optional :out-of-range :past :placeholder-shown :read-only :read-write :required :root :scope :target :target-within :user-invalid :valid :visited})


;; Breakpoint helpers
(defn- px [x] (if (number? x) (str x "px") x))

(defn- number-string? [v]
  (and (string? v) (not (re-find #"[^0-9.]" v))))

(defn- normalize-css-value [css-val]
  (let [v* (if (keyword? css-val) (name css-val) css-val)
        v (cond
            (number? v*)
            (px v*)

            (number-string? v*)
            (str v* "px")

            :else v*)]
    v))

(defn above [kw m]
  {:min-width (some-> (get m kw) normalize-css-value)})

(defn css-below-val [kw m]
  (when-let [v (some-> (get m kw) normalize-css-value)
             ]
    (let [unit (if (re-find #"em$" v) "em" "px")
          amount (if (= unit "em") 0.00125 0.02)]
      (str "calc(" v " - " amount unit ")"))))

(defn below [kw m]
  {:max-width (css-below-val kw m)})

(defn between [start-k end-k m]
  {:max-width (css-below-val end-k m)
   :min-width (some-> (get m start-k) normalize-css-value)})


;; utils fns
(defn- convert-number
  ([n]
   (convert-number n nil))
  ([n k]
   (if (number? n)
     (if-not (contains? int-vals k)
       (str n "px")
       n)
     n)))

(defn- mq-key? [k]
  (and (map? k)
       (some #(contains? k %) [:min-width :max-width :min-height :max-height])))

(defn- convert-mq-vals [m]
  (reduce (fn [acc [k v]]
            (assoc acc k (if (number? v) (convert-number v) v))) {} m))

(defn- sp-conversion [v k]
  (cond
    (map? v)
    (reduce (fn [acc [key val]]
              (cond
                (mq-key? key)
                (assoc-in acc
                          [:media (convert-mq-vals key)]
                          {k (get v key)})

                (= key :=)
                (assoc acc key (sp-conversion val k))

                :else
                (assoc-in acc [:mode key] {k (sp-conversion (get v key) k)})))
            {:mode {}
             :media {}}
            v)

    (vector? v)
    (when (and (= (count v) 1)
               (vector? (first v))
               (not (empty? (first v))))
      [(mapv #(cond
                (number? %)
                (convert-number %)
                (keyword? %)
                (name %)
                :else %)
             (first v))])

    (number? v)
    (convert-number v k)

    :else v))


(defn deep-merge [& maps]
  (apply merge-with (fn [& args]
                      (if (every? map? args)
                        (apply deep-merge args)
                        (last args)))
         maps))


(defn- psuedo-class-keystring [k]
  (let [key (if (keyword? k) (name k) k)
        new-key (if (= ":" (first key)) key (str ":" key))]
    new-key))


(defn- keystrings [m]
  (reduce (fn [acc [k v]]
            (let [new-key (psuedo-class-keystring k)]
              (assoc acc new-key v)))
          {}
          m))


(defn- psuedo-class-key? [k]
  (contains? psuedo-classes k))


;; Pipline fns
(defn- modes->map [m globals]
  (let [modes-map* (if-let [modes (:s/mode m)]
                     (assoc m :s/mode (reduce (fn [acc m] (deep-merge acc m)) {} modes))
                     m)
        modes-map (assoc modes-map* :s/mode (keystrings (:s/mode modes-map*)))
        globals-map (keystrings globals)
        modes-map-merged (deep-merge {:s/mode globals-map} modes-map)]
     modes-map-merged))


(defn- nested-modes [acc [k v]]
  (assoc acc k (reduce (fn [acc [k v]]
                         (if (map? v)
                             (reduce (fn [acc [key val]]
                                       (assoc-in acc [::stylefy/mode (psuedo-class-keystring key)] {k val}))
                                     acc
                                     v)
                             (assoc acc k v)))
                       {::stylefy/mode {}}
                       v)))


(defn- int-vals->px-vals
  [m]
  (reduce
   (fn [acc [key val]]
     (assoc acc
            (if (map? key) (int-vals->px-vals key) key)
            (if (map? val) (int-vals->px-vals val) (convert-number val key))))
   {}
   m))


(defn- media->map [m globals]
  (let [media-map* (if-let [media (:s/media m)]
                     (let [a (reduce (fn [acc m] (deep-merge acc m)) {} media)]
                       (assoc m :s/media (or a {})))
                     m)
        media-map-merged* (deep-merge {:s/media globals} media-map*)
        media-map-merged (int-vals->px-vals media-map-merged*)
        with-nested-modes (if (:s/media m)
                            (assoc media-map-merged
                                   :s/media
                                   (reduce nested-modes
                                           {}
                                           (:s/media media-map-merged)))
                            media-map-merged)]
     with-nested-modes))


(defn- extract-modes [m]
  (reduce
   (fn [acc [k v]]
     (if (map? v)
       (let [default (:= v)
             with-modes (assoc acc :s/mode (conj (:s/mode acc) (:mode v)))
             with-media (assoc with-modes :s/media (conj (:s/media acc) (:media v)))
             with-default (assoc with-media k default)]
         (merge with-modes with-default))
       (assoc acc k v)))
   {:s/mode []
    :s/media []}
   m))

(defn- remove-empties [m]
  (let [mode (if (empty? (:s/mode m))
               (dissoc m :s/mode)
               m)
        media (if (empty? (:s/media m))
               (dissoc mode :s/media)
               mode)]
    media))


(defn- stylefy-keys [m]
  (-> m
   (assoc ::stylefy/media (:s/media m))
   (assoc ::stylefy/mode (:s/mode m))
   (dissoc :s/media)
   (dissoc :s/mode)))



(defn- remove-globals [m]
  (reduce (fn [acc [k v]]
            (if (or (string? k) (map? k) (psuedo-class-key? k))
              (dissoc acc k)
              (assoc acc k v)))
          {}
          m))


(defn- globals [m pred]
  (reduce (fn [acc [k v]]
            (if (pred k)
              (assoc acc k v)
              (dissoc acc v)))
          {}
          m))


(defn- global-mq [m]
  (int-vals->px-vals (globals m map?)))


(defn- global-modes [m]
  (int-vals->px-vals (globals m #(or (string? %) (psuedo-class-key? %)))))


(defn- valid-keys [m]
  (reduce (fn [acc [k v]]
            (if (or (keyword? k) (map? k) (string? k))
              (assoc acc k v)
              acc))
          {}
          m))

(defn- remove-nil-and-empty [m]
  (reduce (fn [acc [k v]]
            (if (not (nil? v))
              (if (and (map? v) (empty? v))
                acc
                (assoc acc k (if (map? v) (remove-nil-and-empty v) v)))
              acc))
          {}
          m))




(defn- s+->stylefy [style]
  (let [valid-keys (valid-keys style)
        global-modes (global-modes valid-keys)
        global-mq (global-mq valid-keys)
        globals-removed (remove-globals valid-keys)
        reduced (reduce (fn [acc [k v]]
                          (assoc acc k (sp-conversion v k)))
                        {}
                        globals-removed)
        extracted (extract-modes reduced)
        no-empties (remove-empties extracted)
        modes (modes->map no-empties global-modes)
        medias (media->map modes global-mq)
        keyss (stylefy-keys medias)
        styles (remove-nil-and-empty keyss)]
    styles))


;; Debugging
#_(def bp* {:max-width 500
          :min-width :12rem})

#_(s+->stylefy
  {bp* {"hover" {:font-size 40}
        "first-child" {:font-size 40}}})

#_(s+->stylefy
  {:font-size {bp* {"hover" 20
                    "first-child" 40}}})

(defn s+
  ([style]
   (s+ style nil))
  ([style attr]
   (use-style
    (s+->stylefy style)
    attr)))
