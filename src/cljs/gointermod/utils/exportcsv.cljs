(ns gointermod.utils.exportcsv
  (:require [re-frame.core :as re-frame]))

(def export-token  "\t")

(defn encode-data
  "helper to encode and format csv data for download"
  [csv-data]
  (.encodeURI js/window
    (str "data:text/csv;charset=utf-8," csv-data)))

(defn stringify-results [results]
  ;loop through organisms
  (reduce (fn [my-tsv [organism result-vecs]]
    (str my-tsv
      ;;loops through results of each organism
      (reduce (fn [new-str one-result]
        ;;glue the results together
        (str new-str (clojure.string/join export-token (vals (into (sorted-map) one-result))) "\n")) "" result-vecs))
  ) "" @results))

(defn make-header-row [results]
  (let [headers (keys (into (sorted-map)
                  (second (first (remove #(empty? (second %)) (seq @results))))))
        non-keyword-headers (reduce (fn [newvec header] (conj newvec (clj->js header))) [] headers)]
    (clojure.string/join export-token non-keyword-headers)
))

(defn all-term-button []
  [:a.download
    {:on-click (fn []
      ;;this insanity gets the alphabetically ordered headers from the first non-empty result.
      (let [results (re-frame/subscribe [:multi-mine-results])
            headers (make-header-row results)
            downloadable-results (stringify-results results)]
      (.open js/window (encode-data (str headers "\n" downloadable-results)))) ;TODO REPLACE D WITH DATA
    )}
    [:svg.icon [:use {:xlinkHref "#icon-download"}]]
    "All orthologs & GO Terms" ])

(defn download-button
  "Visual component with an interactivity handler for when the user clicks the button. Triggers a CSV download of the provided data.
  Weird note: Enrichment can't use this trigger. something to do with dereferencing the data caused an endless reload loop. DOSing the servers ain't cool. See the Enrichment handler and view for what we did there. Ideally in the world where we have lots of time we could refactor so all download buttons use the enrichment method."
  [csv-data]
    [:div.download
     [:h3 "TSV Downloads:"]
      [:a.download
        {:on-click (fn []
          (.open js/window (encode-data csv-data))
        )}
        [:svg.icon [:use {:xlinkHref "#icon-download"}]]
        "This table" ]
     [all-term-button]
])
