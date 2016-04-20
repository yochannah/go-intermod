(ns gointermod.orthologresults.views
    (:require [re-frame.core :as re-frame]))

(defn headers []
  [:thead [:tr
  [:th "Species"]
  [:th "Orthologs"]
  [:th "Include"]
  [:th "Biological Process"]
  [:th "Molecular Function"]
  [:th "Cellular Component"]
   ]])

(defn results []
  [:tbody
   ;loop through results into tr.[]
   ]
  )

(defn orthologs []
  (fn []
     [:div.ortholog-results
      [:h2 "Orthologous Genes"]
      [:table
      [headers]
      [results]
       ]]))
