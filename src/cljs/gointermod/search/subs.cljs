(ns gointermod.search.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :evidence-codes
 (fn [db]
   (reaction (:evidence-codes @db))))

(defn get-active-evidence-codes [evidence-codes]
  "returns codes which are checked in the search box"
  (reduce (fn [new-vec code]
    (if (:checked code)
      ;;it's checked, so add to vector & return it
      (conj new-vec (:code code))
      ;;not checked, jsut return the same vector
      new-vec
)) [] evidence-codes))


(re-frame/register-sub
  ;;only codes that are checked :)
  :active-evidence-codes
  (fn [db]
    (reaction (get-active-evidence-codes (:evidence-codes @db)))))

(re-frame/register-sub
  :expand-evidence-codes?
  (fn [db]
    (reaction (:expand-evidence-codes? @db))))

(re-frame/register-sub
  :initialised
  (fn [db]
    (reaction (:initialised @db))))


(re-frame/register-sub
  :active-modal
  (fn [db]
    (reaction (:active-modal @db))))

  (re-frame/register-sub
   :input-organism
   (fn [db]
     (reaction (:selected-organism @db))))

 (re-frame/register-sub
  :search-term
  (fn [db]
    (reaction (:search-term @db))))

(re-frame/register-sub
 :human-orthologs-of-other-input-organism
 (fn [db]
   (reaction (:human-orthologs-of-other-input-organism @db))))

(re-frame/register-sub
  :mapped-resolved-ids
  (fn [db]
    (reaction (:mapped-resolved-ids @db))))
