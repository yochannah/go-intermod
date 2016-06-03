(ns gointermod.ontology.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.utils.comms :as comms]))

(defn sort-by-parent-count
  "Nuff said, really."
  [resultset]
  (sort-by #(count (:parents %)) resultset))

(defn get-keys-for-tree
  "Helper for make-organism-tree."
  [parents]

  (reduce (fn [new-vec result]
    (conj new-vec (:name result))
) [] (sort-by-parent-count parents)))

(defn get-orthologs-for-term
  "Helper for make-organism-tree. gets the orthologues assocated with a given GO term."
  [go-term organism]

  (let [unfiltered-results (re-frame/subscribe [:multi-mine-results])]
    (filterv (fn [result]
        (= (:go-term result) go-term)
) (organism @unfiltered-results))))

(defn make-organism-tree
  "Makes the parent/child tree for a single organism from flat intermine results"
  [results organism]

  (reduce (fn [new-map result]
    (let [keys (get-keys-for-tree (:parents result))]
      (assoc-in new-map (conj keys :results organism)
          (get-orthologs-for-term (last keys) organism)))
) {} (sort-by-parent-count results)))

(defn deep-merge
  "Recursively merge a nested map."
  [& maps]

  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (println "Something unexpected occurred" maps)
))

(defn make-tree
  "create map structure for each organism then recursively merge them into one."
  [flat-terms]
  (apply deep-merge (vals (reduce (fn [new-map [organism results]]
    (assoc new-map organism
      (make-organism-tree (:results results) organism))
) {} flat-terms))))

(re-frame/register-handler
;;handler for making the flat results returned by intermine into a tree structure for the graph rendering.
 :go-ontology-tree
 (fn [db [_ _]]
   (let [tree (make-tree (:flat (:go-ontology db)))]
(assoc-in db [:go-ontology :tree] tree))))

(defn get-go-terms
  "gets go terms for a single organism. Helper for get-all-go-terms"
  [organism db]
  (let [these-results (organism (:multi-mine-results db))]
    (reduce (fn [x y]
      (conj (set x) (:go-id y))
    ) [] these-results)
    ))

(defn get-all-go-terms
  "gets go terms for all organisms that have results."
  [db]
  (assoc db :go-terms
    (reduce (fn [new-map [id organism]]
      (assoc new-map id (get-go-terms id db))
) {} (:multi-mine-results db))))


(re-frame/register-handler
 ;;Grabs all the GO terms from the search and queries for their parents.
 ;;As eachresult is returned, it dispatches concat-ontology-results below.
 :load-go-graph
 (fn [db [_ _]]
    (let [go-termed-db (get-all-go-terms db)]
      (comms/ontology-query-all-organisms (:go-terms go-termed-db))
 go-termed-db)))

(re-frame/register-handler
;; Adds asynchronously fetched results to the DB, and dispatches events to generate the count of nodes and to generate the graph data shape (tree).
 :concat-ontology-results
  (fn [db [_ results organism]]
    (let [flat (assoc-in db [:go-ontology :flat organism] results)]
      (re-frame/dispatch [:go-ontology-tree])
      (re-frame/dispatch [:go-ontology-nodecount])
flat)))

(defn nodelist
"Flatten out that massive node graph so that we can count the individual
 unique terms. This is because we don't want to render graphs with too many terms - they become spaghetti monsters. The actual (n) value for which we don't render is set directly in the view."
  [nodes]

  (set (flatten (reduce (fn [new-set [name vals]]
    (if (= name :results)
      new-set
      (concat new-set [name] (nodelist vals)))
) #{} nodes))))


(re-frame/register-handler
 ;;handler for the nodelist function above.
  :go-ontology-nodecount
  (fn [db [_ _]]
    (let [tree (get-in db [:go-ontology :tree])
          nodelist (nodelist tree)]
      (assoc-in db [:go-ontology :nodes] (count nodelist))
)))
