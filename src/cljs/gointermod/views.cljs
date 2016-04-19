(ns gointermod.views
    (:require [re-frame.core :as re-frame]
              [gointermod.search.views :as search]
              [gointermod.icons :as icons]))

(defn main-panel []
  (fn []
    [:div
      [icons/icons]
      [search/search]
    ]))
