(ns gointermod.heatmap.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.search.handlers :as search]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn get-headers []
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])]
    (:headers @heatmap)))

(defn headers []
  ;;subscribe to aggregate results for a given branch
  ;;each term is in a th
  (let [headers (get-headers)]
  [:thead
   [:tr
    [:th "Species"]
    [:th "Orthologue"]
    (map (fn [header]
      ^{:key header}
      [:th.goterm [:div [:span header]]]) headers)
   ]]
  ))

(defn counts []
  ;;subscribe to the heatmap data
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])]
  ;;output tr, one per organism,ortholog combo
  [:tbody
    (map (fn [result]
        ^{:key (str (first result) (second result))}
       [:tr
        (map (fn [val]
           ;;one td per go term
           ^{:key (gensym)}
           [:td val]) result)
        ]) (:rows @heatmap))
   ]))

(defn heatmap []
  (fn []
    (re-frame/dispatch [:aggregate-heatmap-results])
     [:div.heatmap
      [:h2 "Annotation count by species"]
        [:table
        [headers]
        [counts]
       ]
]))
