(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))


(defn ontology []
 [:div.ontology
  [:h2 "Ontology graph"]
  [:div
   "D F"
   (.log js/console (clj->js @(re-frame/subscribe [:go-ontology-tree])))
   ]])
