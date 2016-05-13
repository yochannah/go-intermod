(ns gointermod.enrichment.handlers
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]
              [gointermod.utils.comms :as comms]
              [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn filter-by-branch [results branch]
  (filter (fn [res] ;;only return branches we care about
    (= (:ontology-branch res) branch)) results))


(defn get-ids [organism]
  (let [results (re-frame/subscribe [:multi-mine-results])
        active-filter (re-frame/subscribe [:active-filter])
        this-resultset (organism @results)
        filtered (filter-by-branch this-resultset @active-filter)]
    (reduce (fn [new-vec result]
      (conj new-vec (:ortho-db-id result) (:original-db-id result))
) [] filtered)))

(defn enrich [db]
  (let [organisms (re-frame/subscribe [:organisms])
        max-p (re-frame/subscribe [:max-p])
        test-correction (re-frame/subscribe [:test-correction])]
        ;(.clear js/console)
    (doall (map (fn [[id organism]]
      (let [ids (get-ids (:id organism))]
        (cond (and (:output? organism) (seq ids))
         (go (let [res (<! (comms/enrichment
            (select-keys (:mine organism) [:service])
            {:widget "go_enrichment_for_gene"
             :maxp @max-p
             :format "json"
             :correction @test-correction
             :ids ids}))]
               (re-frame/dispatch [:concat-enrichment-results res id])
        ))))
    ) @organisms))
  ))

(re-frame/register-handler
 :enrich-results
 (fn [db [_ _]]
   (enrich db)
 db))

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
