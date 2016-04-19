(ns gointermod.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.search.handlers :as search]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
