(ns gointermod.nav
    (:require [re-frame.core :as re-frame]))

    (defn nav []
      (let [active-view (re-frame/subscribe [:active-view])
            active-filter (re-frame/subscribe [:active-filter])]
        [:nav
          [:h2 "Show results in:"]
          [:ul
            [:li
              [:a {:href "#/"
                   :class (cond (= @active-view :ortholog-summary) "active")}
                    [:svg.icon [:use {:xlinkHref "#icon-summary"}]]
                    "Ortholog\u00A0Summary"]]
            [:li
              [:a {:href "#/heatmap"
                   :class (cond (= @active-view :heatmap) "active")}
                    [:svg.icon [:use {:xlinkHref "#icon-heatmap"}]]
                    "Interactive\u00A0Heatmap"]]
            [:li
              [:a {:href "#/ontology"
                 :class (cond (= @active-view :ontology) "active")}
                  [:svg.icon [:use {:xlinkHref "#icon-tree"}]]
                  "Ontology\u00A0Diagram"]]
            [:li
              [:a {:href "#/enrichment"
                 :class (cond (= @active-view :enrichment) "active")}
                  [:svg.icon [:use {:xlinkHref "#icon-enrichment"}]]
                  "Enrichment"]]]

         (cond (not= @active-view :ortholog-summary)
          [:div
          [:h2 "Results filter:"]
          [:ul
            [:li
              [:a {:class (cond (= @active-filter "biological_process") "active")
                   :on-click #(re-frame/dispatch [:active-filter "biological_process"] )}
                [:svg.icon [:use {:xlinkHref "#icon-biological-process"}]]
                  "Biological\u00A0Process"]]
            [:li
            [:a {:class (cond (= @active-filter "molecular_function") "active")
                 :on-click #(re-frame/dispatch [:active-filter "molecular_function"] )}
                [:svg.icon [:use {:xlinkHref "#icon-molecular-function"}]]
                  "Molecular\u00A0Function"]]
            [:li
            [:a {:class (cond (= @active-filter "cellular_component") "active")
                 :on-click #(re-frame/dispatch [:active-filter "cellular_component"] )}
                [:svg.icon [:use {:xlinkHref "#icon-cellular-component"}]]
                  "Cellular\u00A0Component"]]]])
       ]))
