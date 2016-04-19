(ns go-intermod.search.subs
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
