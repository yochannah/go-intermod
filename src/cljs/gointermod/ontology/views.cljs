(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [reagent.core :as reagent]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn make-id [thenaughtystring] (clojure.string/escape thenaughtystring {" " "_"}))
(defn elem [id] (.getElementById js/document (make-id id)))
(def nodelist (reagent/atom #{}))
(def dontrender (reagent/atom #{}))


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

(defn organism-node [term vals parent]
  "outputs the go term, organism, and orthologue for nodes with these types of results"
  (into [:div.title {:id (make-id term)} term ]
    (map (fn [[organism results]]
      [:div.organism {:class (clj->js organism)}
        (utils/get-abbrev organism) " "
        (reduce (fn [_ result] (:display-ortholog-id result)) [] results)]
) vals)))

(defn graph [node parent]
  "recursively builds an HTML tree of terms. "
  (let [my-state (reagent/atom {})]
  (reagent/create-class {
    :component-will-mount (fn [this]
      (let [nodes (set (keys (reagent/props this)))
            duplicates (clojure.set/intersection nodes @nodelist)]
        (cond (seq duplicates)
          (do
        (.log js/console "component will mount" (clj->js (keys (reagent/props this))) (clj->js (clojure.set/intersection nodes @nodelist)))
            (swap! my-state assoc :what duplicates)
          ))
      )
    )
    :component-will-unmount (fn [this]
      (.log js/console "un mount" (clj->js @my-state) (clj->js (set (keys (reagent/props this)))))
      (reset! my-state {})
      (swap! nodelist clojure.set/difference (set (keys (reagent/props this))))
          )

    :reagent-render (fn [node parent]
      (into [:div.flexy] (map (fn [[k v]]
;(cond (not (does-node-exist? k))
;;save the node to an atom so we don't make duplicates
(swap! nodelist conj k)
        [:div.goterm {:key k}
          (if (contains? v :results)
            ;;output the goterm and the organisms/orthologues, or just the go term if it has no orthologues associated.
              [organism-node k (:results v) parent]
            (cond (empty? @my-state)  [:div.title {:id (make-id k) }
               (clj->js k) ]))
         (let [non-result-children (dissoc v :results)]
            (cond (and (map? non-result-children) (seq non-result-children))
              [:div.children {:key (gensym)} [graph non-result-children k]]))
    ]) node)))
    ;;it's impossible to calculate the position of a node that hasn't been added to the screen-dom (as opposed to the shadow dom) yet, so we have to wait until the html has been added to the screen, then we send the relevant html nodes to the line calculator function
    :component-did-mount (fn [this]
      (mapv (fn [[parentname propvals]]
        (mapv (fn [[childname _]]
          (cond (and (not (keyword? childname)) (some? parentname)  )
            (drawline (elem parentname) (elem childname)))
        ) propvals)
    ) (reagent/props this)))
})))



(defn ontology []
 [:div.ontology
 (let [tree @(re-frame/subscribe [:go-ontology-tree])]
 (re-frame/dispatch [:go-ontology-tree])
  [:h2 "Ontology graph "]
  (.clear js/console)
   [:div
    [:svg#lineplacer
      [:path#jango ;;we clone this element lots.
        {:style {:stroke "#666" :stroke-width 2 :fill "transparent"} :d ""}]]
    [graph tree nil]]
)])
