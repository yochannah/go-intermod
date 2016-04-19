(ns gointermod.search.views
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
        [:input {:type "checkbox" :defaultChecked "checked"}
        (:abbrev details)]])
     @organisms)]))


(defn search-form []
  "Visual component for initialising GO search."
  [:form.searchform {:name "searchform"}
   [:h3 "Search"]
   [:p "Search for gene orthologs across Human, Mouse, Rat, Fly, Zebrafish, Worm, and Yeast, and retrieve Gene Ontology annotations for any or all species."]
   [organism-dropdown]
   [:textarea {:placeholder "Type identifiers here, e.g. 'ADH5'"}]
   [:button {:type "submit"} "Search"]])

   (defn evidence-code-filters-expanded []
     "Visually outputs list of evidence codes and checks the components which are marked as true in the db"
     (let [evidence-codes (re-frame/subscribe [:evidence-codes])]
       [:form
         (map (fn [code-info]
           ^{:key (:code code-info)}
            [:label
              [:input {:type "checkbox" :defaultChecked (:checked code-info)}
              (:name code-info)]])
         @evidence-codes)]))

 (defn evidence-code-filters-mini []
   "Visually outputs list of evidence codes and checks the components which are marked as true in the db"
   (let [evidence-codes (re-frame/subscribe [:evidence-codes])]
     [:div.evidence-mini
        [:h5 "Showing:"]
       (map (fn [code-info]
          (cond (:checked code-info)
            ^{:key (:code code-info)}
            [:span.code {:title (:name code-info)} (:code code-info)]))
       @evidence-codes)]))


(defn search-filters []
  "Search filter component. "
  (let [expand-codes? (re-frame/subscribe [:expand-evidence-codes?])]
    [:div.filters
      [:div.filter
        [:h4 "Output species"]
        [organism-output-selector]]
      [:div.filter
        [:h4 "Evidence codes"]
          (if @expand-codes?
            [evidence-code-filters-expanded]
            [evidence-code-filters-mini])
        [:a
         {:on-click
          (fn []
            (re-frame/dispatch [:toggle-evidence-codes]))}
         (if @expand-codes?
           "Shrink codes"
           "Change codes")]
       ]]))


(defn search []
  (fn []
     [:div.search
      [search-form]
      [search-filters]]))
