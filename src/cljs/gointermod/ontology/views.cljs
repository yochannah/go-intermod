(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))


(defn ontology []
  (fn []
     [:div.ontology
      [:h2 "Ontology graph"]
      [:div
       (re-frame/dispatch [:load-go-graph])
       "This is where GO goes"
       ]]))
