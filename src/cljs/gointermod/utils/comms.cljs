(ns gointermod.utils.comms
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [cljs-http.client :as http]
          [gointermod.utils.utils :as utils]
          [re-frame.core :as re-frame]
          [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defn get-service [source]
(let [mines (re-frame/subscribe [:organisms])
        service (:service (:mine (source @mines)))]
(clj->js service)))

(defn make-base-query [identifier organism evidence-codes]
  (str "<query model=\"genomic\" view=\"Gene.id Gene.symbol Gene.secondaryIdentifier Gene.primaryIdentifier Gene.organism.shortName Gene.organism.taxonId Gene.homologues.homologue.id Gene.homologues.homologue.primaryIdentifier Gene.homologues.homologue.secondaryIdentifier Gene.homologues.homologue.symbol Gene.homologues.homologue.organism.shortName Gene.homologues.homologue.organism.taxonId Gene.homologues.dataSets.name Gene.homologues.dataSets.url Gene.goAnnotation.evidence.code.code Gene.goAnnotation.evidence.publications.pubMedId Gene.goAnnotation.evidence.publications.title Gene.goAnnotation.ontologyTerm.identifier Gene.goAnnotation.ontologyTerm.name Gene.goAnnotation.ontologyTerm.namespace\" sortOrder=\"Gene.symbol ASC\" constraintLogic=\"B and C and A and E\" name=\"intermod_go\" >
    <constraint path=\"Gene.goAnnotation.qualifier\" op=\"IS NULL\" code=\"B\" />
    <constraint path=\"Gene.goAnnotation.ontologyTerm.obsolete\" op=\"=\" value=\"false\" code=\"C\" />
    <constraint path=\"Gene.homologues.homologue\" code=\"A\" op=\"LOOKUP\" value=\"" identifier "\" extraValue=\"" (utils/get-abbrev organism) "\"/>
    <constraint path=\"Gene.goAnnotation.evidence.code.code\" op=\"ONE OF\" code=\"E\">" evidence-codes "</constraint>
</query>"))

(defn create-constraint-values [values]
  (reduce (fn [new-str value]
    (str new-str "<value>" value "</value>")
) "" values))

(defn go-query
  "Get the results of GO term query for specified symbol/identifier"
  [input-organism identifiers output-organism]
;  (.log js/console "%cgo-query. Input %s, output %s" (clj->js input-organism) (clj->js output-organism) output-organism)
  (let [service (get-service output-organism)
        evidence-codes (re-frame/subscribe [:active-evidence-codes])
        evidence-code-constraint-values (create-constraint-values @evidence-codes)
        query (make-base-query identifiers output-organism evidence-code-constraint-values)]
    (go (let [response (<! (http/post (str "http://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "json"}}))]
            (js->clj (-> response :body))
))))

(defn query-all-selected-organisms [input-organism identifiers]
  "query all organisms that are selected as an output species in the search bar"
  (let [output-organisms (re-frame/subscribe [:organisms])]
    (doall (map (fn [[output-organism vals] stuff]
      (cond
        ;;if the result is checked
        (:output? vals)
        ;;query for it
        (go (let [res(<! (go-query input-organism identifiers output-organism))]
          (re-frame/dispatch [:concat-results res output-organism])
)))) @output-organisms))))

(defn resolve-ids
  "Completes the steps required to resolve identifiers.
  1. Start an ID Resolution job.
  2. Poll the server for the job status (every 1s)
  3. Delete the job (side effect).
  4. Return results"
  [source input]
  (.log js/console (clj->js source) (clj->js input))
  (go (let [root (.-root (get-service source))
            response (<! (http/post (str "http://" root "/service/ids")
                                    {:with-credentials? false
                                     :json-params (clj->js input)}))]
        (if-let [uid (-> response :body :uid)]
          (loop []
            (let [status-response (<! (http/get (str "http://" root "/service/ids/" uid "/status")
                                                {:with-credentials? false}))]
              (if (= "SUCCESS" (:status (:body status-response)))
                (let [final-response (<! (http/get (str "http://" root "/service/ids/" uid "/results")
                                                   {:with-credentials? false}))]
                  (http/delete (str "http://" root "/service/ids/" uid)
                               {:with-credentials? false})
                  final-response)
                (do
                  (<! (timeout 1000))
                  (recur)))))))))


  (defn resolve-id
    "Resolves an ID or set of IDs from Intermine."
    [source input]
      (go (let [res (<! (resolve-ids
         source
         {:identifiers (if (string? input) [input] input)
          :type "Gene"
          :caseSensitive false
          :wildCards true
          :extra (utils/get-abbrev source)}))]
    (-> res :body :results))))

    (defn enrichment
      "Get the results of using a list enrichment widget to calculate statistics for a set of objects."
      [ {{:keys [root token]} :service} {:keys [ids widget maxp correction]}]
      (println "ids" ids)
      (go (let [response (<! (http/post (str "http://" root "/service/list/enrichment")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params (merge
                       {:widget widget
                        :maxp maxp
                        :format "json"
                        :correction correction}
                        {:ids (clojure.string/join "," ids)}


            )}))]
            (-> response :body))))
