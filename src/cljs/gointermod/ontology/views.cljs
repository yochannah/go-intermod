(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [reagent.core :as reagent]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

;;some utility dom handling functions
(defn make-id [thenaughtystring]
  "escaping those annoying spaces found in go terms so we can use the go term as an HTML ID"
  (clojure.string/escape thenaughtystring {" " "_"}))
(defn elem [id] (.getElementById js/document (make-id id)))

;;some state required to ensure we don't render the same terms twice.
;;duplicate IDs makes angry browsers.
(def nodelist (reagent/atom #{}))

;;common definitions for elements we come back to repeatedly.
(def jango (elem "jango"))
(def lineplacer (elem "lineplacer"))

;;pixelcalcs
(defn svg-offset-y [] (.-top (.getBoundingClientRect (elem "lineplacer"))))
(defn svg-offset-x [] (.-left (.getBoundingClientRect (elem "lineplacer"))))
(defn y-offset [] (+ (aget js/document "body" "scrollTop") (svg-offset-y)))
(defn get-middle-x [box] (- (+ (.-left js/box) (/ (.-width js/box) 2)) (svg-offset-x) ))
(defn get-middle-y [box] (- (+ (.-top js/box) (/ (.-height js/box) 2)) (y-offset)) )

(defn clonepath
  "Clones the master path node and appends it to the svg. Returns the cloned appended node so it can be shaped correctly."
  []
  (let [boba (.cloneNode js/jango)]
      (.removeAttribute js/boba "id")
      (.appendChild js/lineplacer boba)
  boba))

(defn build-path
  "Makes a pretty line from smack bang the middle of [parent] to [child]"
  [parent child]

  (let [parent-size (.getBoundingClientRect js/parent)
  child-size (.getBoundingClientRect js/child)
  start-x (get-middle-x parent-size)
  start-y (get-middle-y parent-size)
  end-x (get-middle-x child-size)
  end-y (get-middle-y child-size)]
  (str "M " start-x " " start-y " S " start-x " "
       (if (not= start-y end-y)
         ;;don't make it curvy if it's a direct left-right line
        (- start-y 35)
        start-y)
       ", "  end-x " " end-y)
))

(defn drawline
  "given a parent HTML element and a child HTML element, draw a line between the bottom of the parent and the top of the child. This is done by calculating the locations of each of the boxes, and offsetting by 1) the amount scrolled on the window if any and 2) the other elements in the page."
  [parent child]
     (.setAttribute (clonepath) "d" (build-path parent child)))

(defn organism-node
  "outputs the go term, organism, and orthologue for nodes with these types of results"
  [term vals parent]

  (into [:div.title {:id (make-id term)} term ]
    (map (fn [[organism results]]
      [:div.organism {:class (clj->js organism)}
        (utils/get-abbrev organism) " "
        (reduce (fn [_ result] (:display-ortholog-id result)) [] results)]
) vals)))

(defn graph
"recursively builds an HTML tree of terms. "
[node parent]

(let [my-state (reagent/atom #{})]
  (reagent/create-class {
    :component-will-mount (fn [this]
      (let [nodes (set (keys (reagent/props this)))
            duplicates (clojure.set/intersection nodes @nodelist)]
        (cond (seq duplicates)
          (swap! my-state clojure.set/union duplicates))
    ))
    :component-will-unmount (fn [this]
      ;;we reset the "what's been rendered" counter here; if we don't, and we navigate away from this page, and come back, nothing will render, since we still have the previous state.
      (reset! my-state {})
      (swap! nodelist clojure.set/difference (set (keys (reagent/props this)))))

    :reagent-render (fn [node parent]
      (into [:div.flexy] (map (fn [[k v]]
        ;;save the node to an atom so we don't make duplicates
        (swap! nodelist conj k)
        ;;ok, render tho GO term itself
        [:div.goterm {:key k}
          (if (contains? v :results)
            ;;output the goterm and the organisms/orthologues, or just the go term if it has no orthologues associated.
              [organism-node k (:results v) parent]
              (cond (not (contains? @my-state k))
                ;;this cond is saying don't render duplicate go terms, kthx.
                [:div.title {:id (make-id k) }
                  (clj->js k) ]))
         ;;now we've done the terms, let's render the child terms
         ;;if there are some.
         (let [non-result-children (dissoc v :results)]
            (cond (and (map? non-result-children) (seq non-result-children))
              [:div.children [graph non-result-children k]]))
    ]) node)))

    :component-did-mount (fn [this]
    ;;it's impossible to calculate the position of a node that hasn't been added to the screen-dom (as opposed to the shadow dom) yet, so we have to wait until the html has been added to the screen, then we send the relevant html nodes to the line calculator function.
      (mapv (fn [[parentname propvals]]
        (mapv (fn [[childname _]]
          (cond (and (not (keyword? childname)) parentname)
            (drawline (elem parentname) (elem childname)))
        ) propvals)
    ) (reagent/props this)))
})))

(defn no-spaghetti-graphs-please [nodes]
  [:div

   [:p
    [:svg.icon [:use {:xlinkHref "#icon-info"}]]
    " Looks like there are " nodes " GO terms associated with these genes. "]
   [:p "This many terms usually results in an unreadable spaghetti monster graph. Try selecting fewer genes / orthologs / organisms and searching again."]]
)


(defn ontology []
 [:div.ontology
 (let [tree @(re-frame/subscribe [:go-ontology-tree])
      nodes (re-frame/subscribe [:go-ontology-nodecount])]

 (re-frame/dispatch [:go-ontology-tree])
 (re-frame/dispatch [:go-ontology-nodecount])
  [:h2 "Ontology graph "]
;  (.clear js/console)
  (if (> @nodes 40)
    ;;Dude. No spaghetti here. This graph would be massive.
    [no-spaghetti-graphs-please @nodes]
    ;;ok it's a small graph. Let's render.
   [:div
    [:svg#lineplacer
      [:path#jango ;;we clone this element lots.
        {:style {:stroke-width 2 :fill "transparent"} :d ""}]]
    [graph tree nil]]
))])
