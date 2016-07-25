(ns gointermod.search.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame :refer [after enrich]]
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
         (update-in new-map [(:display-ortholog-id result) (:ontology-branch result)] conj (:go-term result))
         (assoc-in [(:display-ortholog-id result) :is-selected?] true)
         (update-in [(:display-ortholog-id result) :dataset]
            (fn [dataset new-val]
              (conj (set dataset) new-val) ) (:data-set-name result) )
         (assoc-in [(:display-ortholog-id result) :human-ortholog] (:human-ortholog result))
         (assoc-in [(:display-ortholog-id result) :original-id] (:display-original-id result))
         (assoc-in [(:display-ortholog-id result) :original-input-gene] (:original-input-gene result))
       ))
      {} search-results)
)

(defn lookup-original-input-identifier [identifier result]
  (let [id-map (re-frame/subscribe [:mapped-resolved-ids])
  input-identifier (utils/get-id (get @id-map identifier)) ]
  (if input-identifier
    input-identifier
    (utils/get-id result :original)
)))

(defn nonhuman-ortholog-to-input-gene
  "Our queries automatically store lookup maps from original input gene to reolved ID, and from the human ortholog to its non-human orthologue result. This lookup allows us to trace back to the original input identifier. "
  [primary-id]
  (let [input-map (re-frame/subscribe [:input-map])
        mapped-ids (re-frame/subscribe [:mapped-resolved-ids])
        original (:original (get @mapped-ids primary-id))
        input-vals (get @input-map original)
        nonhuman-result (:input input-vals)
        human-result (:input (get @input-map primary-id))]
    (if (nil? nonhuman-result) ;;only one of these two will be non-null
      human-result nonhuman-result
  )))

(defn resultset-to-map [results]
  "translate that silly vector of results into a map with meaningful keys. This is for the main query."
  (map (fn [result]
    (let [original-primary-id (get result 7)
          ortho-primary-id (get result 3)
          human-ortholog (re-frame/subscribe [:input-gene-friendly-id (lookup-original-input-identifier original-primary-id result)])]

    { :human-ortholog @human-ortholog
      :original-input-gene (nonhuman-ortholog-to-input-gene original-primary-id)
      :ortho-db-id (get result 0)
      :ortho-symbol (get result 1)
      :ortho-secondary-id (get result 2)
      :ortho-primary-id ortho-primary-id
      :ortho-organism (get result 4)
      :original-db-id (get result 6)
      :original-symbol (get result 9)
      :original-secondary-id (get result 8)
      :original-primary-id original-primary-id
      :original-organism (get result 10)
      :go-id (get result 15)
      :go-term (get result 16)
      :ontology-branch (get result 17)
      :data-set-name (get result 12)
      :data-set-url (get result 13)
      :display-ortholog-id (utils/get-id result :ortholog)
      :display-original-id (lookup-original-input-identifier original-primary-id result)
     }
  )) results))

  (defn original-resultset-to-map [results]
    "translate that silly vector of results into a map with meaningful keys. This is only used for the supplementary query that is used for the original input (e.g. fetch the input gene itself as well as the homologues"
    (let [na "N/A" ;;hey jude
          original-datasource (re-frame/subscribe [:mapped-resolved-ids])
          input-map (re-frame/subscribe [:input-map])]
    (map (fn [result]
      (let [original-primary-id (get result 3)
            input (:input (get @input-map original-primary-id))
            human-ortholog @(re-frame/subscribe [:input-gene-friendly-id (lookup-original-input-identifier original-primary-id result)])]
        {:human-ortholog human-ortholog
        :original-input-gene input
        :ortho-db-id na
        :ortho-symbol na
        :ortho-secondary-id na
        :ortho-primary-id na
        :ortho-organism na
        :original-db-id (get result 0)
        :original-symbol (get result 1)
        :original-secondary-id (get result 2)
        :original-primary-id original-primary-id
        :original-organism (get result 4)
        :go-id (get result 7)
        :go-term (get result 8)
        :ontology-branch (get result 9)
        :data-set-name (:dataset (get @original-datasource original-primary-id (str na " - original input gene")))
        :data-set-url na
        :display-ortholog-id (utils/get-id result :original-gene-set)
        :display-original-id (lookup-original-input-identifier original-primary-id result)
       }
    )) results)))

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

(def extract-gene-map [
;;Middleware to ensure that the gene map is attached the the DB straight after the result is retrieved.
;;the purpose of the gene map is to ensure that organisms which don't have a human symbol (e.g. fly at this moment in time) are able to find out what the original "input" gene was.
  (enrich (fn [db [handler-name unmapped-results organism]]
  ;  (.log js/console "%c(:mapped-resolved-ids db)" "color:#fff;background:darkseagreen; border-radius:3px;font-weight:bold; padding:2px 5px;text-shadow:1px 1px rgba(0,0,0,0.2)" (clj->js (:mapped-resolved-ids db)))
    (if (or (= :human organism) (= (:selected-organism db) organism))
      (let [multi-mine-results (:human (:multi-mine-results db))
        mapped-results
        (reduce (fn [new-map result]
          (->
            (assoc-in new-map [(:original-primary-id result) :symbol] (:original-symbol result))
            (assoc-in [(:original-primary-id result) :secondary] (:original-secondary result)))

        ) (:mapped-resolved-ids db) multi-mine-results)]
        (assoc db :mapped-resolved-ids mapped-results))
      db)
))])

(re-frame/register-handler
 :concat-results
 extract-gene-map
 (fn [db [_ search-results source]]
   (let [mapped-results (resultset-to-map (:results search-results))
         status (result-status search-results mapped-results)
         aggregate (aggregate-by-orthologue mapped-results)
         results (->
          (update-in db [:multi-mine-results source] concat mapped-results)
          (assoc-in     [:organisms source :status] status)
          (update-in    [:multi-mine-aggregate source] concat aggregate)
          )]
     ;(.log js/console "%cmulti-mine-aggregate" "color:mediumorchid;font-weight:bold;" (clj->js aggregate))
   results)
))


(re-frame/register-handler
 :concat-original-genes
 extract-gene-map
 (fn [db [_ search-results organism]]
    (let [mapped-results (original-resultset-to-map (:results search-results))
          status (result-status search-results mapped-results)
          aggregate (aggregate-by-orthologue mapped-results)]
    (->
      (update-in db [:multi-mine-results organism] concat mapped-results)
      (assoc-in     [:organisms organism :status] status)
      (update-in    [:multi-mine-aggregate organism] concat aggregate))
)))



(defn search-token-fixer-upper "accept any separator, so long as it's newline, tab, space, or comma. Yeast will need special treatment."
  [term]
  (clojure.string/escape term
    {"\n" ","
     ";"  ","
     " "  ","
     "\t" ","
}))

(defn handle-human-orthologues-for-nonhuman-query
  "creates a data map such that any non-human organisms may use this for a lookup table if they're missing data. Also dispatches teh standard query using the human orthologues as the lookup values"
  [results db]
;(.log js/console "%cresults" "color:cornflowerblue;font-weight:bold;" (clj->js results))
  (if results
    (let [results-map
      (reduce (fn [new-map [symbol secondary primary dataset original]]
        (assoc new-map primary {:symbol symbol
                                :secondary secondary
                                :dataset dataset
                                :primary primary
                                :original original})) {} results)]
    ;;if we successfully retrieved 0 or more human orthologues for the non human identifiers, proceed with the standard query.
        (re-frame/dispatch [:save-mapped-resolved-ids results-map])
        (comms/query-all-selected-organisms (:selected-organism db)
          (clojure.string/join "," (keys results-map)))
    )
    ;;so this is what happens when we had an error trying to retrieve orthologues.
    (re-frame/dispatch [:error-loading-human-orthologs])))

(defn resolve-id-map [resolved-ids]
  ;;could handle these better
  (cond (seq (:unresolved resolved-ids))
    (.debug js/console "%cUnresolved-ids" "background-color:#333;color:hotpink;font-weight:bold;" (clj->js (:unresolved resolved-ids))))

  (reduce (fn [new-map entry]
    ;(.log js/console "%centry" "color:#fff;background:purple; border-radius:3px;font-weight:bold; padding:2px 5px;" (clj->js entry))
    (let [input (first (:input entry))
          vals {:input (keyword input)
           :second (:secondaryIdentifier (:summary entry))
           :symbol (:symbol (:summary entry))
           :primary (:primaryIdentifier (:summary entry))}]
    (assoc new-map
      (:primaryIdentifier (:summary entry)) vals
      input vals
    ))) {} (:MATCH (:matches resolved-ids))))

(re-frame/register-handler
  :save-mapped-resolved-ids
  (fn [db [_ ids]]
    (assoc db :mapped-resolved-ids (merge ids (:mapped-resolved-ids db)))
))

(re-frame/register-handler
  :save-input-map
  (fn [db [_ ids]]
    (assoc db :input-map (merge ids (:input-map db)))
))


(re-frame/register-handler
  ;;What do we do when a search button is pressed? This.
  :perform-search
  (fn [db [_ _]]
    (let [input-org (:selected-organism db)
          search-terms (search-token-fixer-upper (:search-term db))
          search-terms-vec (clojure.string/split search-terms ",")]
      (go (let [resolved-ids (<! (comms/resolve-id input-org search-terms-vec))
                mapped-resolved-ids (resolve-id-map resolved-ids)]
        (re-frame/dispatch [:save-input-map mapped-resolved-ids])
        ;(.log js/console "%cmapped-resolved-ids" "color:orange;font-weight:bold;" (clj->js mapped-resolved-ids) (clj->js input-org))
      (if (= input-org :human)
        ;if the input organism is human, go straight to the remote organism mines with the main query.
        ;asynchronously query all dem mines and add the results to the db
          (go (comms/query-all-selected-organisms (:selected-organism db) (clojure.string/join "," (keys mapped-resolved-ids))))

        ; if the input organism is not human, first we have to resolve the
        ; identifiers to their human orthologues, then do the above query
        (go (let [response (<! (comms/get-human-orthologs search-terms input-org))
                  results (:results response)]
          (handle-human-orthologues-for-nonhuman-query results db)
         ))
        )))
      ;;set state of app to no longer show home page
    (re-frame/dispatch [:initialised])
      ;;remove stuff from the previous search. we leave the resolved IDs though because they don't change. wait, the datasets might. todo, cleanse datasets.
    (dissoc db
            :multi-mine-results
            :enrichment
            :multi-mine-aggregate
            :go-terms
            :mapped-resolved-ids
            :input-map
            :go-ontology
            :human-orthologs-of-other-input-organism))))

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
