(ns style-plus.core (:require [clojure.string :as string]
   [clojure.walk :as walk]
   [style-plus.shorthand :as shorthand]
   [closet-essentials.atomic :as atomic]
   [garden.core]
   [stylefy.core :as stylefy :refer [use-style]]))


(def int-vals
  #{:font-weight :order :opacity :flex-grow :flex-shrink :z-index :grid-row :grid-row-start :grid-row-end :grid-column :grid-column-start :grid-column-end :columns :column-count :counter-increment :counter-reset :counter-set })

;:left and :right (used with @page rule) have been removed from this check to avoid clash with standard css props
(def psuedo-classes #{:active :any-link :blank :checked :current :default :defined :disabled :drop :empty :enabled :first :first-child :first-of-type :fullscreen :future :focus :focus-visible :focus-within :host :hover :indeterminate :in-range :invalid :last-child :last-of-type :link :local-link :only-child :only-of-type :optional :out-of-range :past :placeholder-shown :read-only :read-write :required :root :scope :target :target-within :user-invalid :valid :visited})

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

(defn convert-vector [v]
  (mapv #(cond
           (vector? %)
           (convert-vector %)
           (number? %)
           (convert-number %)
           (keyword? %)
           (name %)
           :else %)
        v))

(defn vector->string [v inner?]
  (string/join (if inner? " " ", ")
               (mapv #(cond
                        (vector? %)
                        (vector->string % true)
                        (number? %)
                        (convert-number %)
                        (keyword? %)
                        (name %)
                        :else %)
                     v)))


(defn- sp-conversion [v k]
  (cond
    (map? v)
    (reduce (fn [acc [key val]]
              (cond
                (mq-key? key)
                (assoc-in acc
                          [:media (convert-mq-vals key)]
                          {k (sp-conversion (get v key) k)})

                (= key :=)
                (assoc acc key (sp-conversion val k))

                :else
                (assoc-in acc [:mode key] {k (sp-conversion (get v key) k)})))
            {:mode {}
             :media {}}
            v)

    (vector? v)
    (vector->string v false)

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


(defn- resolve-css-val [v k]
  (if (map? v)
    (reduce (fn [acc [map-key v]] (assoc acc map-key (shorthand/val-sh v k))) {} v)
    (shorthand/val-sh v k)))


(defn- styles-reduced [m]
  (reduce
   (fn [acc [k v]]
     (let [resolved-css-prop (shorthand/key-sh k)
           resolved-css-val (resolve-css-val v k)
           converted-val (sp-conversion resolved-css-val resolved-css-prop)]
       (assoc acc resolved-css-prop converted-val)))
   {}
   m))


;; Pipline fns
(defn- modes->map [m globals]
  (let [modes-map* (if-let [modes (:s/mode m)]
                     (assoc m :s/mode (reduce (fn [acc m] (deep-merge acc m)) {} modes))
                     m)
        modes-map (assoc modes-map* :s/mode (keystrings (:s/mode modes-map*)))
        globals-map (->> globals
                         keystrings
                         (reduce (fn [acc [k v]] (assoc acc k (styles-reduced v))) {}))
        modes-map-merged (deep-merge {:s/mode globals-map} modes-map)]

     modes-map-merged))


(defn- nested-modes [acc [k v]]
  (assoc
   acc
   k
   (reduce
    (fn [acc [k v]]
      (if (map? v)
        (reduce
         (fn [acc [key val]]
           (assoc-in
            acc
            [::stylefy/mode (psuedo-class-keystring key)]
            {k val}))
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
            (if (map? key)
              (int-vals->px-vals key)
              key)
            (if (map? val)
              (int-vals->px-vals val)
              (convert-number val key))))
   {}
   m))


(defn- media->map
  [m globals]
  (let [media-map* (if-let [media (:s/media m)]
                     (let [a (reduce (fn [acc m] (deep-merge acc m)) {} media)]
                       (assoc m :s/media (or a {})))
                     m)
        globals-hydrated (reduce (fn [acc [k v]] (assoc acc k (styles-reduced v))) {} globals)
        media-map-merged* (deep-merge {:s/media globals-hydrated} media-map*)
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
        reduced (styles-reduced globals-removed)
        extracted (extract-modes reduced)
        no-empties (remove-empties extracted)
        modes (modes->map no-empties global-modes)
        medias (media->map modes global-mq)
        keyss (stylefy-keys medias)
        styles (remove-nil-and-empty keyss)]
    styles))


(defn ns+
  "Creates a string that represents a fully namespaced-qualified
   identifier for an element within a component rendering function.
   Includes line number of parent function.

   Intended to be called with a Var-quoted name of the enclosing
   function, as well as an optional keyword. The keyword is a user-defined,
   symantic name (similar to a classname) associated with html element within
   the function.

   The resulting string is intended to be used as the value of a custom-data
   attribute, in order to help quickly identify the specific element when
   inspecting output in an environment such as Chrome DevTools.
   This is helpful when using a library such as Stylefy, which elides the
   the need for using classnames, which traditionally double as both css
   selectors and unique identifiers.

   It can be optionally called with just the var-quoted function name.

   Example:

    (defn my-button [label]
      [:div
      (s+ {:cursor :pointer
           :text-align :center
           :border [[1 :solid :blue]}
          {:role :button
           :on-click #()
           :data-ns (ns+ #'my-button :outer)})
        [:span
         (s+ {:background :yellow}
             {:data-ns (ns+ #'my-button :inner})
         label]])"

  ([x]
   (when (= cljs.core/Var (type x))
     (ns+ x nil)))
  ([var-quoted-fn el-ident]
   (let [{ns* :ns name* :name line* :line} (meta var-quoted-fn)
         namespace* (when ns* (str ns* "/"))
         fn-name (when name* (str name*))
         el-ident-str (when el-ident (str (when fn-name "::") (name el-ident)))
         line-number (when line* (str ":" line*))]
     (str namespace* fn-name el-ident-str line-number))))


(defn data-ns-map [style]
  (when-let [m (meta style)]
    (when-let [[k v] (first m)]
      {:data-ns (if (true? v)
                  (ns+ k)
                  (ns+ k v))})))
#_(defn s+*
  ([x]
   (if (map? x)
     (s+ nil x nil)
     (when (vector? x) )))
  ([x y]
   (when (= attr {:data-stylitics :showcase-nav})
     (s+->stylefy style))
   (use-style
    (s+->stylefy style)
    (merge (data-ns-map style) attr)))
  ([x y z]))

(defn s+
  ([style]
   (s+ style nil))
  ([style attr]
   (use-style
    (s+->stylefy style)
    (merge (data-ns-map style) attr))))

(defn !imp
  "Appends \"!important\" to a css style value.
   Expects a string or keyword."
  [v]
  (if-not (or (map? v) (vector? v))
    (str v "!important")
    v))

(defn cssfn [k & args]
  (str (name k) "(" (string/join ", " (map #(if (keyword? %) (name %) (str %)) args)) ")"))


(defn atomic [& ks]
  (reduce (fn [acc k] (merge (k atomic/atomic) acc)) {} ks))

(defn- v->str [v]
  (cond
    (or (symbol? v) (string? v) (keyword? v))
    (name v)
    (number? v)
    (str v "px")
    :else (when (vector? v)
            (string/join " " (map v->str v)))))


(defn linear-gradient
  "Can be used with :background or :background-image css prop.
   direction arg must be one of the following three types of keywords:
   :to-bottom-right
   :77deg
   :0.5turn

   Examples:
   (linear-gradient :to-right [:blue :10%] [:red :60%] [:teal :80%])
   (linear-gradient :95deg :blue :red :teal)
   (linear-gradient :0.33turn :blue :10% :teal)

   More info here:
   https://developer.mozilla.org/en-US/docs/Web/CSS/linear-gradient"
  [direction & stops]
  (let [direction (when direction
                    (str
                     (if (number? direction)
                       (str (js/parseFloat direction) "deg")
                       (when (or (string? direction)
                                 (keyword? direction))
                         (string/replace (name direction) #"-" " ")))
                     ", "))]
    (str
     "linear-gradient("
     direction
     (string/join ", " (map v->str stops))
     ")")))


;; Debugging
#_(def bp* {:max-width 500
          :min-width :12rem})

#_(js/console.clear)

#_(pprint
 (s+->stylefy {:h :100*}))

#_(s+->stylefy
  {:text-shadow [[10 10 :blue] [10 10 :red]]}
  #_{:flex [[1 1 :auto]]})

#_(s+->stylefy
  {bp* {"hover" {:font-size 40}
        "first-child" {:font-size 40}}})

#_(s+->stylefy
  {:font-size {bp* {"hover" 20
                    "first-child" 40}}})
