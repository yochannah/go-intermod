(ns gointermod.orthologresults.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      ))

  (re-frame/register-sub
    :search-results
    (fn [db]
      (reaction (:search-results @db))))

  (re-frame/register-sub
    :multi-mine-results
    (fn [db]
      (reaction (:multi-mine-results @db))))

  (re-frame/register-sub
    :aggregate-results
    (fn [db]
      (reaction (:multi-mine-aggregate @db))))

  (defn lookup-original-input-identifier [id-map identifier]
    (let [input-identifier (utils/get-id (get id-map identifier)) ]
    (if input-identifier
      input-identifier
      identifier
  )))



  (re-frame/register-sub
    :input-gene-friendly-id
    (fn [db [_ identifier]]
      (reaction (lookup-original-input-identifier (:mapped-resolved-ids @db) identifier))))


  (re-frame/register-sub
    :are-all-orthologs-selected?
    (fn [db]
      (reaction (:are-all-orthologs-selected? @db))))
