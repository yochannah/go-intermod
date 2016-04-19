(ns go-intermod.handlers
    (:require [re-frame.core :as re-frame]
              [go-intermod.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
