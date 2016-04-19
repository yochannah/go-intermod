(ns go-intermod.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
              [go-intermod.search.subs :as search]))

(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))


(re-frame/register-sub
 :organisms
 (fn [db]
  (reaction (:organisms @db))))
