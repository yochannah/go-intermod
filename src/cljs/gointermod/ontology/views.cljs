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
      [:div.ontology "Not here yet. Sorry folks. Look at the kitten instead" [:br]
        ;[:blockquote "She wore a raspberry beret, the kind you find in the second hand store."]
        [:img {:src (str "https://placekitten.com/g/" (rand-nth [200 300 400 500]) "/" (rand-nth [200 300 400 500]))}]
        ]]))
