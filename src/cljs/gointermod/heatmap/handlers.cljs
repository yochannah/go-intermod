(ns gointermod.heatmap.handlers
    (:require [re-frame.core :as re-frame]
              [gointermod.utils.utils :as utils]
              [gointermod.db :as db]))

(defn max-key-orthologs [organism orthologs]
  "Helper for get-ortholog-count-max. It's abstruse enough that splitting it into two was the only way. Given an ortholog with a set of go term counts, we'll figure out which has the highest cont and return it.
  Returns: we're returning a vector consisting of the keys as the first value and the max-count as the second value."
  (map (fn [[orthk terms]]
    (let [termk (apply max-key val terms)]
      [[organism orthk (key termk)] (val termk)])
) orthologs))

(defn get-ortholog-count-max [results]
  "Given a map in the shape {:organism {:ortholog {:some-go-term count-of-that-go term}}}, return a set of keys which represent the highest nested count."
  (apply max-key
    ;;use orthologue as the key for this level
    (fn [[organism orthologs]] orthologs)
    ;;map over the orthologues and return the max key for the go terms inside
    (map (fn [[organism orthologs]]
      (apply max-key last (max-key-orthologs organism orthologs))
) results)))


(defn merge-results [results go-branch]
  "merges results from all organisms into one big fat map, and filters out the other two go branches"
  (filter
    (fn [result]  (= (last result) go-branch))
    (apply concat (map (fn [[_ organism]]
      (:results organism)
  ) results))))

(defn map-results [results]
  "translate that silly vector of results into a map with meaninful keys"
  ;;TODO: ortholog needs to be get-id with yeast conditionals. Boo.
  (map (fn [result]
    {:results result
     :organism (get result 3)
     :go-id (get result 15)
     :go-term (get result 16)
     :ortholog (utils/get-id result)
     }
  ) results))

(defn extract-go-terms [results]
  "just an ordered array of terms, thanks"
  (distinct (into [] (map (fn [ result]
      (:go-term result)
  ) results))))

(defn aggregate-orthologs [results]
  "create map with keys organism>orthologue>go term, with counts as values for the heatmap."
  (reduce (fn [new-map result]
      (update-in new-map
        [(:organism result)
         (:ortholog result)
         (:go-term result)] inc)
    ) {} results)
  )

(defn map-terms [term-list organism-ortholog-terms]
  "for each term in term list, output the count, or 0 if there is no count.
    return a nice vector."
;  (.log js/console (clj->js organism-ortholog-terms))
  (map (fn [term]
    (get organism-ortholog-terms term 0)
    ) term-list))

(defn build-result-matrix [go-terms aggregate-ortholog-counts]
  "This is basically a matrix representation of the table we'll output. We can't just use maps because the order is important, and maps can't be trusted."
  (apply concat (map (fn [[organism orthologs]]
    (map (fn [[ortholog terms]]
      (concat [organism ortholog] (map-terms go-terms terms))
    ) orthologs)
  ) aggregate-ortholog-counts)))

(defn extract-results [search-results]
  "TODO: FIX THAT BIG FAT HARDCODED BIOLOGICAL PROCESS"
  (let [merged-results (merge-results search-results "biological_process")
        map-results (map-results merged-results)
        go-terms (extract-go-terms map-results)
        counts (aggregate-orthologs map-results)
        final-heatmap-matrix (build-result-matrix go-terms counts)
        max-count (get-ortholog-count-max counts)]
;      (.clear js/console)
    {:rows final-heatmap-matrix :headers go-terms :max-count max-count}
    ))



(re-frame/register-handler
  :aggregate-heatmap-results
  (fn [db [_ _]]
    ;(.clear js/console)
    (assoc db :heatmap (extract-results (:multi-mine-results db)))
  ))
