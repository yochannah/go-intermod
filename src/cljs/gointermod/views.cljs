(ns gointermod.views
    (:require [re-frame.core :as re-frame]
      [gointermod.search.views :as search]
      [gointermod.orthologresults.views :as orthologs]
      [gointermod.icons :as icons]
      [json-html.core :as json-html])
(:use [json-html.core :only [edn->hiccup]]))


(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
      [orthologs/orthologs]
      [:div.db (edn->hiccup @(re-frame/subscribe [:db]))]
    ]))
