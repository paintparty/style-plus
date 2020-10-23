(ns style-plus.atomic
  (:require
   [style-plus.shorthand :as shorthand]))

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

(defn scale [{:keys [k sh-k max step unit val-fn]}]
  (reduce (fn [acc v]
            (let [css-val (str (if val-fn (val-fn v) v) (name unit))
                  prefix (when sh-k (str (name sh-k) "-"))]
              (assoc acc (keyword (str prefix css-val)) {k css-val})))
          {}
          (range 0 (+ step max) step)))

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
  shorthand-enum-map
  (flex-atomic :row)
  (flex-atomic :column)
  (scale {:k :opacity :sh-k :o :max 100 :step 5 :unit :%})
  {:p-0 {:padding 0}}
  (margin-padding :padding 30 1)
  {:m-0 {:margin 0}}
  (margin-padding :margin 30 1)
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
  (scale {:k :width :sh-k :w :max 100 :step 5 :unit :%})
  {:width-0 {:width 0}}
  (scale {:k :height :sh-k :h :max 100 :step 5 :unit :%})
  {:height-0 {:height 0}}
  (scale {:k :font-size :max 48 :step 1 :unit :px})
  (scale {:k :line-height :sh-k :lh :max 48 :step 1 :unit :px})
  (scale {:k :letter-spacing :max 30 :step 1 :unit :ex :val-fn #(/ % 100)})
  (scale {:k :border-radius :sh-k :radius :max 30 :step 1 :unit :px})
  {:radius-50% {:border-radius :50%}}
  {:radius-100% {:border-radius :100%}}
  {:radius-pill {:border-radius "9999px"}}
  {:100 {:font-weight 100}
   :200 {:font-weight 200}
   :300 {:font-weight 300}
   :400 {:font-weight 400}
   :500 {:font-weight 500}
   :600 {:font-weight 600}
   :700 {:font-weight 700}
   :800 {:font-weight 800}
   :900 {:font-weight 900}}
  {:bgi-contain {:width :100%
                 :height :100%
                 :background-size :contain
                 :background-position [[:center :center]]
                 :background-repeat :no-repeat}
   :bgi-cover {:width :100%
               :height :100%
               :background-size :cover
               :background-position [:center :center]
               :background-repeat :no-repeat}
   :uppercase {:text-transform :uppercase}
   :capitalize {:text-transform :capitalize}
   :line-through {:text-decoration :line-through}
   :underline {:text-decoration :underline}
   :nowrap {:white-space :nowrap}
   :grid {:display :grid}
   :flex {:display :flex}
   :block {:display :block}
   :inline {:display :inline}
   :table {:display :table}
   :none {:display :none}
   :black {:color :black}
   :ta-c {:text-align :center}
   :ta-l {:text-align :left}
   :ta-r {:text-align :r}
   :italic {:font-style :italic}
   :serif {:font-family :serif}
   :sans {:font-family :sans-serif}
   :absolute {:position :absolute}
   :relative {:position :relative}
   :fixed {:position :fixed}
   :fixed-fill {:position :fixed :top 0 :right 0 :bottom 0 :left 0}
   :absolute-fill {:position :absolute :top 0 :right 0 :bottom 0 :left 0}
   :absolute-centered {:position :absolute :top :50% :left :50% :transform "translate(-50%, -50%)"}
   :pointer {:cursor :pointer}
   :outline {:outline [[1 :dotted :silver]]}
   :white {:color :white}
   :whitesmoke {:color :whitesmoke}
   :silver {:color :silver}
   :gray {:color :gray}
   :grey {:color :grey}
   :bgc-white {:background-color :white}
   :bgc-whitesmoke {:background-color :whitesmoke}
   :bgc-silver {:background-color :silver}
   :bgc-gray {:background-color :gray}
   :bgc-grey {:background-color :grey}
   :bgc-black {:background-color :black}
   :b-white {:border [[1 :solid :white]]}
   :b-whitesmoke {:border [[1 :solid :whitesmoke]]}
   :b-silver {:border [[1 :solid :silver]]}
   :b-gray {:border [[1 :solid :gray]]}
   :b-grey {:border [[1 :solid :grey]]}
   :b-black {:border [[1 :solid :black]]}
   :blur-1px {:filter "blur(1px)"}
   :blur-2px {:filter "blur(2px)"}
   :blur-3px {:filter "blur(3px)"}
   :blur-4px {:filter "blur(4px)"}
   :blur-5px {:filter "blur(5px)"}}))

