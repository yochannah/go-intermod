(ns gointermod.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.orthologresults.handlers :as orthologs]
              [gointermod.heatmap.handlers]
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
    (re-frame/dispatch [:enrich-results])
    (re-frame/dispatch [:aggregate-heatmap-results])
    (assoc db :active-filter active-filter)))
