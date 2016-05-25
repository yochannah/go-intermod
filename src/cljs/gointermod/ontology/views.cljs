(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn organism-node [organism vals]
  [:div.title [:div.organism {:class (clj->js organism)} (clj->js organism)]])

(defn graph [node]
  [:div [:div.jonesy "|"]
    [:div.flexy
     (let [organisms (re-frame/subscribe [:organisms])]
    (doall  (map (fn [[k v]]
        [:div.goterm {:key k}
         ;(.log js/console (clj->js organisms) (clj->js k))
          (if (contains? @organisms k)
            [organism-node k v]
            [:div.title (clj->js k)]
            )
          (cond (and (map? v) (not (contains? @organisms k)))
          [:div.children {:key (gensym)} (graph v)])
]
) node)))]])



(defn ontology []
 [:div.ontology
  (re-frame/dispatch [:go-ontology-tree])
  [:h2 "Ontology graph"]
   (let [tree @(re-frame/subscribe [:go-ontology-tree])]
    (graph tree)
)])
