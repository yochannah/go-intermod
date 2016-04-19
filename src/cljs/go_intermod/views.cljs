(ns go-intermod.views
    (:require [re-frame.core :as re-frame]))

(defn organism-dropdown []
  "returns a dropdown containing all of the organism names"
  (let [organisms (re-frame/subscribe [:organisms])]
    [:select
     (map (fn [[id details]]
        ^{:key id}
        [:option
         (:common details) " - " (:abbrev details)])
      @organisms)
   ]))

(defn organism-output-selector []
 "returns form component with a checkbox beside each org name"
 (let [organisms (re-frame/subscribe [:organisms])]
   [:form
    (map (fn [[id details]]
       ^{:key id}
       [:label
        [:input {:type "checkbox" :checked "checked"}
        (:abbrev details)]])
     @organisms)]))


(defn search-form []
  "Visual component for initialising GO search."
  [:form.searchform {:name "searchform"}
   [:h3 "Search"]
   [:p "Search for gene orthologs across Human, Mouse, Rat, Fly, Zebrafish, Worm, and Yeast, and retrieve Gene Ontology annotations for any or all species."]
   [organism-dropdown]
   [:textarea {:placeholder "ADH5"}]
   [:button {:type "submit"} "Search"]])

(defn evidence-code-filters []
  "Visually outputs list of evidence codes and checks the components which are marked as true in the db"
 (let [evidence-codes (re-frame/subscribe [:evidence-codes])]
 [:form
 (map (fn [[name is-checked?]]
   ^{:key name}
   [:label
   [:input {:type "checkbox" :checked is-checked?}
   name]])
   @evidence-codes)
]))

(defn search-filters []
  "search filter component. "
  [:div.filters
    [:div
      [:h4 "Output species"]
      [organism-output-selector]]
    [:div
      [:h4 "Evidence codes"]
      [evidence-code-filters]]
    ])


(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div
       [:div.search
        [search-form]
        [search-filters]]])))
