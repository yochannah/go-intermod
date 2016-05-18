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
  [:th
    [:input
     {:type "checkbox"
      :checked @(re-frame/subscribe [:are-all-orthologs-selected?])
      :on-change #(re-frame/dispatch [:toggle-select-all])}]]
  [:th "Species"]
  [:th "Input Gene"]
  [:th "Orthologs"]
  [:th.count "Biological Process"]
  [:th.count "Molecular Function"]
  [:th.count "Cellular Component"]
  [:th "Source"]
  ]])

(defn aggregate-results []
  "output aggregated search results into table rows"
  (let [results (re-frame/subscribe [:aggregate-results])]
    [:tbody
    (doall (map (fn [[organism organism-details] organisms]
      (doall  (map (fn [[ortholog ortholog-details] organism-details]
           ^{:key (gensym)}
           [:tr {:class (clj->js organism)}
            [:td
              [:input
               {:type "checkbox"
                :checked (:is-selected? ortholog-details)
                :on-change #(re-frame/dispatch [:select-ortholog-result organism ortholog])}]]
              [:td.organism (utils/get-abbrev organism)]
              [:td (:original-id ortholog-details)]
              [:td (clj->js ortholog)]
              [:td (get ortholog-details "biological_process" 0)]
              [:td (get ortholog-details "molecular_function" 0)]
              [:td (get ortholog-details "cellular_component" 0)]
              [:td.dataset (clojure.string/join ", " (:dataset ortholog-details))]
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
     (let [are-there-results? (re-frame/subscribe [:aggregate-results])]
      (if @are-there-results?
        ;;if there are results:
        (do [:h2 "Orthologous Genes"]
        [:table.aggregate
          [aggregate-headers]
          [aggregate-results]])
        ;;Placeholder for non-results
        [:div "type something into the searchbar up the top and press search"]
))]))
