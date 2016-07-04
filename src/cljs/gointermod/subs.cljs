(ns gointermod.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
      [gointermod.orthologresults.subs :as orthologs]
      [gointermod.heatmap.subs]
      [gointermod.enrichment.subs]
      [gointermod.ontology.subs]
      [gointermod.search.subs :as search]))

 (re-frame/register-sub
  :db
  (fn [db]
    (reaction @db)))

(re-frame/register-sub
 :organisms
 (fn [db]
  (reaction (:organisms @db))))

(re-frame/register-sub
 :checked-organisms
 (fn [db]
  (reaction (filter (fn [[organism details]]
    (:output? details)) (:organisms @db)))))

(re-frame/register-sub
 :active-view
 (fn [db]
   (reaction (:active-view @db))))

(re-frame/register-sub
 :active-filter
 (fn [db]
  (reaction (:active-filter @db))))

(re-frame/register-sub
 :filters
 (fn [db]
  (reaction (:filters @db))))

(defn get-pretty-active-filter [db]
  (let [active-filter (:active-filter db)
        filters (:filters db)]
    (get-in filters [active-filter :pretty-name])))

(re-frame/register-sub
 :active-filter-pretty
 (fn [db]
  (reaction (get-pretty-active-filter @db))))

(defn count-annotations [resultvec]
  (reduce (fn [annotation-count [gene details]]
    (+ annotation-count
      (get details "biological_process")
      (get details "molecular_function")
      (get details "cellular_component"))
    ) 0 resultvec)
  )

(defn get-result-counts [db]
  (let [results (:multi-mine-aggregate db)]
    (reduce (fn [new-map [organism results]]
      (assoc new-map organism {:genes (count results)
                               :annotations (count-annotations results)})
    ) {} results)
))

(re-frame/register-sub
 :mine-result-counts
 (fn [db]
   (reaction (get-result-counts @db))))

(re-frame/register-sub
 :ontology-graph-max-limit
(fn [db]
  (reaction (:ontology-graph-max-limit @db))))
