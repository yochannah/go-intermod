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
   (.clear js/console)
   (let [go-termed-db (get-all-go-terms db)]
;     (.log js/console "GO" (clj->js go-termed-db))
   (comms/ontology-query-all-organisms (:go-terms db))
   go-termed-db)))
