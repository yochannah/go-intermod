(ns gointermod.enrichment.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn output-success [this-response]
  "This is the bog standard enrichment widget results we know and love."
  [:table [:thead [:tr
      [:th.matches "Matches"]
      [:th.description "GO Term"]
      [:th.pval "P-value"]]]
    [:tbody
      (map (fn [result]
        ^{:key (str (:p-value result) (:identifier result))}
        [:tr
         [:td.matches (:matches result)]
         [:td.description (:description result)]
         [:td.pval (:p-value result)]]) (:results this-response))]
    ])

(defn output-error [result]
  "Error. Error. Does not compute."
 [:div.error [:svg.icon [:use {:xlinkHref "#icon-sad"}]] (:error result) ])

(defn process-response [organism this-response]
  "When there's been a response, we need to either tell the user there was an error or just output the data"
  ^{:key (:id organism)}
  [:div.organism
    {:class (str (clj->js (:id organism)) " " (cond (:error this-response) "error"))} [:h3 (:abbrev organism)]
      (cond
        (:error this-response)
          [output-error this-response]
        (:wasSuccessful this-response)
          [output-success this-response]
         )
   ]
  )

(defn organism-enrichment []
  "One enrichment results box per organism, outputting the error or the enrichment results"
  (let [organisms (re-frame/subscribe [:organisms])
        enrichment (re-frame/subscribe [:enrichment-results])]
    [:div.organisms
      (doall (map (fn [[id organism]]
          ^{:key (str "enrich" organism)}
          [process-response (id @organisms) (id @enrichment)]
      ) @enrichment)
   )]))

(defn test-correction-filter []
  "Visual component to allow users to select a test correction value"
  (let [correction (re-frame/subscribe [:test-correction])]
  [:div [:label "Test correction"
      [:select
        {:on-change (fn [e]
            (re-frame/dispatch [:test-correction (aget e "target" "value")])
            (re-frame/dispatch [:enrich-results]))
          :value @correction}
          [:option {:value "Holm-Bonferroni"} "Holm-Bonferroni"]
          [:option {:value "Benjamini Hochberg"} "Benjamini Hochberg"]
          [:option {:value "Bonferroni"} "Bonferroni"]
          [:option {:value "None"} "None"]]]
]))

(defn ontology-filter []
  "interactive dropedown to select the ontology (biological process, molecular function, or cellular component. Stays in sync with the fliter on the left."
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
  "UI component to allow the user to select a p-value"
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

(defn debugbutton []
  [:button
   {:on-click
    (fn []
      (re-frame/dispatch [:enrich-results])
                        )} "I would like some enrichment, pls!"])

(defn enrichment []
  (let [max-p (re-frame/subscribe [:max-p])]
    (re-frame/dispatch [:enrich-results])
   [:div.enrichment
    [:h2 "Enrichment"]
    [:div.settings
      [test-correction-filter]
      [max-p-filter]
      [ontology-filter] ]
    [organism-enrichment]
    ])
)
