(ns gointermod.utils.export
(:require [re-frame.core :as re-frame]))

(defn download-button [data]
     [:button
      {:style {:float "right"}
       :on-click (fn [e]
        (.log js/console "%cdata" "color:hotpink;font-weight:bold;" (clj->js data))
                           )} "Export"]
  )
