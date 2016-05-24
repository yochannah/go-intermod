(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn graph [node]
  [:div.goterm [:div.jonesy "|"]
  [:div.flexy
    (map (fn [[k v]]
      ;(.log js/console (clj->js k) (clj->js v))
      (if (map?  v)
      (do ^{:key k} [:div.goterm [:div.title k] [:div.children (graph v)]])
      (do ^{:key (gensym)} [:div.title (str k)]))
) node)]])



(defn ontology []
 [:div.ontology
  (re-frame/dispatch [:go-ontology-tree])
  [:h2 "Ontology graph"]
   (let [tree @(re-frame/subscribe [:go-ontology-tree])]
     (.log js/console  "TREEEEEEEE"(clj->js tree))
    (graph tree)
     )
   ])
