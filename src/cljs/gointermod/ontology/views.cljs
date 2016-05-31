(ns gointermod.ontology.views
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [re-frame.core :as re-frame]
      [gointermod.utils.utils :as utils]
      [reagent.core :as reagent]
      [gointermod.utils.comms :as comms]
      [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn make-id [thenaughtystring] (clojure.string/escape thenaughtystring {" " "_"}))
(defn elem [id] (.getElementById js/document (make-id id)))


(defn organism-node [term vals]
  (into [:div.title term]
   (map (fn [[organism results]]
      [:div.organism
       {:key (gensym) :class (clj->js organism) :id (make-id term)}
       (utils/get-abbrev organism) " " (reduce (fn [_ result] (:display-ortholog-id result)) [] results)]
) vals)))

;;pixelcalcs
(defn x-offset [] (aget js/document "body" "scrollTop"))
(defn get-middle-x [box] (- (+ (.-left js/box) (/ (.-width js/box) 2)) 272 ))
(defn get-middle-y-top [box] (+ (- (.-top js/box) 224) (x-offset)) )
(defn get-middle-y-bottom [box] (+ (- (.-bottom js/box) 224) (x-offset)))

(def jango (elem "jango")) (def lineplacer (elem "lineplacer"))
(defn clonepath []
  (let [boba (.cloneNode js/jango)]
      (.removeAttribute js/boba "id")
      (.appendChild js/lineplacer boba)
  boba))

(defn drawline [parent child]
  (let [parent-size (.getBoundingClientRect js/parent)
        child-size (.getBoundingClientRect js/child)
        start-x (get-middle-x parent-size)
        start-y (get-middle-y-bottom parent-size)
        end-x (get-middle-x child-size)
        end-y (get-middle-y-top child-size)
        d (str "M " start-x " " start-y " S " start-x " " start-y ", "  end-x " " end-y)
        path (clonepath)]

;    (.log js/console "path" path "d" d)
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
                  [organism-node k (:results v)]
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
