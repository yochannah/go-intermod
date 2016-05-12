(ns gointermod.enrichment.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [cljs.core.async :refer [put! chan <! >! timeout close!]]))




(defn enrich [db]
  (.log js/console (clj->js db))
  (let [organisms (re-frame/subscribe [:organisms])])
  ;  (go (let [res (<! (im/enrichment
  ;    (select-keys upstream-data [:service])
  ;     {:widget "go_enrichment_for_gene"
  ;      :maxp (:maxp @persistent-state)
  ;      :format "json"
  ;      :correction (:correction "")}
  ;      :ids (:payload (:data upstream-data))))]
  ;   ))
  )

(re-frame/register-handler
 :enrich-results
 (fn [db [_ _]]
   ;(.clear js/console)
   (enrich db)
   ;(assoc db :heatmap (extract-results (:multi-mine-results db)))
 db))

 (re-frame/register-handler
  :test-correction
  (fn [db [_ correction-value]]
    (assoc db :test-correction correction-value)))

(re-frame/register-handler
 :max-p
 (fn [db [_ max-p-value]]
   (assoc db :max-p max-p-value)))
