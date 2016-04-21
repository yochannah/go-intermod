(ns gointermod.utils.comms
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [cljs-http.client :as http]
          [re-frame.core :as re-frame]
          [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn get-service [source]
(let [mines (re-frame/subscribe [:organisms])
        service (:service (:mine (source @mines)))]
(clj->js service)))

(defn make-base-query [identifier]
  (str "<query model=\"genomic\" view=\"Gene.symbol Gene.secondaryIdentifier Gene.primaryIdentifier Gene.organism.name Gene.organism.taxonId Gene.homologues.homologue.primaryIdentifier Gene.homologues.homologue.secondaryIdentifier Gene.homologues.homologue.symbol Gene.homologues.homologue.organism.name Gene.homologues.homologue.organism.taxonId Gene.homologues.dataSets.name Gene.homologues.dataSets.url Gene.goAnnotation.evidence.code.code Gene.goAnnotation.evidence.publications.pubMedId Gene.goAnnotation.evidence.publications.title Gene.goAnnotation.ontologyTerm.identifier Gene.goAnnotation.ontologyTerm.name Gene.goAnnotation.ontologyTerm.namespace\" sortOrder=\"Gene.symbol ASC\" constraintLogic=\"B and C and A\" name=\"intermod_go\" > <constraint path=\"Gene.goAnnotation.qualifier\" op=\"IS NULL\" code=\"B\" />  <constraint path=\"Gene.goAnnotation.ontologyTerm.obsolete\" op=\"=\" value=\"false\" code=\"C\" />
<constraint path=\"Gene.homologues.homologue.symbol\" op=\"=\" value=\"" identifier "\" code=\"A\" /></query>"))

(defn go-query
  "Get the results of GO term query for specified symbol/identifier"
  [organism identifier]
  (.log js/console identifier)
  (let [service (get-service organism)
        query (make-base-query identifier)]
    (go (let [response (<! (http/post (str "http://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "json"}}))]
            (js->clj (-> response :body))
))))
