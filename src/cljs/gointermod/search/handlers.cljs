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

(defn aggregate-by-orthologue [search-results]
  "helper to get the counts of aggregate go terms per organism / go term / ontology branch "
  (reduce (fn [new-map [original-symbol original-secondary-id original-primary-id original-organism _ primary-id secondary-id symbol organism & args ]]
    (let [id (keyword (utils/get-id primary-id secondary-id symbol organism))
          original-id (utils/get-id original-primary-id original-secondary-id original-symbol original-organism)]
      (->
        (update-in new-map [id (keyword (last args))] inc)
        (assoc-in [id :is-selected?] true)
        (assoc-in [id :original-id] original-id)
      )))
      {} search-results)
)

(re-frame/register-handler
 :concat-results
 (fn [db [_ search-results source]]
   (assoc-in db [:multi-mine-results source] search-results)
   (assoc-in db [:multi-mine-aggregate source] (aggregate-by-orthologue (:results search-results))))
)

(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    ;asynchronously query all dem mines and add the results to the db
    (go
      (comms/query-all-selected-organisms (:selected-organism db) (:search-term db))
) (dissoc db :multi-mine-results :multi-mine-aggregate)))
