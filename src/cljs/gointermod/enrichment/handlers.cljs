(ns gointermod.enrichment.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.utils.comms :as comms]
              [gointermod.utils.utils :as utils]
              [gointermod.utils.exportcsv :as exportcsv]
              [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn filter-by-branch [results branch]
  (filter (fn [res] ;;only return branches we care about
    (= (:ontology-branch res) branch)) results))


(defn get-ids [organism]
  (let [results (re-frame/subscribe [:multi-mine-results])
        active-filter (re-frame/subscribe [:active-filter])
        this-resultset (organism @results)]
    (filter number? ;excludes n/a and blank string results
      (distinct (reduce (fn [new-vec result]
        (conj new-vec (:ortho-db-id result)
          ;add original genes if this is a human query.
          (cond (= organism :human)
            (:original-db-id result)))
) [] this-resultset)))))

(defn sort-by-pval [server-response]
  (let [results (:results server-response)]
    (assoc server-response :results (sort-by :p-value results))))

(defn enrich [db]
  (let [organisms (:organisms db)
        max-p (:max-p db)
        filter (:active-filter db)
        test-correction (:test-correction db)]
    (doall (map (fn [[id organism]]
      (let [ids (get-ids (:id organism))]
        (cond
          (and (:output? organism) (> (count ids) 1))
            (go (let [res (<! (comms/enrichment
              (select-keys (:mine organism) [:service])
              {:widget "go_enrichment_for_gene"
               :maxp max-p
               :format "json"
               :filter filter
               :correction test-correction
               :ids ids}))]
                 (re-frame/dispatch [:concat-enrichment-results (sort-by-pval res) id])))
          (= (count ids) 0)
            (re-frame/dispatch [:concat-enrichment-results {:error "There were no orthologues for this organism"} id])
          (= (count ids) 1)
            (re-frame/dispatch [:concat-enrichment-results {:error "More than one orthologue per organism is required in order to enrich a list. Try searching for multiple genes. "} id])
          ))) organisms))
  ))

(defn refresh-enrichment-statuses [db]
  "sets all enrichments to either loading or blank. use when a new search is initiated"
  (reduce (fn [new-map [id organism]]
    (cond (:output? organism)
      (assoc-in new-map [id :loading] true))
) (:enrichment db) (:organisms db)))

(re-frame/register-handler
 :enrich-results
 (fn [db [_ _]]
  ; (.clear js/console)
   (enrich db)
   (assoc db :enrichment (refresh-enrichment-statuses db))))

 (re-frame/register-handler
  :concat-enrichment-results
  (fn [db [_ results organism]]
    (assoc-in db [:enrichment organism] results)))

 (re-frame/register-handler
  :test-correction
  (fn [db [_ correction-value]]
    (assoc db :test-correction correction-value)))

(re-frame/register-handler
 :max-p
 (fn [db [_ max-p-value]]
   (assoc db :max-p max-p-value)))

   (defn result-to-csv-rows "helper for csv-body. converts a single organism's results to a set of csv rows" [organism results]
       (if results
         ;;return each result formatted nicely if there are results
          (reduce (fn [new-str result]
            (str
              new-str (clojure.string/join exportcsv/export-token
                [organism
                (:matches result)
                (:identifier result)
                (:description result)
                (:p-value result)]) "\n" )
         ) "" results)
         ;;this means we have no results....
         "")
     )

   (defn csv-body
     "output graph as summary of all enrichments across all organisms."
     [enrichment]
     (let [headers (clojure.string/join exportcsv/export-token ["Organism""Matches""GO ID""GO Term""P-Value\n"])]
       (str headers
         (reduce (fn [new-str [id organism]]
           (str new-str (result-to-csv-rows (utils/get-abbrev id) (:results organism)))
         ) "" enrichment)
     )))

(re-frame/register-handler
  :download-enrichment
  (fn [db [_ _]]
    (.open js/window (exportcsv/encode-data (csv-body (:enrichment db))))
    db))
