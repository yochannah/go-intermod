(ns gointermod.views
    (:require [re-frame.core :as re-frame]
      [gointermod.config :as config]
      [gointermod.nav :as nav]
      [gointermod.search.views :as search]
      [gointermod.orthologresults.views :as orthologs]
      [gointermod.heatmap.views :as heatmap]
      [gointermod.ontology.views :as ontology]
      [gointermod.enrichment.views :as enrichment]
      [gointermod.utils.icons :as icons]
      [json-html.core :as json-html])
(:use [json-html.core :only [edn->hiccup]]))




(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
    [:main
      [nav/nav]
      (let [active-view (re-frame/subscribe [:active-view])]
        [:section.contentbody
        (cond
          (= @active-view :ortholog-summary)
            [orthologs/orthologs]
          (= @active-view :heatmap)
            [heatmap/heatmap]
          (= @active-view :ontology)
            [ontology/ontology]
          (= @active-view :enrichment)
            [enrichment/enrichment]
        )]
    )]
      ;  (when config/debug?
      ;    [:div.db  (edn->hiccup (dissoc @(re-frame/subscribe [:db]) :multi-mine-results :heatmap))])
    ]))
