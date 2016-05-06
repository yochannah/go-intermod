(ns gointermod.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
      [gointermod.orthologresults.subs :as orthologs]
      [gointermod.heatmap.subs]
      [gointermod.search.subs :as search]))

(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

 (re-frame/register-sub
  :db
  (fn [db]
    (reaction @db)))

(re-frame/register-sub
 :organisms
 (fn [db]
  (reaction (:organisms @db))))

(re-frame/register-sub
 :active-view
 (fn [db]
   (reaction (:active-view @db))))

(re-frame/register-sub
 :active-filter
 (fn [db]
  (reaction (:active-filter @db))))

  (re-frame/register-sub
   :filters
   (fn [db]
    (reaction (:filters @db))))
