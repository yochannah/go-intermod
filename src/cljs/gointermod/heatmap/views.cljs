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
  ;;subscribe to a branch
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])]
  ;;output tds of counts
  [:tbody
   (.log js/console "HEATMAP" (clj->js (:rows @heatmap)))
    (map (fn [result]
        ^{:key (str (first result) (second result))}
       [:tr
        (map
         (fn [val]
           ^{:key (gensym)}
           [:td val]) result)
        ]) (:rows @heatmap))
   ]))

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
