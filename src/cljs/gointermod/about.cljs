(ns gointermod.about
    (:require [re-frame.core :as re-frame]))

(defn about []
  (fn []
     [:div
      [:h2 "InterMOD GO Tool"]
      [:p "This tool is a collaborative effort made the the InterMOD consortium to create a tool that aggregates Gene Ontology search results across Human, Mouse, Rat, Fly, Zebrafish, Worm, and Yeast"]
      ]))
