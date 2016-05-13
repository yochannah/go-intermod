(ns gointermod.enrichment.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :max-p
 (fn [db]
  (reaction (:max-p @db))))


(re-frame/register-sub
 :test-correction
 (fn [db]
  (reaction (:test-correction @db))))

(re-frame/register-sub
 :enrichment-results
 (fn [db]
  (reaction (:enrichment @db))))
