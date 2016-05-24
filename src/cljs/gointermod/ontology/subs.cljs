(ns gointermod.ontology.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(defn sort-by-parent-count [resultset]
  (sort-by #(count (:parents %)) resultset)
  )

(defn get-keys-for-tree [parents]
  (reduce (fn [new-vec x ]

  ;  (.log js/console "excks" (clj->js x))
           (conj new-vec
             (:name x) )
)  [] (sort-by-parent-count parents)))

(defn make-tree [flat-terms organism]
  (let [
        results (sort-by-parent-count (:results (organism flat-terms)))
        ]
   (reduce (fn [new-map result]
     (assoc-in new-map (conj (get-keys-for-tree (:parents result)) organism) true)
           ) {} results)
))

(re-frame/register-sub
 :go-ontology-tree
 (fn [db]
   (reaction (make-tree (:flat (:go-ontology @db)) :mouse))))

(re-frame/register-sub
 :go-ontology-flat
 (fn [db]
   (reaction (:flat (:go-ontology @db)))))
