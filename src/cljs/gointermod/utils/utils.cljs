(ns gointermod.utils.utils
(:require [re-frame.core :as re-frame]))

(defn get-id
  ;;this one saves time if we have a result row handy
  ([resultvec]
   (get-id (get resultvec 5) (get resultvec 6) (get resultvec 7)  (get resultvec 3))
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
  )))

(defn get-organism-details-by-name [organism-name]
  (let [organisms (re-frame/subscribe [:organisms])]
  (filter (fn [[organism vals]]
    (=  organism-name
        (:abbrev vals))
  ) @organisms)
))

(defn get-abbrev [source]
(let [mines (re-frame/subscribe [:organisms])
        organism (:abbrev (source @mines))]
(clj->js organism)))

(defn organism-name-to-id [organism-name]
  (let [[[organism details] x] (get-organism-details-by-name organism-name)]
    (:id details)
    )
  )
