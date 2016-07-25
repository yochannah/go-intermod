(ns gointermod.utils.utils
(:require [re-frame.core :as re-frame]))



(defn search-token-fixer-upper "accept any separator, so long as it's newline, tab, space, or comma. Yeast will need special treatment."
  [term]
  (clojure.string/escape term
    {"\n" ","
     ";"  ","
     " "  ","
     "\t" ","
}))

(defn get-id
  ;;this one saves time if we have a result row handy
  ([resultvec original-or-ortholog]
   (cond
     (= :ortholog original-or-ortholog)
      (get-id (get resultvec 3) (get resultvec 2) (get resultvec 1)  (get resultvec 4))
     (= :original original-or-ortholog)
      (get-id (get resultvec 7) (get resultvec 8) (get resultvec 9)  (get resultvec 10))
     (= :original-gene-set original-or-ortholog)
      (get-id (get resultvec 3) (get resultvec 2) (get resultvec 1)  (get resultvec 4)))
  )
  ([primary secondary symbol organism]
  "returns first non-null identifier, preferring symbol or primary id"
  (if (= organism "S. cerevisiae")
    ;;if it's yeast, we want secondary identifier first
    ;(do
      ;(.log js/console "yeast")
      (first (remove nil? [secondary symbol primary]))
      ;)
    ;;else we want the symbol
    (first (remove nil? [symbol secondary primary]))
  ))
  ([identifier-map]
   "expects an identifier map with input, secondary, & symbol values. Returns the first which has a value. Only designed to work on :human data since :human is hte hub"
;   (.log js/console "%cStuff:" "color:goldenrod;font-weight:bold;" (clj->js identifier-map) (first (remove nil? [(:input identifier-map) (:symbol identifier-map) (:secondary identifier-map)])))
    (first (remove nil? [(:input identifier-map) (:symbol identifier-map) (:secondary identifier-map)]))
   )
  )

(defn get-organism-details-by-name [organism-name]
  (let [organisms (re-frame/subscribe [:organisms])]
  (filter (fn [[organism vals]]
    (=  organism-name
        (:abbrev vals))
  ) @organisms)
))

(defn get-abbrev [organism-keyword]
(let [mines (re-frame/subscribe [:organisms])
        organism (:abbrev (organism-keyword @mines))]
(clj->js organism)))

(defn organism-name-to-id [organism-name]
  (let [[[organism details] x] (get-organism-details-by-name organism-name)]
    (:id details)
    )
  )

(defn loader []
     [:div.loader
      [:div.worm.loader-organism]
      [:div.zebra.loader-organism]
      [:div.human.loader-organism]
      [:div.yeast.loader-organism]
      [:div.rat.loader-organism]
      [:div.mouse.loader-organism]
      [:div.fly.loader-organism]]
  )
