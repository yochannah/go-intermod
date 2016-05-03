(ns gointermod.heatmap.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.search.handlers :as search]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn headers []
  ;;subscribe to aggregate results for a given branch
  ;;each term is in a th
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])]
  [:thead
   [:tr
    [:th "Species"]
    [:th "Orthologue"]
    [:th "All the terms go next"]
   ]])
  )

(defn counts []
  ;;subscribe to a branch
  ;;output tds of counts
  [:tbody
   [:tr
    [:td "Human"]
    [:td "Orthologue"]
    [:td "Counts here"]
]])

(defn heatmap []
  (fn []
     [:div.heatmap
      [:h2 "Annotation count by species"]
      [:button {:on-click #( (re-frame/dispatch [:aggregate-heatmap-results]))} "aggregate me, baby"]
      [:table
       [headers]
       [counts]
       ]
]))
