(ns gointermod.search.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))

(re-frame/register-handler
 ;;toggle whether or not to show evidence code selection screen.
 ;;it's massive so we're defaulting to hide
  :toggle-evidence-codes
  (fn [db [_ args]]
    (assoc db :expand-evidence-codes?
           (not (:expand-evidence-codes? db)))))

(re-frame/register-handler
 ;;toggle whether or not a given evidence code is checked.
 :toggle-evidence-code
 (fn [db [_ index]]
   (update-in db [:search :evidence-codes index :checked] not)
   ))

(re-frame/register-handler
  ;;toggle whether or not a given evidence code is checked.
  :toggle-output-organism
  (fn [db [_ organism]]
    (update-in db [:organisms organism :output?] not)
    ))

(re-frame/register-handler
  ;;There's only one input organism for the search. Set it.
  :select-input-organism
  (fn [db [_ organism]]
    (.log js/console "organism changed:" (clj->js organism))
    (assoc db :selected-organism (keyword organism))
    ))
