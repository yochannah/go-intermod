(ns gointermod.orthologresults.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.exportcsv :as exportcsv]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

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
  [:th.count.bio "Biological Process"]
  [:th.count.molecular "Molecular Function"]
  [:th.count.cellular "Cellular Component"]
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
              [:td.bio (get ortholog-details "biological_process" 0)]
              [:td.molecular (get ortholog-details "molecular_function" 0)]
              [:td.cellular (get ortholog-details "cellular_component" 0)]
              [:td.dataset (clojure.string/join ", " (:dataset ortholog-details))]
            ]) organism-details))
        ) @results))]))

(defn csv-body
  "returns results of the graph in csv string format for download"
  []
  (let [results (re-frame/subscribe [:aggregate-results])]
    (reduce (fn [outer-str [organism organism-details] organisms]
      (str outer-str
        (reduce (fn [inner-str [ortholog ortholog-details] organism-details]
          (str inner-str
               (utils/get-abbrev organism) ","
               (:original-id ortholog-details) ","
               (clj->js ortholog) ","
               (get ortholog-details "biological_process" 0) ","
               (get ortholog-details "molecular_function" 0) ","
               (get ortholog-details "cellular_component" 0) ","
               "\""
               (clojure.string/join ", " (:dataset ortholog-details))
               "\"" "\n")
        ) "" organism-details))
    ) "" @results)
))


(defn orthologs []
  (fn []
     [:div.ortholog-results
     (let [are-there-results? (re-frame/subscribe [:aggregate-results])]
      (if @are-there-results?
        ;;if there are results:
        [:div
          [:h2 "Orthologous Genes"] [exportcsv/download-button (csv-body)]
          [:table.aggregate
            [aggregate-headers]
            [aggregate-results]]]
))]))
