(ns go-intermod.views
    (:require [re-frame.core :as re-frame]))

(defn search-form []
  "Visual component for initialising GO search."
  [:form {:name "searchform"}
   [:h3 "Search"]
   [:p "Search for gene orthologs across Human, Mouse, Rat, Fly, Zebrafish, Worm, and Yeast, and retrieve Gene Ontology annotations for any or all species."]
   [:select
     [:option "Human"]
     [:option "Fly"]
    ]
   [:textarea {:placeholder "ADH5"}]
   [:button {:type "submit"} "Search"]])

(defn search-filters []
  [:div.filters "Output species"])

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div "Hello from " @name
       [:div.search
        [search-form]
        [search-filters]]])))
