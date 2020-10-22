(ns style-plus.shorthand)

(def css-sh
  {:ai {:name :align-items
        :vals {:c :center
               :fs :flex-start
               :fe :flex-end
               :n :normal
               :s :start
               :e :end
               :b :baseline}}

   :b {:name :border}
   :br {:name :border-right}
   :bl {:name :border-left}
   :bt {:name :border-top}
   :bb {:name :border-bottom}

   :bgi {:name :background-image}
   :bg {:name :background}
   :bgs {:name :background-size}
   :bgc {:name :background-color}
   :bgp {:name :background-position}
   :bgr {:name :background-repeat
         :vals {:nr :no-repeat
                :rx :repeat-x
                :ry :repeat-y
                :r :round
                :s :space}}

   :c {:name :color
       :vals {:t :transparent
              :b :blue
              :c :cyan
              :p :pink
              :m :magenta
              :v :violet
              :l :lime
              :g :green
              :i :indigo
              :k :khaki
              :n :navy
              :y :yellow
              :o :orange
              :r :red
              :s :silver
              :u :unset
              :w :white
              :ws :whitesmoke}}

   :d {:name :display
       :vals {:f :flex
              :b :block
              :t :table
              :g :grid
              :li :list-item
              :n :none
              :i :inline
              :ib :inline-block
              :it :inline-table
              :if :inline-flex
              :ig :inline-grid}}

   :f {:name :flex}
   :fb {:name :flex-basis}
   :fg {:name :flex-grow}
   :fs {:name :flex-shrink}
   :fw {:name :flex-wrap
        :vals {:w :wrap
               :n :nowrap
               :wr :wrap-reverse}}
   :fd {:name :flex-direction
        :vals {:r :row
               :rr :row-reverse
               :c :column
               :cr :column-reverse}}

   :g {:name :grid}
   :ga {:name :grid-area}
   :gac {:name :grid-auto-columns}
   :gaf {:name :grid-auto-flow}
   :gar {:name :grid-auto-rows}
   :gc {:name :grid-column}
   :gce {:name :grid-column-end}
   :gcg {:name :grid-column-gap}
   :gcs {:name :grid-column-gap}
   :gg {:name :grid-gap}
   :gr {:name :grid-row}
   :gre {:name :grid-row-end}
   :grg {:name :grid-row-gap}
   :grs {:name :grid-row-start}
   :gt {:name :grid-template}
   :gta {:name :grid-template-areas}
   :gtr {:name :grid-template-rows}

   :h {:name :height}

   :jc {:name :justify-content
        :vals {:c :center
               :s :start
               :e :end
               :fs :flex-start
               :fe :flex-end
               :l :left
               :r :right
               :n :normal
               :sb :space-between
               :sa :space-around
               :se :space-evenly}}

   :ji {:name :justify-items
        :vals {:a :auto
               :n :normal
               :c :center
               :s :start
               :e :end
               :fs :flex-start
               :fe :flex-end
               :ss :self-start
               :se :self-end
               :l :left
               :r :right}}

   :lh {:name :line-height}

   :m {:name :margin}
   :mr {:name :margin-right}
   :ml {:name :margin-left}
   :mt {:name :margin-top}
   :mb {:name :margin-bottom}

   :p {:name :padding}
   :pr {:name :padding-right}
   :pl {:name :padding-left}
   :pt {:name :padding-top}
   :pb {:name :padding-bottom}

   :o {:name :opacity}

   :ta {:name :text-align
        :vals {:c :center
               :r :right
               :l :left
               :j :justify
               :ja :justify-all
               :s :start
               :e :end
               :mp :match-parent}}
   :tt {:name :text-transform
        :vals {:u :uppercase
               :l :lowercase
               :c :captitalize}}
   :td {:name :text-decoration
        :vals {:u :underline
               :o :overline
               :lt :line-through}}
   :tdl {:name :text-decoration-line
         :vals {:u :underline
                :o :overline
                :lt :line-through}}
   :tdc {:name :text-decoration-color}
   :tds {:name :text-decoration-style
         :vals {:s :solid
                :w :wavy}}
   :tdt {:name :text-decoration-thickness
         :vals {:ff :from-font}}

   :w {:name :width}

   :ws {:name :white-space
        :vals {:n :nowrap
               :p :pre
               :pw :pre-wrap
               :pl :pre-line}}

   :va {:name :vertical-align
        :vals {:b :baseline
               :s :sub
               :t :top
               :tt :text-top
               :tb :text-bottom
               :m :middle}}

   :z {:name :z-index}})

(def scales
  [{:key :o :range [0 100 5] :unit :%}
   {:key :w
    :range [0 100 5] :unit :%}
   {:key :h
    :range [0 100 5] :unit :%}
   {:key :hover-dim:hover
    :range [0 100 5] :unit :%}
   {:key :ex
    :range [0 0.2 0.01] :unit :ex}
   {:key :font-weight
    :range [100 900 100]}
   {:key :blur
    :range [0 5 1] :unit :px}
   {:key :border-radius
    :range [0 20 1] :unit :px}
   {:key :font-size
    :range [0 48 1] :unit :px}
   {:key :top
    :range [0 100 25] :unit :%}
   {:key :left
    :range [0 100 25] :unit :%}
   {:key :bottom :range [0 100 25] :unit :%}
   {:key :right :range [0 100 25] :unit :%}
   {:key :lh :range [0 48 1] :unit :px}
   {:key :lh :range [0 4 0.1] :unit :rem}])

(def css-sh-by-propname
  (reduce (fn [acc [_ v]] (assoc acc (:name v) (:vals v))) {} css-sh))

(defn val-sh [v k]
  (if (keyword? v)
      (or (some-> css-sh k :vals v)
          (some-> css-sh-by-propname k v) v)
    v))

(defn key-sh [k]
  (if (keyword? k)
    (or (some-> css-sh k :name) k)
    k))
