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

  (defn make-tree [flat-terms]
    (reduce (fn [new-map [organism results]]
      (cond (seq (:results results))
      (do (.log js/console "%cResult" "border-bottom:solid 3px hotpink" (clj->js organism)(clj->js (:results results)))
        (reduce (fn [x result]
          (assoc-in new-map (conj (get-keys-for-tree (:parents result)) organism) true)) (sort-by-parent-count (:results results))))
  )) {} flat-terms))

  (re-frame/register-handler
   :go-ontology-tree
   (fn [db [_ _]]
     (let [tr (make-tree (:flat (:go-ontology db)))]
       (.log js/console "%cTreetop" "border-bottom:solid 3px cornflowerblue" (clj->js tr))

     (assoc-in db [:go-ontology :tree] tr))))
