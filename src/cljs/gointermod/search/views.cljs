(ns gointermod.search.views
    (:require [re-frame.core :as re-frame]))

(defn organism-dropdown []
  "returns a dropdown containing all of the organism names"
  (let [organisms (re-frame/subscribe [:organisms])]
    [:select {:on-change (fn [e] (re-frame/dispatch [:select-input-organism (aget e "target" "value")]))}
     (map (fn [[id details]]
        ^{:key id}
        [:option {:value (js->clj id)}
         (:common details) " - " (:abbrev details)])
      @organisms)
   ]))

(defn organism-output-selector []
 "returns form component with a checkbox beside each org name"
 (let [organisms (re-frame/subscribe [:organisms])]
   [:form
    (map (fn [[id details]]
       ^{:key id}
       [:label {:class (clj->js id)}
        [:input
         {:type "checkbox"
          :defaultChecked (:output? details)
          :on-change (fn [] (re-frame/dispatch [:toggle-output-organism id] id))}]
        (:abbrev details)])
     @organisms)]))


(defn search-form []
  "Visual component for initialising GO search."
  [:div.geneinput
    [:div.intro
      [:h3 "Search"]
      [:p "Search for gene orthologs across Human, Mouse, Rat, Fly, Zebrafish, Worm, and Yeast, and retrieve Gene Ontology annotations for any or all species."]]
    [:form.searchform
     {:name "searchform"
      :on-submit
        (fn [e]
          (.preventDefault js/e)
          (re-frame/dispatch [:set-status-loading])
          (re-frame/dispatch [:perform-search ]))}
      [organism-dropdown]
        [:textarea
         {:placeholder "Type identifiers here, e.g. 'ADH5'"
          :auto-focus "autofocus"
          :on-change (fn [e] (re-frame/dispatch [:update-search-term (aget e "target" "value")]))}]
        [:button {:type "submit"} "Search"]]])

   (defn evidence-code-filters-expanded []
     "Visually outputs list of evidence codes and checks the components which are marked as true in the db"
     (let [evidence-codes (re-frame/subscribe [:evidence-codes])]
       [:form.evidence.evidence-large {:style {:min-height "30em"}}
         (map-indexed (fn [index code-info]
           ^{:key (:code code-info)}
            [:label
              [:input
               {:type "checkbox"
                :defaultChecked (:checked code-info)
                :on-click (fn [] (re-frame/dispatch [:toggle-evidence-code index]))}]
             (:name code-info)])
         @evidence-codes)]))

 (defn evidence-code-filters-mini []
   "Visually outputs list of evidence codes and checks the components which are marked as true in the db"
   (let [evidence-codes (re-frame/subscribe [:evidence-codes])]
     [:div.evidence-mini.evidence
       (map (fn [code-info]
          (cond (:checked code-info)
            ^{:key (:code code-info)}
            [:span.code {:title (:name code-info)} (:code code-info)]))
       @evidence-codes)]))


(defn search-filters []
  "Search filter component. "
  (let [expand-codes? (re-frame/subscribe [:expand-evidence-codes?])]
    [:div.filters ;{:class (if @expand-codes? "expanded" "shrunk")}
      [:div.filter.output
        [:h4 "Output species"]
        [organism-output-selector]]
      [:div.filter
        [:h4 "Evidence codes"]
        [:h5 "Showing: "
        [:a
        {:href "#"
         :on-click
          (fn [e]
            (.preventDefault js/e)
            (re-frame/dispatch [:toggle-evidence-codes]))}
            (if @expand-codes?
              "(Shrink codes)"
              "(Change codes)")]]
          (if @expand-codes?
            [evidence-code-filters-expanded]
            [evidence-code-filters-mini])
       ]]))


(defn search []
  (fn []
    (let [expand-codes? (re-frame/subscribe [:expand-evidence-codes?])]
     [:div.search {:class (if @expand-codes? "expanded" "shrunk")}
      [search-form]
      [search-filters]])))
