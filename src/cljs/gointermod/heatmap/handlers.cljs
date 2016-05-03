(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))


(defn extract-results [search-results]
  (reduce (fn [new-map [organism details]]
    (assoc new-map organism (:results details))) {} search-results))

(defn aggregate-by-go-term [results]
    (map (fn [[ortholog ortholog-results] result]
      ;(.log js/console "agg by go term" (clj->js result) (clj->js k) (clj->js v))
      ;(.log js/console (clj->js (group-by (fn [ortholog-result] (get ortholog-result 15)) ortholog-results)))
      (group-by (fn [ortholog-result] (get ortholog-result 15)) ortholog-results)
      ) results)
  )

  ; {
  ;   :organism "human"
  ;   :ortholog "adh5"
  ;   :go-term "Something"
  ;   :results [array of shit]
  ; }

(defn aggregate-by-ortholog [results]
  ;;todo: need to get the correct name of the orthologue or whatever and use it.
  ;;yeast, I'm looking at you.

    (reduce (fn [mine [name values]]
      (assoc mine name
        (->
          (group-by (fn [result] (get result 0)) values)
          (aggregate-by-go-term)))
        ) {} results))

(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (.clear js/console)
    (.log js/console (clj->js (assoc db :heatmap (aggregate-by-ortholog (extract-results (:multi-mine-results db))))))
    (assoc db :heatmap (aggregate-by-ortholog (extract-results (:multi-mine-results db))))
  ))
