(ns go-intermod.handlers
    (:require [re-frame.core :as re-frame]
              [go-intermod.db :as db]
              [go-intermod.search.handlers :as search]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
