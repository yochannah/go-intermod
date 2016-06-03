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
  :go-terms
  (fn [db]
    (reaction (:go-terms @db))))


(re-frame/register-sub
 :go-ontology-nodecount
 (fn [db]
   (reaction (:nodes (:go-ontology @db)))))

(re-frame/register-sub
 :go-ontology-status
 (fn [db]
   (reaction (:status (:go-ontology @db)))))

(re-frame/register-sub
  :go-ontology-loading
  (fn [db]
    (reaction (:loading (:go-ontology @db)))))
