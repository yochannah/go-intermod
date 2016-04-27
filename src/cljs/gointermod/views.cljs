(ns gointermod.views
    (:require [re-frame.core :as re-frame]
      [gointermod.config :as config]
      [gointermod.search.views :as search]
      [gointermod.orthologresults.views :as orthologs]
      [gointermod.icons :as icons]
      [json-html.core :as json-html])
(:use [json-html.core :only [edn->hiccup]]))

(defn nav []
  [:nav
  [:h2 "Show results in:"]
  [:ul
    [:li.active
     [:svg.icon [:use {:xlinkHref "#icon-summary"}]]
     "Ortholog\u00A0Summary"]
   [:li
    [:svg.icon [:use {:xlinkHref "#icon-heatmap"}]]
    "Interactive\u00A0Heatmap"]
   [:li
     [:svg.icon [:use {:xlinkHref "#icon-tree"}]]
     "Ontology\u00A0Diagram"]]
   [:h2 "Results filter:"]
   [:ul
    [:li
     [:svg.icon [:use {:xlinkHref "#icon-biological-process"}]]
     "Biological\u00A0Process"]
   [:li
     [:svg.icon [:use {:xlinkHref "#icon-molecular-function"}]]
     "Molecular\u00A0Function"]
   [:li
     [:svg.icon [:use {:xlinkHref "#icon-cellular-component"}]]
     "Cellular\u00A0Component"]]
   ])


(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
     [:main
      [nav]
      [orthologs/orthologs]]
      (when config/debug?
        [:div.db  (edn->hiccup (dissoc @(re-frame/subscribe [:db]) :multi-mine-results))])
    ]))
