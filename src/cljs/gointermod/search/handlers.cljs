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
  (reduce (fn [new-map result]
     (->
         (update-in new-map [(:display-ortholog-id result) (:ontology-branch result)] inc)
         (assoc-in [(:display-ortholog-id result) :is-selected?] true)
         (update-in [(:display-ortholog-id result) :dataset]
            (fn [dataset new-val]
              (conj (set dataset) new-val) ) (:data-set-name result) )
         (assoc-in [(:display-ortholog-id result) :original-id] (:display-original-id result))
       ))
      {} search-results)
)

(defn resultset-to-map [results]
  "translate that silly vector of results into a map with meaninful keys"
  (map (fn [result]
    {:results result
      :ortho-db-id (get result 0)
      :ortho-symbol (get result 1)
      :ortho-secondary-id (get result 2)
      :ortho-primary-id (get result 3)
      :ortho-organism (get result 4)
      :original-db-id (get result 6)
      :original-symbol (get result 9)
      :original-secondary-id (get result 8)
      :original-primary-id (get result 7)
      :original-organism (get result 10)
      :go-id (get result 15)
      :go-term (get result 16)
      :ontology-branch (get result 17)
      :data-set-name (get result 12)
      :data-set-url (get result 13)
      :display-ortholog-id (utils/get-id result :ortholog)
      :display-original-id (utils/get-id result :original)
     }
  ) results))

(defn result-status [search-results]
  (if (:error search-results)
    ;;return the error details if there are some
    {:status :error
     :details (:error search-results)}
    ;;else, return the count of results
    {:status :success
     :details (count (:results search-results))}
    )
   )

(re-frame/register-handler
 ;;I think I've done this the long way around but my brain is being slippery.
 :set-status-loading (fn [db [_ _]]
  (assoc db :organisms
    (reduce (fn [new-map [id organism]]
      (if (:output? organism)
        ;;if we're searching this organism, output a loading status
        (assoc new-map id
          (merge organism {:status {:status :loading}}))
        ;;else, the status is n/a: we're not searching at all.
        (assoc new-map id
          (merge organism {:status {:status :na}})))
    ) {} (:organisms db))
)))

(re-frame/register-handler
 :concat-results
 (fn [db [_ search-results source]]
   (re-frame/dispatch [:aggregate-heatmap-results])
   (re-frame/dispatch [:enrich-results])
   (let [mapped-results (resultset-to-map (:results search-results))
         status (result-status search-results)]
   (->
    (assoc-in db [:multi-mine-results source] mapped-results)
    (assoc-in [:organisms source :status] status)
    (assoc-in [:multi-mine-aggregate source] (aggregate-by-orthologue mapped-results))))
))

(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    ;asynchronously query all dem mines and add the results to the db
    (go
      (comms/query-all-selected-organisms (:selected-organism db) (:search-term db)))
    (re-frame/dispatch [:initialised])
    (dissoc db :multi-mine-results :multi-mine-aggregate)))

(re-frame/register-handler
 ;;saves the most recent query xml to be associated with a given organism
 :initialised
 (fn [db [_ _]]
  (assoc db :initialised true)
  ))


(re-frame/register-handler
 ;;saves the most recent query xml to be associated with a given organism
 :save-query
 (fn [db [_ query organism]]
  (assoc-in db [:organisms organism :query] query)
  ))

  (re-frame/register-handler
    ;;There's only one input organism for the search. Set it.
    :active-modal
    (fn [db [_ organism]]
      (assoc-in db [:active-modal] organism)
      ))
