(ns gointermod.orthologresults.views
    (:require [re-frame.core :as re-frame]))

(defn headers []
  [:thead [:tr
  [:th "Species"]
  [:th "Orthologs"]
  [:th "GO identifier thingy"]
  [:th "Term"]
  [:th "Branch"]
   ]])

 (defn aggregate-headers []
   [:thead [:tr
   [:th "Species"]
   [:th "Orthologs"]
   [:th "Include"]
   [:th "Biological Process"]
   [:th "Molecular Function"]
   [:th "Cellular Component"]
    ]])

(defn aggregate-result-row [[original-symbol original-secondary-id original-id _ _ homie-id homie-secondary-id homie-symbol homie-organism _ data-set _ _ _ _ go-identifier ontology-term ontology-branch]]
;  (.log js/console (clj->js result))
  ^{:key (str original-id go-identifier)}
  [:tr
  [:td homie-organism]
  [:td homie-symbol]
  [:td [:input {:type "checkbox"}]]
  [:td homie-organism]
  [:td homie-organism]
  [:td homie-organism]
   ])

 (defn result-row [[original-symbol original-secondary-id original-id _ _ _ homie-id homie-secondary-id homie-symbol homie-organism _ data-set  _ pub-id _ go-identifier ontology-term ontology-branch]]
   ^{:key (gensym)}
   [:tr
   [:td homie-organism]
   [:td homie-symbol]
   [:td go-identifier]
   [:td ontology-term]
   [:td ontology-branch]
    ])

(defn count-by-ontology-branch [branch]
  (let [search-results (:results @(re-frame/subscribe [:search-results]))]
  (count (filter
   (fn [result] (= (last result) branch)) search-results))
))

(defn get-id [primary secondary symbol]
  "returns first non-null identifier, preferring symbol or primary id"
  (first (remove nil? [symbol primary secondary]))
  )

(defn aggregate-by-species []
  (let [search-results (:results @(re-frame/subscribe [:search-results]))]
    (reduce (fn [new-map [_ _ _ _ _ primary-id secondary-id symbol organism & args ]]
      (update-in new-map [(keyword organism ) (keyword (get-id primary-id secondary-id symbol)) (keyword (last args))] inc))
        {} search-results)
))

(defn aggregate-results []
  "output search results into table rows"
  (let [search-results (re-frame/subscribe [:search-results])]
  [:tbody
   ;loop through results into tr.[]
   (map aggregate-result-row (:results @search-results))
   ]
  ))

(defn results []
  "output search results into table rows"
  (let [search-results (re-frame/subscribe [:search-results])]
  [:tbody
   ;loop through results into tr.[]
   (map result-row (:results @search-results))
   ]
  ))

(defn orthologs []
  (fn []
     [:div.ortholog-results
      (.log js/console "Aggregate" (clj->js (aggregate-by-species)))
      [:br]
     "bio:" (count-by-ontology-branch "biological_process")
     "molecular_function:" (count-by-ontology-branch "molecular_function")
     "cellular_component:" (count-by-ontology-branch "cellular_component")
      [:h2 "Orthologous Genes"]
      [:table
      [headers]
;      [aggregate-headers]
;      [aggregate-results]
      [results]
       ]]))
