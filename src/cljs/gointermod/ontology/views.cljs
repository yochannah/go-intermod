(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [reagent.core :as reagent]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn make-id [thenaughtystring] (clojure.string/escape thenaughtystring {" " "_"}))
(defn elem [id] (.getElementById js/document (make-id id)))


(defn organism-node [term vals parent]
  (into [:div.title term]
   (map (fn [[organism results]]
      [:div.organism
       {:key (gensym) :class (clj->js organism) :id (make-id term)}
       (utils/get-abbrev organism) " " (reduce (fn [_ result] (:display-ortholog-id result)) [] results)]
) vals)))

;;common definitions for elements we come back to a bit.
(def jango (elem "jango"))
(def lineplacer (elem "lineplacer"))

;;pixelcalcs
(defn svg-offset [] (.-top (.getBoundingClientRect (elem "lineplacer"))))
(defn y-offset [] (+ (aget js/document "body" "scrollTop") (svg-offset)))
(defn get-middle-x [box] (- (+ (.-left js/box) (/ (.-width js/box) 2)) 272 ))
(defn get-middle-y-top [box] (- (.-top js/box) (y-offset)) )
(defn get-middle-y-bottom [box] (- (.-bottom js/box) (y-offset)))

(defn clonepath []
  "Clones the master path node and appends it to the svg. Returns the cloned appended node so it can be shaped correctly."
  (let [boba (.cloneNode js/jango)]
      (.removeAttribute js/boba "id")
      (.appendChild js/lineplacer boba)
  boba))

(defn drawline [parent child]
  "given a parent HTML element and a child HTML element, draw a line between the bottom of the parent and the top of the child. This is done by calculating the locations of each of the boxes, and offsetting by 1) the amount scrolled on the window if any and 2) the other elements in the page."
  (let [parent-size (.getBoundingClientRect js/parent)
        child-size (.getBoundingClientRect js/child)
        start-x (get-middle-x parent-size)
        start-y (get-middle-y-bottom parent-size)
        end-x (get-middle-x child-size)
        end-y (get-middle-y-top child-size)
        d (str "M " start-x " " start-y " S " start-x " " start-y ", "  end-x " " end-y)
        path (clonepath)]
     (.setAttribute js/path "d" d)
    ))

(defn graph [node parent]
  (reagent/create-class {
    :reagent-render
      (fn [node parent]
      [:div {:parent-node parent}
       ;(.log js/console parent)
          (into [:div.flexy] (map (fn [[k v]]
            [:div.goterm {:key k}
              (if
                (contains? v :results)
                  [organism-node k (:results v) parent]
                ;(not= k :results)
                  [:div.title {:id (make-id k) } (clj->js k) ]
                )
             (let [non-result-children (dissoc v :results)]
              (cond (and (map? non-result-children) (seq non-result-children))
              [:div.children
               {:key (gensym)} [graph non-result-children k]]))
            ]) node))])
    :component-did-mount (fn [this]
      (let [props (reagent/props this)]
        (mapv (fn [[parentname propvals]]
            (mapv (fn [[childname _]]
                    (cond (and (not (keyword? childname)) (some? parentname)  )
                  (drawline (elem parentname) (elem childname)))
                    ) propvals)
               ) props)))
}))



(defn ontology []
 [:div.ontology
 (let [tree @(re-frame/subscribe [:go-ontology-tree])]
  (re-frame/dispatch [:go-ontology-tree])
  [:h2 "Ontology graph "]
  (.clear js/console)
   [:div [:svg#lineplacer
   [:path#jango ;;we clone this element lots.
    {:style {:stroke "#666" :stroke-width 2 :fill "transparent"},
     :d ""}]]
    [graph tree nil]]
)])
