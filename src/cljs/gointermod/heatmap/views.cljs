(ns gointermod.heatmap.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn heatmap []
  (fn []
     [:div.heatmap
      [:h2 "Annotation count by species"]
      [:div "This is still in the works, kids. In the meantime, here's a kitten." [:br]
;       [:blockquote "I only want to see you laughing in the purple rain"]
               [:img {:src (str "https://placekitten.com/g/" (rand-nth [200 300 400 500]) "/" (rand-nth [200 300 400 500]))}]]
        ]))
