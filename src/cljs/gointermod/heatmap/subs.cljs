(ns gointermod.heatmap.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :heatmap-aggregate
 (fn [db]
  (reaction (:heatmap @db))))

(re-frame/register-sub
 :heatmap-aggregate-csv
 (fn [db]
  (reaction (:heatmap-csv @db))))


(re-frame/register-sub
 :all-results
 (fn [db]
  (reaction (:all-results (:heatmap @db)))))


(defn count-orthologs [results]
  (reduce (fn [new-map [organism result]] (assoc new-map organism (count result))) {} results))

(re-frame/register-sub
 :ortholog-count
 (fn [db]
   (reaction (count-orthologs (:multi-mine-aggregate @db)))
))
