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

(defn aggregate-headers []
  [:thead [:tr
  [:th [:input {:type "checkbox" :on-click #(re-frame/dispatch [:toggle-select-all])}]]
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
      (doall  (map (fn [[ortholog ortholog-details] organism-details]
           ^{:key (gensym)}
           [:tr {:class (clj->js organism)}
            [:td [:input
                  {:type "checkbox"
                   :checked (:is-selected? ortholog-details)
                   :on-change #(re-frame/dispatch [:select-ortholog-result organism ortholog])}]]
            [:td  (comms/get-abbrev organism)]
            [:td (clj->js ortholog)]
              [:td (:biological_process ortholog-details)]
              [:td (:molecular_function ortholog-details)]
              [:td (:cellular_component ortholog-details)]
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
        ]))
