(ns go-intermod.views
    (:require [re-frame.core :as re-frame]
              [go-intermod.search.views :as search]))

(defn main-panel []
  (fn []
    [:div
     [search/search]]))
