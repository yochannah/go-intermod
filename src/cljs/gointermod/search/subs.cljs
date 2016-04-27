(ns gointermod.search.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :evidence-codes
 (fn [db]
   (reaction (:evidence-codes @db))))

(re-frame/register-sub
  :expand-evidence-codes?
  (fn [db]
    (reaction (:expand-evidence-codes? @db))))

  (re-frame/register-sub
   :input-organism
   (fn [db]
     (reaction (:selected-organism @db))))

 (re-frame/register-sub
  :search-term
  (fn [db]
    (reaction (:search-term @db))))
