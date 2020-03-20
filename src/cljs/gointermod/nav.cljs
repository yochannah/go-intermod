(ns gointermod.nav
    (:require [re-frame.core :as re-frame]))

(defn build-url
  "generate a deep link for a given mine go query - e.g. link straight to humanmine query results page"
  [organism]
  (str "https://"
      (:url (:mine organism))
      "/loadQuery.do?skipBuilder=true&query="
      (.encodeURIComponent js/window (:query organism))
      "&trail=|query&method=xml"
))

(defn modal
  "visual component - modal popup to show the query in search status sidebar"
  [organism]
  [:div.fade {:on-click
      (fn [e]
        (.stopPropagation js/e)
        (re-frame/dispatch [:active-modal nil]))}
   [:div.modal {:class (:id organism)
      :on-click (fn [e]
        (.stopPropagation js/e))}
    [:h4 "Query for " (:abbrev organism)]
    [:a.close {:aria-label "close"
        :on-click (fn [e]
          (.stopPropagation js/e)
          (re-frame/dispatch [:active-modal nil]))} "×"]
    [:a.view-query { :href (build-url organism)
          :target "_blank"}
      [:svg.icon [:use {:xlinkHref "#icon-external"}]]
           "View this query in " (:name (:mine organism))]
    (if (:query organism)
      ;;show the query if there is one
      [:pre (:query organism)]
      ;;Else tell them it wasn't searched
      [:p "This organism wasn't selected as an output species so no query was performed."]
    )
   ]]
  )

(defn prep-status-details
  "status bar for a given mine"
  [organism]
  (let [result-counts @(re-frame/subscribe [:mine-result-counts])
        organism-id (:id organism)
        result-count (organism-id result-counts)]
    [:div
      (cond
        (= (:status (:status organism)) :loading)
          [:span.loading "Loading . . ."]
        (= (:status (:status organism)) :error)
          [:span.error "Error loading results"]
        (= (:status (:status organism)) :na)
          [:span.na "No search performed"]
        (= (:status (:status organism)) :success)
            [:div.counts
              [:span.success "Genes: " (:genes result-count)]
              (cond (not= 0 (:orthologs (:genes result-count)) )
                [:span.success "Annotations: " (:annotations result-count)])
             ]
        )
     (let [status (:status organism)
           active-modal (re-frame/subscribe [:active-modal])
           active (= @active-modal (:id organism))]
        [:div.query {:class (cond active "active")}
          (cond active [modal organism])
          [:svg.icon [:use {:xlinkHref "#icon-code"}]]]
)]))

(defn status []
  (let [organisms (re-frame/subscribe [:organisms])]
    [:div.status
    [:h4 "Search status: "]
    (into [:div.results]
      (map (fn [[_ organism]]
        [:div.organism
          {:on-click
            #(re-frame/dispatch [:active-modal (:id organism)])
            :class (clj->js (:id organism))}
            [:h5 (:abbrev organism)]
            [:div (prep-status-details organism)
          ]]
    ) @organisms))
]))

(defn nav []
(let [active-view (re-frame/subscribe [:active-view])
      active-filter (re-frame/subscribe [:active-filter])]
  [:nav
    [:h2 "Show results in:"]
    [:ul
      [:li
        [:a {:href "#/"
             :class (cond (= @active-view :ortholog-summary) "active")}
              [:svg.icon [:use {:xlinkHref "#icon-summary"}]]
              "Ortholog\u00A0Summary"]]
      [:li
        [:a {:href "#/heatmap"
             :class (cond (= @active-view :heatmap) "active")}
              [:svg.icon [:use {:xlinkHref "#icon-heatmap"}]]
              "Interactive\u00A0Heatmap"]]
      [:li
        [:a {:href "#/ontology"
           :class (cond (= @active-view :ontology) "active")}
            [:svg.icon [:use {:xlinkHref "#icon-tree"}]]
            "Ontology\u00A0Diagram"]]
      [:li
        [:a {:href "#/enrichment"
           :class (cond (= @active-view :enrichment) "active")}
            [:svg.icon [:use {:xlinkHref "#icon-enrichment"}]]
            "Enrichment"]]]

   (cond (not= @active-view :ortholog-summary)
    [:div
    [:h2 "Results filter:"]
    [:ul
      [:li
        [:a {:class (cond (= @active-filter "biological_process") "active")
             :on-click #(re-frame/dispatch [:active-filter "biological_process"] )}
          [:svg.icon [:use {:xlinkHref "#icon-biological-process"}]]
            "Biological\u00A0Process"]]
      [:li
      [:a {:class (cond (= @active-filter "molecular_function") "active")
           :on-click #(re-frame/dispatch [:active-filter "molecular_function"] )}
          [:svg.icon [:use {:xlinkHref "#icon-molecular-function"}]]
            "Molecular\u00A0Function"]]
      [:li
      [:a {:class (cond (= @active-filter "cellular_component") "active")
           :on-click #(re-frame/dispatch [:active-filter "cellular_component"] )}
          [:svg.icon [:use {:xlinkHref "#icon-cellular-component"}]]
            "Cellular\u00A0Component"]]]])
   (let [are-there-results? (re-frame/subscribe [:aggregate-results])]
    (cond @are-there-results?
      [status]))
 ]))
