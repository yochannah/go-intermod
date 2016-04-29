(ns gointermod.enrichment.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))


(defn enrichment []
  (fn []
   [:div.enrichment
    [:h2 "Enrichment"]
    [:div "Not done - but you can enrich your life by admiring this kitten"
      [:br]
        [:img {:src (str "https://placekitten.com/g/" (rand-nth [200 300 400 500 600]) "/" (rand-nth [200 300 400 500 600]))}]
        ]]
))
