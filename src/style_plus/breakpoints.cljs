(ns style-plus.breakpoints)

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

(defn above
  "Get the min-width breakpoint media-query"
  [kw m]
  {:min-width (some-> (get m kw) normalize-css-value)})

(defn- css-below-val [kw m]
  (when-let [v (some-> (get m kw) normalize-css-value)
             ]
    (let [unit (if (re-find #"em$" v) "em" "px")
          amount (if (= unit "em") 0.00125 0.02)]
      (str "calc(" v " - " amount unit ")"))))

(defn below
  "Get the max-width breakpoint media-query"
  [kw m]
  {:max-width (css-below-val kw m)})

(defn between
  "Get the max-width / min-width media-query"
  [start-k end-k m]
  {:max-width (css-below-val end-k m)
   :min-width (some-> (get m start-k) normalize-css-value)})
