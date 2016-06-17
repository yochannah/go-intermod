(ns gointermod.utils.exportcsv)

(defn encode-data
  "helper to encode and format csv data for download"
  [csv-data]
  (.encodeURI js/window
    (str "data:text/csv;charset=utf-8," csv-data)))

(defn download-button
  "Visual component with an interactivity handler for when the user clicks the button. Triggers a CSV download of the provided data.
  Weird note: Enrichment can't use this trigger. something to do with dereferencing the data caused an endless reload loop. DOSing the servers ain't cool. See the Enrichment handler and view for what we did there. Ideally in the world where we have lots of time we could refactor so all download buttons use the enrichment method."
  [csv-data]
     [:a.download
      {:on-click (fn []
        (.open js/window (encode-data csv-data))
      )}
  [:svg.icon [:use {:xlinkHref "#icon-download"}]]
  "Download data as CSV" ])
