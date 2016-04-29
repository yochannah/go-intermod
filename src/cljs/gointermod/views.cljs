(ns gointermod.views
    (:require [re-frame.core :as re-frame]
      [gointermod.config :as config]
      [gointermod.search.views :as search]
      [gointermod.orthologresults.views :as orthologs]
      [gointermod.heatmap.views :as heatmap]
      [gointermod.ontology.views :as ontology]
      [gointermod.enrichment.views :as enrichment]
      [gointermod.icons :as icons]
      [json-html.core :as json-html])
(:use [json-html.core :only [edn->hiccup]]))

(defn nav []
  (let [active-view (re-frame/subscribe [:active-view])]
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


      [:h2 "Results filter:"]
      [:ul
        [:li
          [:a
            [:svg.icon [:use {:xlinkHref "#icon-biological-process"}]]
              "Biological\u00A0Process"]]
        [:li
          [:a
            [:svg.icon [:use {:xlinkHref "#icon-molecular-function"}]]
              "Molecular\u00A0Function"]]
        [:li
          [:a
            [:svg.icon [:use {:xlinkHref "#icon-cellular-component"}]]
              "Cellular\u00A0Component"]]]
   ]))


(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
    [:main
      [nav]
      (let [active-view (re-frame/subscribe [:active-view])]
        (cond
          (= @active-view :ortholog-summary)
            [orthologs/orthologs]
          (= @active-view :heatmap)
            [heatmap/heatmap]
          (= @active-view :ontology)
            [ontology/ontology]
          (= @active-view :enrichment)
            [enrichment/enrichment]
        )
    )]
      (when config/debug?
        [:div.db  (edn->hiccup (dissoc @(re-frame/subscribe [:db]) :multi-mine-results :heatmap))])
    ]))
