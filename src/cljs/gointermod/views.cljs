(ns gointermod.views
    (:require [re-frame.core :as re-frame]
      [gointermod.config :as config]
      [gointermod.nav :as nav]
      [gointermod.search.views :as search]
      [gointermod.orthologresults.views :as orthologs]
      [gointermod.heatmap.views :as heatmap]
      [gointermod.ontology.views :as ontology]
      [gointermod.enrichment.views :as enrichment]
      [gointermod.utils.icons :as icons]
      [gointermod.utils.utils :as utils]
      [secretary.core :as secretary]
      [json-html.core :as json-html])
(:use [json-html.core :only [edn->hiccup]]))


(defn content []
  (let [active-view (re-frame/subscribe [:active-view])]
    (cond
      (= @active-view :ortholog-summary)
        [orthologs/orthologs]
      (= @active-view :heatmap)
        [heatmap/heatmap]
      (= @active-view :ontology)
        [ontology/ontology]
      (= @active-view :enrichment)
        [enrichment/enrichment]
)))

(defn sample-query [identifier]
  [:a {:on-click (fn []
        (re-frame/dispatch [:update-search-term identifier])
        (re-frame/dispatch [:set-status-loading])
        (re-frame/dispatch [:set-view :ortholog-summary])
        (re-frame/dispatch [:perform-search ]))}
   identifier]
  )

(defn organism-line []
  [:div.organism-line
  [:div.rat]
  [:div.zebrafish]
  [:div.yeast]
  [:div.human]
  [:div.mouse]
  [:div.fly]
  [:div.worm]
])

(defn starter-content []
  [:div.default
    [:div
    [:img {:src "/img/logo.jpg"}]
      [:h2 "InterMOD GO Tool"]
      [:p "Type a Gene identifier or symbol into the searchbar up the top and press search."]
      [:p "If you're not sure what genes to choose, check out the results for "
     [sample-query "SOX18"] " or " [sample-query "ADH5"]]
     [organism-line]
     ]])

(defn default-content []
  (let [initialised (re-frame/subscribe [:initialised])]
  (if @initialised
    ;;loader if we've seen the start page
    [utils/loader]
    ;;if this is our first load, let's have the start page.
    [starter-content]))
  )

(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
      (let [are-there-results? (re-frame/subscribe [:aggregate-results])]
        [:main
          (cond @are-there-results? [nav/nav])
          [:section.contentbody
            (if @are-there-results?
              ;;if there are results:
              [content]
              ;;Placeholder for non-results
              (do (aset js/window "location" "href" "#")
                 [default-content]
        ))]])

        ; (when config/debug?
        ;     [:div.db  (edn->hiccup (:initialised (dissoc @(re-frame/subscribe [:db]) :multi-mine-results :heatmap)))])
    ]))
