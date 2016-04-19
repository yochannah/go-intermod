(ns go-intermod.search.handlers
    (:require [re-frame.core :as re-frame]
              [go-intermod.db :as db]))

(re-frame/register-handler
  :toggle-evidence-codes
  (fn [db [_ args]]
    (assoc db :expand-evidence-codes?
           (not (:expand-evidence-codes? db)))))
