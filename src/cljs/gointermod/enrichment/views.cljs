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

(defn enrichment []
  (fn []
    (re-frame/dispatch [:enrich-results])
   [:div.enrichment
    [:h2 "Enrichment"]
    [:div.settings
      [:div [:label "Test correction"
        [:select
          [:option "Holm-Bonferroni"]]]]
          [:div [:label "Max p-value"
        [:select
          [:option "0.05"]]]]
      ; [:label "Ontology"
      ;   [:select
      ;    (let [filters (re-frame/subscribe [:filters])]
      ;     (map (fn [[k v]]
      ;       [:option {:value k}  (:pretty-name v)])
      ;      @filters))
      ;   ]]
        [:div
          [:span "Ontology branch: " @(re-frame/subscribe [:active-filter-pretty])]]
     ]
    [organism-enrichment]
    ]
))
