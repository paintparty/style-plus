(ns style-plus.atomic
  (:require
   [garden.core]
   [style-plus.shorthand :as shorthand]
   [style-plus.colors :refer [colors+]]))

(defonce all-color-props
 (apply
  merge
  (map
   (fn [{:keys [prop prefix]}]
     (apply
      merge
      (map (fn [[color-name color-value]]
             (let [pf- (when prefix (str (name prefix) "-"))]
               {(keyword (str pf- (name color-name) "+")) {prop color-value}
                (keyword (str pf- (name color-name))) {prop color-name}}))
           colors+)))
   [{:prop :background-color :prefix :bgc}
    {:prop :color}
    {:prop :border-color :prefix :bc}])))


(defonce shorthand-enum-map
  (reduce
   (fn [acc [k v]]
     (if-let [vals (:vals v)]
       (merge acc
              (reduce
               (fn [acc [sh expanded]]
                 (assoc acc (keyword (str (name k) "-" (name sh))) {(:name v) expanded}))
               {}
               vals))
       acc))
   {}
   shorthand/css-sh))


(defn flex-atomic [fd]
  (reduce
   (fn [acc [sh expanded]]
     (assoc acc
            (keyword (str "flex-" (-> fd name first) "-" (name sh)))
            {:display :flex
             :flex-direction fd
             :justify-content expanded}))
   {}
   {:c :center
    :fs :flex-start
    :fe :flex-end
    :sb :space-between
    :sa :space-around
    :se :space-evenly}))

(defn scale [{:keys [k sh-k min max step unit val-fn]
              :or {unit ""}}]
  (reduce (fn [acc v]
            (let [css-val (str (if val-fn (val-fn v) v) (name unit))
                  prefix (when sh-k (str (name sh-k) "-"))]
              (assoc acc (keyword (str prefix css-val)) {k css-val})))
          {}
          (range (or min 0) (+ step max) step)))

(defn margin-padding [k max step]
  (reduce
   #(merge %1 %2)
   {}
   (map
    #(let [px (str % "px")]
       (reduce
        (fn [acc [sh expanded]]
          (assoc acc
                 (keyword (str (-> k name first) (name sh) "-" px))
                 {(keyword (str (name k) "-" (name expanded))) px}))
        {}
        {:t :top :r :right :b :bottom :l :left}))
    (range 0 (+ max step) step))))

(defonce atomic-map
 (merge
  ;All shorthand enums
  shorthand-enum-map

  ;color utility classes
  all-color-props

  ;flexbox utility classes
  (flex-atomic :row)
  (flex-atomic :column)

  ;z-index
  (scale {:k :z-index :sh-k :z :max 1000 :step 100 })

  ;margin & padding
  (scale {:k :opacity :sh-k :o :max 100 :step 5 :unit :%})
  {:p-0 {:padding 0}}
  (margin-padding :padding 30 1)
  {:m-0 {:margin 0}}
  (margin-padding :margin 30 1)

  ;border
  (scale {:k :border-width :sh-k :bw :max 20 :step 1 :unit :px})

  ;top, right, bottom, left
  (scale {:k :top :sh-k :top :max 100 :step 5 :unit :%})
  (scale {:k :top :sh-k :top :max 30 :step 1 :unit :px})
  {:top-0 {:top 0}}
  (scale {:k :right :sh-k :right :max 100 :step 5 :unit :%})
  (scale {:k :right :sh-k :right :max 30 :step 1 :unit :px})
  {:right-0 {:right 0}}
  (scale {:k :bottom :sh-k :bottom :max 100 :step 5 :unit :%})
  (scale {:k :bottom :sh-k :bottom :max 30 :step 1 :unit :px})
  {:bottom-0 {:bottom 0}}
  (scale {:k :left :sh-k :left :max 100 :step 5 :unit :%})
  (scale {:k :left :sh-k :left :max 30 :step 1 :unit :px})
  {:left-0 {:left 0}}

  ;width & height
  (scale {:k :width :sh-k :w :max 100 :step 5 :unit :%})
  {:width-0 {:width 0}}
  (scale {:k :height :sh-k :h :max 100 :step 5 :unit :%})
  {:height-0 {:height 0}}

  ;font-sizes in px
  (scale {:k :font-size :max 48 :step 1 :unit :px})
  ;font-sizes in rems, 0-2 in 0.025 increments 
  (scale {:k :font-size :max 2000 :step 25 :unit :rem :val-fn #(/ % 1000)})
  ;font-sizes in rems, 2-6 in 0.1 increments 
  (scale {:k :font-size :min 2000 :max 6000 :step 250 :unit :rem :val-fn #(/ % 1000)})

  ;line-height
  (scale {:k :line-height :sh-k :lh :max 30 :step 1 :val-fn #(/ % 10)})

  ;letter-spacing
  (scale {:k :letter-spacing :max 30 :step 1 :unit :ex :val-fn #(/ % 100)})

  ;border-radius
  (scale {:k :border-radius :sh-k :radius :max 30 :step 1 :unit :px})
  {:radius-50% {:border-radius "50%"}
   :radius-100% {:border-radius "100%"}
   :radius-pill {:border-radius "9999px"}

   ;border
   :bordered {:border-color :silver
              :border-style :solid
              :border-width "1px"}

   ;border
   :outlined {:outline-color :silver
              :outline-style :solid
              :outline-width "1px"}

   ;background image formatting utility
   :bgi-contain {:width "100%"
                 :height "100%"
                 :background-size :contain
                 :background-position "center center"
                 :background-repeat :no-repeat}
   :bgi-cover {:width "100%"
               :background-position "center center"
               :background-repeat :no-repeat}

   ;font-weight
   :100 {:font-weight 100}
   :200 {:font-weight 200}
   :300 {:font-weight 300}
   :400 {:font-weight 400}
   :500 {:font-weight 500}
   :600 {:font-weight 600}
   :700 {:font-weight 700}
   :800 {:font-weight 800}
   :900 {:font-weight 900}

   ;text-align
   :ta-c {:text-align :center}
   :ta-l {:text-align :left}
   :ta-r {:text-align :r}

   ;text-transform
   :uppercase {:text-transform :uppercase}
   :lowercase {:text-transform :lowercase}
   :capitalize {:text-transform :capitalize}
   :full-width {:text-transform :full-width}

   ;text-decoration
   :line-through {:text-decoration :line-through}
   :underline {:text-decoration :underline}
   :overline {:text-decoration :overline}

   ;whitespace
   :nowrap {:white-space :nowrap}

   ;display
   :grid {:display :grid}
   :flex {:display :flex}
   :block {:display :block}
   :inline {:display :inline}
   :table {:display :table}
   :none {:display :none}
   :list-item {:display :list-item}
   :inline-block {:display :inline-block}
   :inline-table {:display :inline-table}
   :inline-flex {:display :inline-flex}
   :inline-grid {:display :inline-grid}

   ;font-style
   :normal {:font-style :normal}
   :oblique {:font-style :oblique}
   :italic {:font-style :italic}

   ;font-family
   :serif {:font-family :serif}
   :sans-serif {:font-family :sans-serif}
   :sans {:font-family :sans-serif}
   :monospace {:font-family :monospace}
   :cursive {:font-family :cursive}
   :fantasy {:font-family :fantasy}
   :system-ui {:font-family :system-ui}
   :ui-serif {:font-family :ui-serif}
   :ui-sans-serif {:font-family :ui-sans-serif}
   :ui-monospace {:font-family :ui-monospace}
   :ui-rounded {:font-family :ui-rounded}
   :emoji {:font-family :emoji}
   :math {:font-family :math}
   :fangsong {:font-family :fangsong}

   ;position
   :absolute {:position :absolute}
   :relative {:position :relative}
   :fixed {:position :fixed}
   :fixed-fill {:position :fixed :top 0 :right 0 :bottom 0 :left 0}
   :absolute-fill {:position :absolute :top 0 :right 0 :bottom 0 :left 0}
   :absolute-centered {:position :absolute :top "50%" :left "50%" :transform "translate(-50%, -50%)"}

   ;box-sizing
   :border-box {:box-sizing :border-box}
   :content-box {:box-sizing :content-box}

   ;cursor
   :pointer {:cursor :pointer}
   :help {:cursor :help}
   :wait {:cursor :wait}
   :crosshair {:cursor :crosshair}
   :not-allowed {:cursor :not-allowed}
   :zoom-in {:cursor :zoom-in}

   ;blur
   :blur-1px {:filter "blur(1px)"}
   :blur-2px {:filter "blur(2px)"}
   :blur-3px {:filter "blur(3px)"}
   :blur-4px {:filter "blur(4px)"}
   :blur-5px {:filter "blur(5px)"}}))

(garden.core/css
 (mapv
  (fn [[k m]] [(str "." (name k)) m])
  (into (sorted-map)
        atomic-map)))

(garden.core/css
  (mapv
   ;escape the . in 1.2rem or 0.02ex below with regex
   (fn [[k m]] [(str "." (name k)) m])
   (sort-by
    (juxt (fn [[k m]] (str (ffirst m))) (fn [[k m]] (str (-> m first second))))
    atomic-map)))

(garden.core/css [[:ta-c {:text-align :center}]
                  [:ta-l {:text-align :left}] ])
