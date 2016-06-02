(ns gointermod.ontology.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.utils.comms :as comms]))

(defn get-go-terms [organism db]
  (let [these-results (organism (:multi-mine-results db))]
    (reduce (fn [x y]
      (conj (set x) (:go-id y))
    ) [] these-results)
    ))

(defn get-all-go-terms [db]
  ;;gets go terms for all organisms that have results.
  (assoc db :go-terms
    (reduce (fn [new-map [id organism]]
      (assoc new-map id (get-go-terms id db))
) {} (:multi-mine-results db))))


(re-frame/register-handler
 :load-go-graph
 (fn [db [_ _]]
;   (.clear js/console)
   (let [go-termed-db (get-all-go-terms db)]
   (comms/ontology-query-all-organisms (:go-terms db))
   go-termed-db)))

(re-frame/register-handler
  :concat-ontology-results
  (fn [db [_ results organism]]
;    (.log js/console "GO" (clj->js results))
  (assoc-in db [:go-ontology :flat organism] results)))

  (defn sort-by-parent-count [resultset]
    (sort-by #(count (:parents %)) resultset)
    )

  (defn get-keys-for-tree [parents]
    (reduce (fn [new-vec result]
             (conj new-vec
               (:name result))
  )  [] (sort-by-parent-count parents)))

(defn get-orthologs-for-term [go-term organism]
    (let [unfiltered-results (re-frame/subscribe [:multi-mine-results])]
    (filterv (fn [result]
        (= (:go-term result) go-term)
      ) (organism @unfiltered-results))
    ))

  (defn make-organism-tree [results organism]
    (reduce (fn [new-map result]
      (let [keys (get-keys-for-tree (:parents result))]
        (assoc-in new-map (conj keys :results organism)
            (get-orthologs-for-term (last keys) organism)))
  ) {} (sort-by-parent-count results)))

  (defn deep-merge [& maps]
    (if (every? map? maps)
      (apply merge-with deep-merge maps)
      (println "Something unexpected occurred" maps)
      )
    )


(defn make-tree [flat-terms]
  (apply deep-merge (vals (reduce (fn [new-map [organism results]]
    (assoc new-map organism
      (make-organism-tree (:results results) organism))
  ) {} flat-terms)
  )))


  (re-frame/register-handler
   :go-ontology-tree
   (fn [db [_ _]]
     (let [tree (make-tree (:flat (:go-ontology db)))]
     (assoc-in db [:go-ontology :tree] tree))))

 (defn flatten-content [node]
   (lazy-seq
     (if (string? node)
       (list node)
       (mapcat flatten-content (:content node)))))


  (defn nodelist [nodes]
    (set (flatten (reduce (fn [new-set [name vals]]
      (if (= name :results)
        new-set
        (concat new-set [name] (nodelist vals)))
  ) #{} nodes))))


  (re-frame/register-handler
    :go-ontology-nodelist
    (fn [db [_ _]]
      (let [tree (get-in db [:go-ontology :tree])
            nodelist (nodelist tree)]
        (assoc-in db [:go-ontology :nodes] nodelist)
        )))
