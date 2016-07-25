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
  [:th "Human Ortholog"]
  [:th "Orthologs"]
  [:th.count.bio "Biological Process"]
  [:th.count.molecular "Molecular Function"]
  [:th.count.cellular "Cellular Component"]
  [:th "Source"]
  ]])

(defn count-annotations [ortholog-details ontology-branch]
  (count (distinct (get ortholog-details ontology-branch))))

(defn aggregate-results []
  "output aggregated search results into table rows"
  (let [results (re-frame/subscribe [:aggregate-results])]
    (fn []
    (into [:tbody]
     (map (fn [[organism organism-details] organisms]
       (cond (seq organism-details)
          (doall (map (fn [[ortholog ortholog-details] organism-details]
            ;(.log js/console "%cortholog-details" "color:hotpink;font-weight:bold;" (clj->js ortholog-details))
            ^{:key (str organism ortholog (gensym))}
            [:tr {:class (clj->js organism)}
              [:td
                [:input
                  {:type "checkbox"
                  :checked (:is-selected? ortholog-details)
                  :on-change #(re-frame/dispatch [:select-ortholog-result organism ortholog])}]]

              [:td.organism (utils/get-abbrev organism)]
              [:td (:original-input-gene ortholog-details) ]
              ;[:td @(re-frame/subscribe [:input-gene-friendly-id (:original-id ortholog-details)]) ]
              [:td (:human-ortholog ortholog-details)]
              [:td (clj->js ortholog)]
              [:td.bio (count-annotations ortholog-details "biological_process")]
              [:td.molecular (count-annotations ortholog-details "molecular_function")]
              [:td.cellular (count-annotations ortholog-details "cellular_component")]
              [:td.dataset (clojure.string/join exportcsv/export-token (:dataset ortholog-details))]
            ]) organism-details)))
) @results)))))

(defn no-results []
  (let [results (re-frame/subscribe [:aggregate-results])]
    (into [:tbody]
      (map (fn [[organism organism-details] organisms]
        (cond (empty? organism-details)
          [:tr {:class (clj->js organism)}
            [:td][:td (utils/get-abbrev organism)]
            [:td {:col-span 7} "No known orthologs for the provided gene(s)."]]
)) @results))))

(defn csv-body
  "returns results of the graph in csv string format for download"
  []
  (let [results (re-frame/subscribe [:aggregate-results])
        headers (clojure.string/join exportcsv/export-token ["Organism" "Original Gene""Ortholog""Biological process""Molecular function""Cellular component""Datasources\n"])]
    (str headers
    (reduce (fn [outer-str [organism organism-details] organisms]
      (str outer-str
        (reduce (fn [inner-str [ortholog ortholog-details] organism-details]
          (str inner-str
               (utils/get-abbrev organism) exportcsv/export-token
               (:original-id ortholog-details) exportcsv/export-token
               (clj->js ortholog) exportcsv/export-token
               (count-annotations ortholog-details "biological_process") exportcsv/export-token
               (count-annotations ortholog-details "molecular_function") exportcsv/export-token
               (count-annotations ortholog-details "cellular_component") exportcsv/export-token
               "\""
               (clojure.string/join " | " (:dataset ortholog-details))
               "\"" "\n")
        ) "" organism-details))
    ) "" @results)
)))


(defn orthologs []
  (fn []
     [:div.ortholog-results
     (let [are-there-results? (re-frame/subscribe [:aggregate-results])]
      (if @are-there-results?
        ;;if there are results:
        [:div
          [:header
            [:h2 "Orthologous Genes"]
            [exportcsv/download-button (csv-body)]
           ]
          [:table.aggregate
            [aggregate-headers]
            [aggregate-results]
            [no-results]]]
))]))
