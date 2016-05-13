(ns gointermod.enrichment.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn organism-enrichment []
  (let [organisms (re-frame/subscribe [:organisms])]
  [:div.organisms
   (map (fn [[_ organism]]
          ^{:key (:id organism)}
          [:div.organism [:h4 (:abbrev organism)]
           ]) @organisms)
   ]))

(defn test-correction-filter []
  (let [correction (re-frame/subscribe [:test-correction])]
  [:div [:label "Test correction"
      [:select
        {:on-change (fn [e]
            (re-frame/dispatch [:test-correction (aget e "target" "value")])
            (re-frame/dispatch [:enrich-results]))
          :value @correction}
          [:option {:value "Holms-Bonferroni"} "Holms-Bonferroni"]
          [:option {:value "Benjamini Hochberg"} "Benjamini Hochberg"]
          [:option {:value "Bonferroni"} "Bonferroni"]
          [:option {:value "None"} "None"]]]
]))

(defn ontology-filter []
  (let [filters (re-frame/subscribe [:filters])
        active-filter (re-frame/subscribe [:active-filter])]
  [:div [:label "Ontology"
     [:select
       {:on-change (fn [e]
          (re-frame/dispatch [:active-filter (aget e "target" "value")]))
        :value @active-filter}
       (map (fn [[k v]]
          ^{:key (str "filter" k)}
          [:option
           {:value k} (:pretty-name v) ])
        @filters)
]]]))

(defn max-p-filter []
  (let [max-p (re-frame/subscribe [:max-p])]
    [:div [:label "Max p-value"
      [:select
       {:value @max-p
        :on-change (fn [e]
          (re-frame/dispatch [:max-p (aget e "target" "value")]))}
        [:option {:value 0.05}"0.05"]
        [:option {:value 0.10} "0.10"]
        [:option {:value 1.00} "1.00"]]]]
  ))

(defn enrichment []
  (let [max-p (re-frame/subscribe [:max-p])]
   [:div.enrichment
    [:h2 "Enrichment"]
    [:div.settings
      [test-correction-filter]
      [max-p-filter]
      [ontology-filter]
     ]
    [organism-enrichment]
    ])
)
