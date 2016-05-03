(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))


(defn extract-results [search-results]
  (reduce (fn [new-vec [organism details]]
    (let [results (:results details)]
      (map (fn [result]
        (conj new-vec
         {:results results
          :organism organism
          :go-term (get result 16)
          :ortholog (get result 0)}))
        results))) [] search-results))


(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (.clear js/console)
;    (.log js/console  (clj->js (extract-results (:multi-mine-results db)) ))
    (assoc db :heatmap (extract-results (:multi-mine-results db)))
  ))
