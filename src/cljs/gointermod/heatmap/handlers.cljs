(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))


(defn make-key [organism result]
;  (.log js/console "making key" (str organism "-" (get result 0) "-" (get result 15)))
  (keyword (str organism "-" (get result 0) "-" (get result 15)))
  )

(defn aggregate-row [organism result]
  ; (.log js/console "new mappy result"  (clj->js {:results result
  ;    :organism organism
  ;    :go-term (get result 16)
  ;    :ortholog (get result 0)}))
  {:results result
   :organism organism
   :go-id (get result 16)
   :go-term (get result 15)
   :ortholog (get result 0)})

(defn merge-results [results]
  (apply concat (map (fn [[_ organism]]
    (:results organism)
  ) results)))

(defn extract-results [search-results]
  (let [merged-results (merge-results search-results)]
  (.log js/console "Merged RESULTS" (clj->js merged-results)(clj->js (count merged-results)))
  (into (sorted-map)
      (map (fn [result]
          ;   (.log js/console "RES" (clj->js result))
        (let [organism (get result 3)
              k (make-key organism result)
              row (aggregate-row organism result)]
              {k row})
      )) merged-results)))



(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (.clear js/console)
    ;(.log js/console  (clj->js (extract-results (:multi-mine-results db)) ))
    (assoc db :heatmap (extract-results (:multi-mine-results db)))
  ))
