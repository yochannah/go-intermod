(ns gointermod.heatmap.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.search.handlers :as search]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.exportcsv :as exportcsv]
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
;    (.log js/console "%c@expanded" "color:goldenrod;font-weight:bold;" (clj->js @expanded))
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

(defn organism-orthologue-tds
  "Visually outputs the first two tds in the heatmap, orthologue and organism.  They require some custom logic compared to the basic number cells"
  [result]
  (let [organism-name (first result)
        organism-id (utils/organism-name-to-id organism-name)
        orthologue-counts (re-frame/subscribe [:ortholog-count])
        org-count (organism-id @orthologue-counts)
        ortholog (second result)
        expanded-org (re-frame/subscribe [:heatmap-expansion])
        is-expanded? (contains? @expanded-org organism-id)
        is-expanded-but-summary-row? (and is-expanded? (number? ortholog))]
  [:tr {:class organism-id}
   ;;organism name td:
    [:td
      ;;output differently depending on the status of the
      ;;expanded/expandable) row
      (cond
        is-expanded-but-summary-row? organism-name
        is-expanded? [:svg.icon [:use {:xlinkHref "#expanded-row"}]]
        true organism-name)

      (if
        ;;so if it's a number, it's a multi-result row. We want number rows to be expandable.
      (number? ortholog)
        [:span
          (if is-expanded?
            [:span.expand {:on-click #(re-frame/dispatch [:collapse-heatmap organism-id])} [:svg.icon [:use {:xlinkHref "#icon-circle-down"}]]]
            [:span.expand {:on-click #(re-frame/dispatch [:expand-heatmap organism-id])} [:svg.icon [:use {:xlinkHref "#icon-circle-right"}]]]
        )]
     )

     ]
    (if (number? ortholog)
      ;;handle aggregate result counts
      [:td
        (if (= org-count 1)
          ;;don't say "1 gene", just output the gene itself.
          ortholog
          [:span org-count " genes " ])]
      ;;if the results aren't aggregate (e.g. the user expanded them) we just output its name
      [:td ortholog]
     )]))

(defn make-cell-title
  "Creates hover text for td cells"
  [result go-term]
  (let [ortholog-name (second result)
        organism (first result)]
  (str organism ", " ortholog-name
       (if (number? ortholog-name) " genes" )
    ".\nGO term: " go-term ".")
))

(defn counts
  "Visually output the table of annotation counts."
  []
  ;;subscribe to the heatmap data
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate])
        go-terms (:headers @heatmap)]
  ;;output tr, one per organism,ortholog combo
    (into [:tbody]
      (map (fn [result]
        (into (organism-orthologue-tds result)
          (map-indexed (fn [index val]
             ;;one td per go term
             [:td {:style (calc-color val)
                   :title (make-cell-title result (nth go-terms index)) } val]
          ) (drop 2 result)))
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
          [:td.no-orthologs {:col-span 3} "No results available"]
          [:td.no-go-terms {:col-span (- cols 2)} "N/A"]
]) empties))))

(defn csv-counts
  "format heatmap results as a csv for download"
  []
  (let [heatmap (re-frame/subscribe [:heatmap-aggregate-csv])
        go-terms (:headers @heatmap)
        headers (str "Organism,Ortholog," (clojure.string/join "," go-terms) "\n")]
    (reduce (fn [csv-str result]
      (str csv-str (clojure.string/join "," result) "\n" )
) headers (:rows @heatmap))))

(defn heatmap []
  (re-frame/dispatch [:trigger-data-handler-for-active-view])
  (fn []  [:div.heatmap
      [:header
        [:h2 "Annotation count by species"]
        [exportcsv/download-button (csv-counts)]]
      [:table
        [headers]
        [counts]
        [empty-rows]
]]))
