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

(defn lookup-original-input-identifier [identifier result]
  (let [id-map (re-frame/subscribe [:mapped-resolved-ids])
        input-identifier (get @id-map identifier)]
  (if input-identifier
    input-identifier
    (utils/get-id result :original)
    )
))

(defn resultset-to-map [results]
  "translate that silly vector of results into a map with meaninful keys"
  (map (fn [result]
    {;:results result
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
      :display-original-id (lookup-original-input-identifier (get result 7) result)
     }
  ) results))

  (defn original-resultset-to-map [results]
    "translate that silly vector of results into a map with meaninful keys"
    (let [na "N/A" ;;hey jude
          original-datasource (re-frame/subscribe [:human-orthologs-of-other-input-organism])]
    (map (fn [result]
      {;:results result
        :ortho-db-id na
        :ortho-symbol na
        :ortho-secondary-id na
        :ortho-primary-id na
        :ortho-organism "H. sapiens"
        :original-db-id (get result 0)
        :original-symbol (get result 1)
        :original-secondary-id (get result 2)
        :original-primary-id (get result 3)
        :original-organism (get result 4)
        :go-id (get result 7)
        :go-term (get result 8)
        :ontology-branch (get result 9)
        :data-set-name (get @original-datasource (get result 1) (str na " - original input gene"))
        :data-set-url na
        :display-ortholog-id (utils/get-id result :original-gene-set)
        :display-original-id (utils/get-id result :original-gene-set)
       }
    ) results)))

(defn result-status [search-results mapped-results]
  (if (seq search-results)
    ;;if there even are any results:
    (if (:error search-results)
      ;;return the error details if there are some
      {:status :error
       :details (:error search-results)}
      ;;else, return the count of results
      {:status :success}
      )
    ;;if the server didn't respond or something
    {:status :error
     :details "Unable to connect to server"}
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
   (let [mapped-results (resultset-to-map (:results search-results))
         status (result-status search-results mapped-results)
         aggregate (aggregate-by-orthologue mapped-results)]
  ;   (.log js/console "%caggregate %s" "color:firebrick;font-weight:bold;" (clj->js source)(clj->js aggregate))
   (->
    (update-in db [:multi-mine-results source] concat mapped-results)
    (assoc-in     [:organisms source :status] status)
    (update-in    [:multi-mine-aggregate source] concat aggregate)))
))

(re-frame/register-handler
 :concat-original-genes
 (fn [db [_ search-results]]
    (let [mapped-results (original-resultset-to-map (:results search-results))
          status (result-status search-results mapped-results)
          aggregate (aggregate-by-orthologue mapped-results)]
    (->
      (update-in db [:multi-mine-results :human] concat mapped-results)
      (update-in  [:multi-mine-aggregate :human] concat aggregate))
)))



(defn search-token-fixer-upper "accept any separator, so long as it's newline, tab, space, or comma. Yeast will need special treatment."
  [term]
  (clojure.string/escape term
    {"\n" ","
     ";"  ","
     " "  ","
     "\t" ","
}))

(defn handle-human-orthologues-for-nonhuman-query [results db]
  (if results
    (let [results-map (reduce (fn [new-map [k v]] (assoc new-map k v)) {} results)]
    ;;if we successfully retrieved 0 or more human orthologues for the non human identifiers, proceed with the standard query.
      (.log js/console "%cresults" "color:cornflowerblue;font-weight:bold;" (clj->js results-map))
      (re-frame/dispatch [:human-orthologs-of-other-input-organism results-map])
      (comms/query-all-selected-organisms (:selected-organism db)
          ;;every even result is an ID, every odd is the data type
         (clojure.string/join "," (keys results-map)))
      )
    ;;so this is what happens when we had an error trying to retrieve orthologues.
    (re-frame/dispatch [:error-loading-human-orthologs])))

    (re-frame/register-handler
      ;;What do we do when a search button is pressed? This.
      :human-orthologs-of-other-input-organism
(fn [db [_ orthologs]]
  (.log js/console "%corthologs" "color:hotpink;font-weight:bold;" (clj->js orthologs))
  (assoc db :human-orthologs-of-other-input-organism orthologs)
  ))

(defn resolve-id-map [resolved-ids]
  (cond (some? (:unresolved resolved-ids))
    (.debug js/console "%cUnresolved-ids" "background-color:#333;color:hotpink;font-weight:bold;" (clj->js (:unresolved resolved-ids))))

  (reduce (fn [new-map entry]
    (assoc new-map
      (:primaryIdentifier (:summary entry))
      (keyword (first (:input entry)))
    )) {} (:MATCH (:matches resolved-ids))))

(re-frame/register-handler
  :save-mapped-resolved-ids
  (fn [db [_ ids]]
    (assoc db :mapped-resolved-ids ids)
))

(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    ;;if the input genes aren't human, we'll need to resolve them to their human orthologues.
    (let [input-org (:selected-organism db)
          search-terms (search-token-fixer-upper (:search-term db))
          search-terms-vec (clojure.string/split search-terms ",")]
      (if (= input-org :human)

        ;;;;;;;;;;EXPERIMENTAL ZONE
        (go (let [resolved-ids (<! (comms/resolve-id :human search-terms-vec))
                  mapped-resolved-ids (resolve-id-map resolved-ids)]

              (re-frame/dispatch [:save-mapped-resolved-ids mapped-resolved-ids])
        ;if the input organism is human, go straight to the remote organism mines with the main query.
        ;asynchronously query all dem mines and add the results to the db
            (go (comms/query-all-selected-organisms (:selected-organism db) (clojure.string/join "," (keys mapped-resolved-ids)))))

        )

        ;;;;;;;

        ; if the input organism is not human, first we have to resolve the
        ; identifiers to their human orthologues, then do the above query
        (go (let [response (<! (comms/get-human-orthologs search-terms input-org))
                  results (:results response)]
          (handle-human-orthologues-for-nonhuman-query results db)
         ))
        )
    (re-frame/dispatch [:initialised])
    (dissoc db :multi-mine-results :enrichment :multi-mine-aggregate :go-terms :go-ontology :human-orthologs-of-other-input-organism))))

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
