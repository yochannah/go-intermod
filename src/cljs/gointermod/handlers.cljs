(ns gointermod.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.orthologresults.handlers :as orthologs]
              [gointermod.search.handlers :as search]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :set-view
 (fn [db [_ active-view]]
   (.log js/console "setting view" (clj->js active-view))
   (assoc db :active-view active-view)))
