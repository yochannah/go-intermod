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
  (let [headers (get-headers)
        active-filter (re-frame/subscribe [:active-filter])
        filters (re-frame/subscribe [:filters])
        filter-info (get @filters @active-filter)]
  [:thead
   [:tr
    [:th.axis {:col-span 2}
      [:div
        [:h3
          [:svg.icon [:use {:xlinkHref (:icon filter-info)}]] " "
          (:pretty-name filter-info) ":"]]
     [:div
      [:div.faux-th.species "Species"]
      [:div.faux-th.ortholog "Ortholog"]
     ]]
    (map (fn [header]
      ^{:key header}
      [:th.goterm [:div [:span header]]]) headers)
   ]]
  ))

(defn calc-color [color-val]
  "Given a count value, this function returns an rgb value weighted to make higher counts darker, with the darkest colours being the maximum value found in the table.
  Results are weighted proportionally - so values of 1 will be quite dark if the highest value in the table is two, or quite light if the highest value is 30."
  (if (= 0 color-val)
    ;;if there's no color val, return white
    {:background "rgb(255,255,255)"}
    ;;if there is, calculate how dark the colour we want is
    (let [heatmap (re-frame/subscribe [:heatmap-aggregate])
          max-val (last (:max-count @heatmap))
          calculated-color (int (/ (* color-val 255) max-val))
          bg-color (- 255 calculated-color)
          mid-color (int (/ (+ 255 bg-color) 2))]
    {:background (str "rgb(" bg-color "," mid-color "," "255)")}
)))

(defn organism-orthologue-tds [result]
  (let [organism-name (first result)
        organism-id (utils/organism-name-to-id organism-name)
        orthologue-counts (re-frame/subscribe [:ortholog-count])
        org-count (organism-id @orthologue-counts)]
  [:tr {:class organism-id}
    [:td organism-name]
   (if (number? (second result))
    [:td
      (if (= org-count 1)
        (second result)
        [:span {:on-click #(re-frame/dispatch [:expand-heatmap organism-id])} org-count " genes " [:svg.icon [:use {:xlinkHref "#icon-circle-right"}]]])]
    [:td (second result)]
     )]))

(defn counts []
  ;;subscribe to the heatmap data
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])]
  ;;output tr, one per organism,ortholog combo
    (into [:tbody]
      (map (fn [result]
        (into (organism-orthologue-tds result)
          (map (fn [val]
             ;;one td per go term
             [:td {:style (calc-color val)} val]) (drop 2 result)))
      )) (:rows @heatmap))
))

(defn empty-rows []
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])
        empties (:missing-organisms @heatmap)
        cols (count (:headers @heatmap))]
    (into [:tbody]
      (map (fn [organism]
        [:tr {:class (utils/organism-name-to-id organism)}
          [:td organism]
          [:td.no-orthologs {:col-span 3} "No orthologs available"]
          [:td.no-go-terms {:col-span (- cols 2)} "N/A"]
]) empties))))

(defn heatmap []
  (re-frame/dispatch [:trigger-data-handler-for-active-view])
    [:div.heatmap
      [:h2 "Annotation count by species"]
        [:table
          [headers]
          [counts]
          [empty-rows]
]])
