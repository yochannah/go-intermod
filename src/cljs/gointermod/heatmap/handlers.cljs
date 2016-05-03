(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))


(defn make-key [organism result]
  ;(.log js/console "making key" (str organism "-" (get result 0) "-" (get result 15)))
  (keyword (str organism "-" (get result 0) "-" (get result 15)))
  )

(defn aggregate-row [details organism result]
  ; (.log js/console "new mappy result"  (clj->js {:results result
  ;    :organism organism
  ;    :go-term (get result 16)
  ;    :ortholog (get result 0)}))
  {:results result
   :organism organism
   :go-term (get result 16)
   :ortholog (get result 0)})

(defn extract-results [search-results]
  (.log js/console "EXTRACT RESULTS" (clj->js search-results))
  (into (sorted-map) (map (fn [[organism details] x]
    (.log js/console "organis" (clj->js organism))

    (map (fn [ result]
       {(make-key organism result)
        (aggregate-row details organism result)}
         ) (:results details))
            ) search-results)))



(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (.clear js/console)
    ;(.log js/console  (clj->js (extract-results (:multi-mine-results db)) ))
    (assoc db :heatmap (extract-results (:multi-mine-results db)))
  ))
