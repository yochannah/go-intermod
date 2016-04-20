(ns gointermod.search.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))

(re-frame/register-handler
  :toggle-evidence-codes
  (fn [db [_ args]]
    (assoc db :expand-evidence-codes?
           (not (:expand-evidence-codes? db)))))
