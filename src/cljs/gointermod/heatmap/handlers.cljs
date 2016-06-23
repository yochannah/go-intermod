(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.utils.utils :as utils]
              [gointermod.db :as db]))

(defn max-key-orthologs
  "Helper for get-ortholog-count-max. It's abstruse enough that splitting it into two was the only way. Given an ortholog with a set of go term counts, we'll figure out which has the highest cont and return it.
  Returns: we're returning a vector consisting of the keys as the first value and the max-count as the second value."
  [organism orthologs]

  (map (fn [[orthk terms]]
    (let [termk (apply max-key val terms)]
      [[organism orthk (key termk)] (val termk)])
) orthologs))

(defn get-ortholog-count-max
  "Given a map in the shape {:organism {:ortholog {:some-go-term count-of-that-go term}}}, return a set of keys which represent the highest nested count."
  [results]

  (apply max-key
    ;;use orthologue as the key for this level
    (fn [[organism orthologs]] orthologs)
    ;;map over the orthologues and return the max key for the go terms inside
    (map (fn [[organism orthologs]]
      (apply max-key last (max-key-orthologs organism orthologs))
) results)))


(defn merge-results
  "merges results from all organisms into one big fat map, and filters out the other two go branches"
  [results go-branch]

  (sort-by :go-term      ;;sort by GO term
    (filter (fn [result] ;;only return branches we care about
      (= (:ontology-branch result) go-branch))
    (apply concat (map (fn [[_ organism]] ;;merge it all
organism) results)))))

(defn extract-go-terms
  "just an ordered array of terms, thanks"
  [results]

  (distinct (into [] (map (fn [ result]
      (:go-term result)
  ) results))))

(defn orthologue-key [result aggregate-orthologs? orthologue-count]
  (let [organism-id (utils/organism-name-to-id (:ortho-organism result))]
    [(:ortho-organism result)
     (if aggregate-orthologs?
       (if (= 1 (organism-id orthologue-count))
          (:display-ortholog-id result)
          (organism-id orthologue-count))
       (:display-ortholog-id result))
]))

(defn aggregate-orthologs
  "create map with keys organism>orthologue>go term, with counts as values for the heatmap."
  ([results aggregate-orthologs?]
    (let [orthologue-counts (re-frame/subscribe [:ortholog-count])]
      (reduce (fn [new-map result]
        (let [k (orthologue-key result aggregate-orthologs? @orthologue-counts)]
          (update-in new-map (conj k (:go-term result)) inc)
        )) {} results)))
  ([results] (aggregate-orthologs results true))
  )

(defn map-terms
  "for each term in term list, output the count, or 0 if there is no count.
    return a nice vector."
  [term-list organism-ortholog-terms]

  (map (fn [term]
    (get organism-ortholog-terms term 0)
    ) term-list))

(defn build-result-matrix
  "This is basically a matrix representation of the table we'll output. We can't just use maps because the order is important, and maps can't be trusted."
  [go-terms aggregate-ortholog-counts]
  (apply concat (map (fn [[organism orthologs]]
  (map (fn [[ortholog terms]]
      (concat [organism ortholog] (map-terms go-terms terms))
    ) orthologs)
  ) aggregate-ortholog-counts)))

(defn find-missing-organisms
  "Helps to create rows for organisms that have no orthologues"
  [counts]
  (let [organism-details (re-frame/subscribe [:organisms])
        all-organism-names (reduce
          (fn [new-set [name organism]]
            (conj new-set (:abbrev organism))) #{} @organism-details)
        shown-organism-names (set (keys counts))]
        (clojure.set/difference all-organism-names shown-organism-names)
  ))

(defn extract-results
  "Formats results in aggregated way"
  ([search-results aggregate-orthologs?]
  (let [active-filter (re-frame/subscribe [:active-filter])
        merged-results (merge-results search-results @active-filter)
        go-terms (extract-go-terms merged-results)
        counts (aggregate-orthologs merged-results aggregate-orthologs?)
        final-heatmap-matrix (build-result-matrix go-terms counts)
        max-count (get-ortholog-count-max counts)
        organisms-present (keys counts)
        missing-organisms (find-missing-organisms counts)]
     {:rows final-heatmap-matrix
      :headers go-terms
      :max-count max-count
      :aggregate-results counts
      :missing-organisms missing-organisms
      :all-results merged-results}
    ))
  ([search-results] (extract-results search-results true))
  )

(defn expand-organism-to-ortholog-level
  "helper to expand the data for a specific organism from organism level aggregation to go-term level aggregation.
  Returns the ortholog-level results for the organism"
  [db organism]
  (let [active-filter (re-frame/subscribe [:active-filter])
        go-terms (:headers (:heatmap db))
        aggregate-results (:aggregate-results (:heatmap db))]
    (first (vals (aggregate-orthologs (organism (:multi-mine-results db)) true)))))


(re-frame/register-handler
 ;;convert one organism's result set to the expanded view. No more than one due to performance.
  :expand-heatmap
  (fn [db [_ organism]]
    (let [org-count (expand-organism-to-ortholog-level db organism)
          org-name (utils/get-abbrev organism)
          new-db (assoc-in db [:heatmap :aggregate-results org-name] org-count)
          ;;if we wanted to allow more than one "open" result set, we'd simply assoc this too.
          new-aggregate (:aggregate-results (:heatmap new-db))
          go-terms (:headers (:heatmap db))
          max-count (get-ortholog-count-max new-aggregate)]
    ;  (.log js/console "%chi" "color:hotpink;font-weight:bold;" (clj->js org-count))
    (->
      (assoc-in db [:heatmap :rows] (build-result-matrix go-terms new-aggregate))
      (assoc-in [:heatmap :max-count] max-count)
))))

(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    (->
     ;;visual results aren't expanded for multiple orthologs
      (assoc db :heatmap (extract-results (:multi-mine-results db)))
     ;;csv export results are expanded because they can't be interactive with expand-on-click like the webapp results can be! :)
      (assoc :heatmap-csv (extract-results (:multi-mine-results db) false)))
    ))
