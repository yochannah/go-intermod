(ns gointermod.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [gointermod.handlers]
              [gointermod.subs]
              [gointermod.routes :as routes]
              [gointermod.views :as views]
              [gointermod.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
