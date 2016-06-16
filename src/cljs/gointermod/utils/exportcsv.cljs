(ns gointermod.utils.exportcsv
(:require [re-frame.core :as re-frame]))

(defn encode-data
  "helper to encode and format csv data for download"
  [csv-data]
  (.encodeURI js/window
    (str "data:text/csv;charset=utf-8," csv-data)))

(defn download-button
  "Visual component with an interactivity handler for when the user clicks the button. Triggers a CSV download of the provided data."
  [csv-data]
     [:a.download
      {:on-click (fn []
        (.open js/window (encode-data csv-data))
      )}
  [:svg.icon [:use {:xlinkHref "#icon-download"}]]
  "Download data as CSV" ])
