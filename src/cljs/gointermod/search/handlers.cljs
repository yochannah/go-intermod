(ns gointermod.search.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.utils.utils :as utils]
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
 ;
 :update-search-results
 (fn [db [_ search-results]]
   (assoc db :search-results search-results)
))

(defn aggregate-by-species [search-results]
  "helper to get the counts of aggregate go terms per organism / go term / ontology branch "
  (reduce (fn [new-map [_ _ _ _ _ primary-id secondary-id symbol organism & args ]]
    (update-in new-map [(keyword organism ) (keyword (utils/get-id primary-id secondary-id symbol)) (keyword (last args))] inc))
      {} search-results)
)

(defn aggregate-by-orthologue [search-results]
  "helper to get the counts of aggregate go terms per organism / go term / ontology branch "
  (reduce (fn [new-map [_ _ _ _ _ primary-id secondary-id symbol organism & args ]]
    (update-in new-map [(keyword (utils/get-id primary-id secondary-id symbol)) (keyword (last args))] inc))
      {} search-results)
)

(re-frame/register-handler
 :aggregate-search-results
 (fn [db [_ search-results]]
   ;(.log js/console "Aggregate: (aggregate-by-species search-results)" (clj->js (aggregate-by-species search-results)))
   (assoc db :aggregate-results (aggregate-by-species search-results)))
)


(re-frame/register-handler
 :concat-results
 (fn [db [_ search-results source]]
;   (.log js/console (clj->js source) "Concat: " (clj->js (:results search-results)))
   (assoc-in db [:multi-mine-results source] search-results)
   (assoc-in db [:multi-mine-aggregate source] (aggregate-by-orthologue (:results search-results))))
)

(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    ;query humanmine
    (go (let
      [search-results (<! (comms/go-query (:selected-organism db) (:search-term db) :mouse))]
        ;add results to the db
      ;  (re-frame/dispatch [:update-search-results search-results])
        (re-frame/dispatch [:aggregate-search-results (:results search-results)])

          (comms/query-all-selected-organisms (:selected-organism db) (:search-term db))
))
    ;experimental multiples query
;  (go (let [search-results (<! (comms/query-all-selected-organisms (:selected-organism db) (:search-term db)))]
;        (.log js/console "Booya")
;  ))

    db))
