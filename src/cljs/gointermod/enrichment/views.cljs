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
          [:option "Holms-Bonferroni"]
          [:option "Benjamini Hochberg"]
          [:option "Bonferroni"]
          [:option "None"]]]]
      [:div [:label "Max p-value"
        [:select
          [:option 0.05]
          [:option 0.10]
          [:option 1.00]]]]
      [:div [:label "Ontology"
      (let [filters (re-frame/subscribe [:filters])
      active-filter (re-frame/subscribe [:active-filter])]
         [:select
          {:on-change (fn [e] (re-frame/dispatch [:active-filter (aget e "target" "value")]))
           :value @active-filter}
           (map (fn [[k v]]
              ^{:key (str "filter" k)}
              [:option
               {:value k} (:pretty-name v) ])
            @filters)
         ])]]
     ]
    [organism-enrichment]
    ]
))
