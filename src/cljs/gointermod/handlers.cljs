(ns gointermod.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.orthologresults.handlers :as orthologs]
              [gointermod.heatmap.handlers]
              [gointermod.ontology.handlers]
              [gointermod.enrichment.handlers]
              [gointermod.search.handlers :as search]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :set-view
 (fn [db [_ active-view]]
   (assoc db :active-view active-view)))

 (re-frame/register-handler
  :active-filter
  (fn [db [_ active-filter]]
    (re-frame/dispatch [:trigger-data-handler-for-active-view])
    (assoc db :active-filter active-filter)))

(def triggers
  {:ontology :load-go-graph
   :heatmap :aggregate-heatmap-results
   :enrichment :enrich-results
})

(re-frame/register-handler
 :trigger-data-handler-for-active-view
 (fn [db [_ _]]
   (let [view (:active-view db)
         handler-to-dispatch (view triggers)]
     (re-frame/dispatch [handler-to-dispatch])
  db)))
