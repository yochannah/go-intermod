(ns gointermod.orthologresults.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.db :as db]))

(re-frame/register-handler
  :toggle-select-all
  (fn [db [_ _]]
    (.log js/console "Yeah, you clicked meh.")
    ;;if there are any selected, deselect.
    ;;else, select all
    db))
