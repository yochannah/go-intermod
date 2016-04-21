(ns gointermod.search.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [cljs.core.async :refer [<!]]
              [gointermod.utils.comms :as comms]))

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
   (update-in db [:evidence-codes index :checked] not)
   ))

(re-frame/register-handler
  ;;toggle whether or not a given organism is checked.
  :toggle-output-organism
  (fn [db [_ organism]]
    (update-in db [:organisms organism :output?] not)
    ))

(re-frame/register-handler
  ;;There's only one input organism for the search. Set it.
  :select-input-organism
  (fn [db [_ organism]]
    (assoc db :selected-organism (keyword organism))
    ))

(re-frame/register-handler
  ;;capture search term in app db
  :update-search-term
  (fn [db [_ term]]
    (assoc db :search-term term)
    ))

(re-frame/register-handler
 :update-search-results
 (fn [db [_ search-results]]
   (assoc db :search-results search-results)
))

(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    ;query humanmine
    (go (let
      [search-results (<! (comms/go-query :human (:search-term db)))]
        (.log js/console search-results)
        ;add results to the db
        (re-frame/dispatch [:update-search-results search-results])
)) db))
