(ns gointermod.orthologresults.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn headers []
  [:thead [:tr
  [:th "Species"]
  [:th "Orthologs"]
  [:th "GO identifier thingy"]
  [:th "Term"]
  [:th "Branch"]
   ]])

 (defn result-row [[original-symbol original-secondary-id original-id _ _  homie-id homie-secondary-id homie-symbol homie-organism _ data-set  _ pub-id _ _ go-identifier ontology-term ontology-branch]]
   ^{:key (gensym)}
   [:tr
   [:td homie-organism]
   [:td homie-symbol]
   [:td go-identifier]
   [:td ontology-term]
   [:td ontology-branch]
    ])

(defn results []
  "output search results into table rows"
  (let [search-results (re-frame/subscribe [:search-results])]
  [:tbody (map result-row (:results @search-results))]))

(defn count-by-ontology-branch [branch]
  (let [search-results (:results @(re-frame/subscribe [:search-results]))]
  (count (filter
   (fn [result] (= (last result) branch)) search-results))
))

(defn aggregate-headers []
  [:thead [:tr
  [:th "Include"]
  [:th "Species"]
  [:th "Orthologs"]
  [:th.count "Biological Process"]
  [:th.count "Molecular Function"]
  [:th.count "Cellular Component"]
  ]])

(defn aggregate-results []
  "output aggregated search results into table rows"
  (let [results (re-frame/subscribe [:aggregate-results])]
    [:tbody
    (doall (map (fn [[organism organism-details] organisms]
      (doall  (map (fn [[ortholog counts] organism-details]
           ^{:key (gensym)}
           [:tr {:class (clj->js organism)}
            [:td [:input {:type "checkbox"}]]
            [:td  (comms/get-abbrev organism)]
            [:td (clj->js ortholog)]
              [:td (:biological_process counts)]
              [:td (:molecular_function counts)]
              [:td (:cellular_component counts)]
            ]) organism-details))
        ) @results))]))

(defn resolve-ids []
  (go
    (let [search-term (re-frame/subscribe [:search-term])
          input-organism (re-frame/subscribe [:input-organism])
          ids (<! (comms/resolve-id @input-organism @search-term))]
      )
  ))


(defn orthologs []
  (fn []
     [:div.ortholog-results
      [:h2 "Orthologous Genes"]
      [:table.aggregate
        [aggregate-headers]
        [aggregate-results]]
      [:table
        [headers]
        [results]
        ]]))
