(ns gointermod.orthologresults.handlers
    (:require [re-frame.core :as re-frame]
              [clojure.walk :as walk]
              [gointermod.db :as db]))

(defn set-all-aggregate-results-to [the-boolean db]
  "updates individual results in the db to 'the-boolean' as well as updating a master are-all-selected record. The master is used for the select-all checkbox."
  (-> (update-in db [:multi-mine-aggregate] (fn [m]
    (walk/postwalk (fn [x]
      (if (and (map? x) (contains? x :is-selected?))
        (assoc x :is-selected? the-boolean)
        x)) m)))
    (assoc :are-all-orthologs-selected? the-boolean)
  ))

(defn are-all-selected? [results]
  "helper method to determine whether or nor all the results in a multi result set are selected. Returns a single boolean value"
  ;;I bet this can be refactored to be simpler.
  (let [all-selected? (atom [])]
    (doall (map (fn [[organism org-details] x]
      (doall (map (fn [[ortholog orth-details] y]
          (swap! all-selected? conj (:is-selected? orth-details))
      ) org-details))
    ) results))
  (reduce (fn [a b] (and a b)) @all-selected?)
))

(re-frame/register-handler
  :toggle-select-all
;  "Selects all if any are deselected, or deselects if all are selected"
  (fn [db [_ _]]
    (if (are-all-selected? (:multi-mine-aggregate db))
      (set-all-aggregate-results-to false db)
      (set-all-aggregate-results-to true db))
    ))

(re-frame/register-handler
  :select-ortholog-result
  (fn [db [_ organism ortholog]]
;    "Selects or deselects a single result in the db"
    (update-in db [:multi-mine-aggregate organism ortholog :is-selected?] not)
    ))
