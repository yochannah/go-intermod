(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))


(defn extract-results [search-results]
  (reduce (fn [new-map [organism details]]
    (assoc new-map organism (:results details))) {} search-results))

(defn aggregate-by-ortholog [results]
  ;;todo: need to get the correct name of the orthologue or whatever and use it.
  ;;yeast, I'm looking at you.
    (reduce (fn [mine [name values]]
      (assoc mine name
        (group-by (fn [result]
            (get result 0)) values))
    ) {} results))

(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (.clear js/console)
    (assoc db :heatmap (aggregate-by-ortholog (extract-results (:multi-mine-results db))))
  ))
