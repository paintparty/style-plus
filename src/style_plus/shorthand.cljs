(ns style-plus.shorthand)

(def css-sh
  {:ta {:name :text-align
        :value {:c :center :r :right :l :left :j :justify}}
   :tt {:name :text-transform
        :value {:u :uppercase :l :lowercase :c :captitalize}}
   :td {:name :text-decoration}
   :tdl {:name :text-decoration-line
         :value {:u :underline :o :overline :lt :line-through}}
   :tdc {:name :text-decoration-color}
   :tds {:name :text-decoration-style
         :value {:s :solid :w :wavy}}
   :tdt {:name :text-decoration-thickness
         :value {:ff :from-font}}
   :o {:name :opacity}
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
         :value {:nr :no-repeat :rx :repeat-x :ry :repeat-y :r :round :s :space}}
   :f {:name :flex}
   :fb {:name :flex-basis}
   :fg {:name :flex-grow}
   :fs {:name :flex-shrink}
   :fw {:name :flex-wrap
        :vals {:w :wrap
               :nw :nowrap
               :wr :wrap-reverse}}
   :fd {:name :flex-direction
        :vals {:r :row :rr :row-reverse :c :column :cr :column-reverse}}
   :jc {:name :justify-content
        :vals {:c :center
               :fs :flex-start
               :fe :flex-end
               :l :left
               :r :right
               :sb :space-between
               :sa :space-around
               :se :space-evenly
               :s :stretch}}
   :ai {:name :align-items
        :vals {:c :center :fs :flex-start :fe :flex-end :s :stretch}}
   :w {:name :width}
   :h {:name :height}
   :p {:name :padding}
   :pr {:name :padding-right}
   :pl {:name :padding-left}
   :pt {:name :padding-top}
   :pb {:name :padding-bottom}
   :m {:name :margin}
   :mr {:name :margin-right}
   :ml {:name :margin-left}
   :mt {:name :margin-top}
   :mb {:name :margin-bottom}
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
   :c {:name :color
       :vals {:t :transparent
              :b :blue
              :c :cyan
              :p :pink
              :m :magenta
              :v :violet
              :l :lime
              :g :green
              :i :indigo :k :khaki
              :n :navy
              :y :yellow
              :o :orange
              :r :red
              :s :silver
              :u :unset
              :w :white
              :ws :whitesmoke}}})

(def css-sh-by-propname
  (reduce (fn [acc [_ v]] (assoc acc (:name v) (:vals v))) {} css-sh))

(defn val-sh [v k]
  (if (keyword? v)
      (or (some-> css-sh k :vals v) (some-> css-sh-by-propname k v) v)
    v))

(defn key-sh [k]
  (if (keyword? k)
    (or (some-> css-sh k :name) k)
    k))
