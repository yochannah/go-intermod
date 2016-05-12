(ns gointermod.nav
    (:require [re-frame.core :as re-frame]))

(defn build-url [organism]
  (str "http://"
      (:url (:mine organism))
      "/loadQuery.do?skipBuilder=true&query="
      (.encodeURIComponent js/window (:query organism))
      "&trail=|query&method=xml"
))

(defn modal [organism]
  [:div.fade {:on-click #(re-frame/dispatch [:active-modal nil])}
   [:div.modal {:class (:id organism)}
    [:h4 "Query for " (:abbrev organism)]
    [:pre {:on-click (fn [e]
      (.stopPropagation js/e))}
     (:query organism)]
   [:a
    {:href (build-url organism)
     :target "_blank"}
    [:svg.icon [:use {:xlinkHref "#icon-external"}]]
    "View this query in " (:name (:mine organism))
    ]
    (.log js/console (build-url organism))
   ]]
  )

(defn prep-status-details [organism]
  [:div
   (let [status (:status organism)
         active-modal (re-frame/subscribe [:active-modal])
         active (= @active-modal (:id organism))]
    (cond
      (= (:status status) :loading)
        [:span.loading "Loading . . ."]
      (= (:status status) :error)
        [:span.error "Error loading results"]
      (= (:status status) :na)
        [:span.na (.log js/console "F") "No search performed"]
      (= (:status status) :success)
        [:span.success (:details status) " results"]
      )
      [:div.query {:class (cond active "active")}
       (cond active [modal organism])
        [:svg.icon [:use {:xlinkHref "#icon-code"}]]]
)])

(defn status []
  (let [organisms (re-frame/subscribe [:organisms])]
    [:div.status
    [:h4 "Search status: "]
     [:div.results
    (doall (map (fn [[_ organism]]
      ^{:key (:id organism)}
      [:div.organism
        {:on-click
          #(re-frame/dispatch [:active-modal (:id organism)])
          :class (clj->js (:id organism))}
          [:h5 (:abbrev organism)]
          [:div (prep-status-details organism)
        ]]
           ) @organisms))
    [:p.info "'Result' in this context means a single organism + ortholog + GO Term combination. Results in the main pane are aggregated, so will not match the numbers shown here."] ]]))

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
   [status]
 ]))
