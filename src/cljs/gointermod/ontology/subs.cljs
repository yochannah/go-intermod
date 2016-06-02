(ns gointermod.ontology.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :go-ontology-flat
 (fn [db]
   (reaction (:flat (:go-ontology @db)))))

 (re-frame/register-sub
  :go-ontology-tree
  (fn [db]
    (reaction (:tree (:go-ontology @db)))))

(re-frame/register-sub
 :go-ontology-nodelist
 (fn [db]
   (reaction (:nodes (:go-ontology @db)))))
