(ns gointermod.orthologresults.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

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

  (re-frame/register-sub
    :are-all-orthologs-selected?
    (fn [db]
      (reaction (:are-all-orthologs-selected? @db))))
