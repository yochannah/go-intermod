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

(defn get-all-go-terms-by-organism
  "gets go terms for all organisms that have results."
  [db]
  (assoc db :go-terms
    (reduce (fn [new-map [id organism]]
      (assoc new-map id (get-go-terms id db))
) {} (:multi-mine-results db))))

(defn get-count-of-go-terms-by-filter
  "Returns the count of terms represented by the active ontology."
  [db]
  (let [filter-ontology (:active-filter db)
        results (apply concat (vals (:multi-mine-results db)))]
  (count (filter (fn [result] (= (:ontology-branch result) filter-ontology)) results))))


(defn loading-state
  "sets all organisms go results to loading."
  []
  (let [organisms (re-frame/subscribe [:organisms])
        go-terms (re-frame/subscribe [:go-terms])]
    (reduce (fn [new-map [organism vals]]
      ( if (and (:output? vals) (seq (organism @go-terms)))
        (assoc new-map organism "loading")
        new-map
      )
    ) {} @organisms)
))

(defn count-go-terms
  "searching for hundreds of GO terms can be quite slow, and since we don't actually want to search for results we *know* will be more than we will ever display, we count results before we dispatch the query"
  [go-terms]
(reduce (fn [totalcount val] (+ totalcount (count val))) 0 (vals go-terms)))

(defn query-for-go-tree
  "Grabs all the GO terms from the search and queries for their parents.
  As each result is returned, it dispatches concat-ontology-results. It laso handles resetting statuses of the organisms / page to loading."
  [db]
  (comms/ontology-query-all-organisms (:go-terms db))
  (->
      (assoc-in db [:go-ontology :nodes] 0)
      (assoc-in [:go-ontology :loading] true)
      (assoc-in [:go-ontology :status] (loading-state)))
)

(defn handle-large-result
  "This is the counterpart of query-for-go-tree, for when there are simply too many results for it to be sane to return results"
  [db go-term-count-for-filter]
  (->
      (assoc-in db [:go-ontology :nodes]
        (str go-term-count-for-filter " or more "))
      (assoc-in [:go-ontology :loading] false)))

(re-frame/register-handler
 :load-go-graph
 ;;decides if there're too many terms to search for or not, and either searches or tells the users sorry, too big.
 (fn [db [_ _]]
    (let [go-terms-by-filter (get-count-of-go-terms-by-filter db)
          go-termed-db (get-all-go-terms-by-organism db)]
      (if (< go-terms-by-filter (:ontology-graph-max-limit db))
        (query-for-go-tree go-termed-db)
        (handle-large-result go-termed-db go-terms-by-filter)
      )
)))

(re-frame/register-handler
;; Adds asynchronously fetched results to the DB, and dispatches events to generate the count of nodes and to generate the graph data shape (tree).
 :concat-ontology-results
  (fn [db [_ results organism]]
    (let [flat (assoc-in db [:go-ontology :flat organism] results)]

      (re-frame/dispatch [:go-ontology-tree])
      (re-frame/dispatch [:go-ontology-nodecount])

      (assoc-in flat [:go-ontology :status organism] "done")
)))

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

(re-frame/register-handler
 ;;set statuses to loading.
  :go-ontology-loading
  (fn [db [_ loading]]
    (assoc-in db [:go-ontology :loading] loading)
))
