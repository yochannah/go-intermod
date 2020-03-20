(ns gointermod.utils.comms
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [cljs-http.client :as http]
          [gointermod.utils.utils :as utils]
          [re-frame.core :as re-frame]
          [cljs.core.async :refer [put! chan <! >! timeout close!]]))

;; THIS WHOLE FILE COULD REALLY USE SOME REFACTORING

(defn get-service [source]
(let [mines (re-frame/subscribe [:organisms])
        service (:service (:mine (source @mines)))]
(clj->js service)))

(defn make-ontology-query [ids]
  (let [namespace (re-frame/subscribe [:active-filter])]
  (str "<query model=\"genomic\" view=\"GOTerm.identifier GOTerm.name GOTerm.parents.identifier GOTerm.parents.name  GOTerm.parents.parents.identifier GOTerm.parents.parents.name\" sortOrder=\"GOTerm.parents.parents.name ASC\"  constraintLogic=\"A and B\"><constraint path=\"GOTerm.identifier\" op=\"ONE OF\" code=\"A\">" ids "</constraint><constraint path=\"GOTerm.namespace\" code=\"B\" op=\"=\" value=\"" @namespace "\"/></query>")))

(defn create-constraint-values [values]
  (reduce (fn [new-str value]
    (str new-str "<value>" value "</value>")
) "" values))

(defn ontology-query
  "Get the results of GO term query for specified symbol/identifier"
  [organism identifiers]
  (let [service (get-service organism)
        ids (create-constraint-values identifiers)
        query (make-ontology-query ids)]
    (go (let [response (<! (http/post (str "https://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "jsonobjects"}}))]
            (js->clj (-> response :body))
))))

(defn ontology-query-all-organisms [identifiers]
  "query all organisms that are selected as an output species in the search bar"
    (let [organisms (re-frame/subscribe [:organisms])]
    (doall (map (fn [[organism vals]]
      (cond (> (count vals) 0)
        ;;query for it
        (go (let [res(<! (ontology-query organism vals))]
          (re-frame/dispatch [:concat-ontology-results res organism])))
      )
) identifiers))))

(defn make-base-query [identifier organism evidence-codes]
  (str "<query model=\"genomic\" view=\"Gene.id Gene.symbol Gene.secondaryIdentifier Gene.primaryIdentifier Gene.organism.shortName Gene.organism.taxonId Gene.homologues.homologue.id Gene.homologues.homologue.primaryIdentifier Gene.homologues.homologue.secondaryIdentifier Gene.homologues.homologue.symbol Gene.homologues.homologue.organism.shortName Gene.homologues.homologue.organism.taxonId Gene.homologues.dataSets.name Gene.homologues.dataSets.url Gene.goAnnotation.evidence.code.code Gene.goAnnotation.ontologyTerm.identifier Gene.goAnnotation.ontologyTerm.name Gene.goAnnotation.ontologyTerm.namespace\" sortOrder=\"Gene.symbol ASC\" constraintLogic=\"B and C and D and E and F and A\" name=\"intermod_go\" >
  <join path=\"Gene.goAnnotation\" style=\"OUTER\"/>
  <constraint path=\"Gene.goAnnotation.qualifier\" op=\"IS NULL\" code=\"B\" />
  <constraint path=\"Gene.goAnnotation.ontologyTerm.obsolete\" op=\"=\" value=\"false\" code=\"C\" />
  <constraint path=\"Gene.homologues.homologue\" code=\"A\" op=\"LOOKUP\" value=\"" identifier "\" extraValue=\"H. sapiens\"/>
  <constraint path=\"Gene.organism.shortName\" code=\"D\" op=\"=\" value=\"" (utils/get-abbrev organism) "\"/>
  <constraint path=\"Gene.goAnnotation.evidence.code.code\" op=\"ONE OF\" code=\"E\">" evidence-codes "</constraint>
  <constraint path=\"Gene.homologues.type\" code=\"F\" op=\"!=\" value=\"paralogue\"/>
</query>"))

(defn human-ortholog-query [identifier organism]
  (str "<query model=\"genomic\" view=\"Gene.primaryIdentifier Gene.homologues.dataSets.name\" sortOrder=\"Gene.id ASC\" constraintLogic=\"A and B\" name=\"intermod_go\" >
  <constraint path=\"Gene.homologues.homologue\" code=\"A\" op=\"LOOKUP\" value=\"" identifier "\" extraValue=\"" (utils/get-abbrev organism) "\"/>
  <constraint path=\"Gene.organism.shortName\" code=\"B\" op=\"=\" value=\"H. sapiens\"/>
</query>"))

(defn get-human-orthologs
  "Get the results of GO term query for specified symbol/identifier"
  [identifiers input-organism]
  (let [service (get-service input-organism)
        query (human-ortholog-query identifiers input-organism)]
    ;(re-frame/dispatch [:save-query query output-organism])
    (go (let [response (<! (http/post (str "https://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "json"}}))]
            (js->clj (-> response :body))
))))


(defn original-gene-query-text [identifiers evidence-codes]
  ;;ok so I can't for the life of me find a neat way to return all of the annotations for the input gene in the orginal query without adding an ugly constraint and getting all of the orthologues back again. So we query it separately.
  (str "<query model=\"genomic\" view=\"Gene.id Gene.symbol Gene.secondaryIdentifier Gene.primaryIdentifier Gene.organism.shortName Gene.organism.taxonId Gene.goAnnotation.evidence.code.code Gene.goAnnotation.ontologyTerm.identifier Gene.goAnnotation.ontologyTerm.name Gene.goAnnotation.ontologyTerm.namespace\" sortOrder=\"Gene.symbol ASC\" constraintLogic=\"B and C and D and E and F and A\" name=\"intermod_go\" >
  <join path=\"Gene.goAnnotation\" style=\"OUTER\"/>
  <constraint path=\"Gene.goAnnotation.qualifier\" op=\"IS NULL\" code=\"B\" />
  <constraint path=\"Gene.goAnnotation.ontologyTerm.obsolete\" op=\"=\" value=\"false\" code=\"C\" />
  <constraint path=\"Gene\" code=\"A\" op=\"LOOKUP\" value=\"" identifiers "\" extraValue=\"H. sapiens\"/>
  <constraint path=\"Gene.goAnnotation.evidence.code.code\" op=\"ONE OF\" code=\"E\">" evidence-codes "</constraint>
</query>"))

(defn original-gene-query
  "Get the results of GO term query for specified symbol/identifier"
  [identifiers]
  (let [service (get-service :human)
        evidence-codes (re-frame/subscribe [:active-evidence-codes])
        evidence-code-constraint-values (create-constraint-values @evidence-codes)
        query (original-gene-query-text identifiers evidence-code-constraint-values)]
    (re-frame/dispatch [:save-query query :human]);;todo this line will overwrite the other human query.
    (go (let [response (<! (http/post (str "https://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "json"}}))]
            (js->clj (-> response :body))
))))

(defn go-query
  "Get the results of GO term query for specified symbol/identifier"
  [input-organism identifiers output-organism]
  (let [service (get-service output-organism)
        evidence-codes (re-frame/subscribe [:active-evidence-codes])
        evidence-code-constraint-values (create-constraint-values @evidence-codes)
        query (make-base-query identifiers output-organism evidence-code-constraint-values)]
    (re-frame/dispatch [:save-query query output-organism])
    (go (let [response (<! (http/post (str "https://" (.-root service) "/service/query/results")
       {:with-credentials? false
        :keywordize-keys? true
        :form-params
        {:query query
         :format "json"}}))]
            (js->clj (-> response :body))
))))

(defn query-all-selected-organisms [input-organism identifiers]
  "query all organisms that are selected as an output species in the search bar"
  (let [output-organisms (re-frame/subscribe [:checked-organisms])]
    (doall (map (fn [[output-organism vals] stuff]
      ;;query for it
      (go (let [res (<! (go-query input-organism identifiers output-organism))]
      (cond (= output-organism :human)

         (re-frame/dispatch [:concat-original-genes (<! (original-gene-query  identifiers ))]))
        (re-frame/dispatch [:concat-results res output-organism])
))) @output-organisms))))

(defn resolve-ids
  "Completes the steps required to resolve identifiers.
  1. Start an ID Resolution job.
  2. Poll the server for the job status (every 1s)
  3. Delete the job (side effect).
  4. Return results"
  [source input]
  ;(.log js/console (clj->js source) (clj->js input))
  (go (let [root (.-root (get-service source))
            response (<! (http/post (str "https://" root "/service/ids")
                            {:with-credentials? false
                              :json-params (clj->js input)}))]
        (if-let [uid (-> response :body :uid)]
          (loop []
            (let [status-response (<! (http/get (str "https://" root "/service/ids/" uid "/status")
                                                {:with-credentials? false}))]
              (if (= "SUCCESS" (:status (:body status-response)))
                (let [final-response (<! (http/get (str "https://" root "/service/ids/" uid "/results")
                                                   {:with-credentials? false}))]
                  (http/delete (str "https://" root "/service/ids/" uid)
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
  [ {{:keys [root token]} :service} {:keys [ids widget maxp correction filter]}]
  (go (let [response (<! (http/post (str "https://" root "/service/list/enrichment")
   {:with-credentials? false
    :keywordize-keys? true
    :form-params (merge
     {:widget widget
      :maxp maxp
      :filter filter
      :format "json"
      :correction correction}
      {:ids (clojure.string/join "," ids)}
  )}))]
(-> response :body))))
