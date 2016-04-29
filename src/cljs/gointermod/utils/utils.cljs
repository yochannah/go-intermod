(ns gointermod.utils.utils)

(defn get-id [primary secondary symbol organism]
  "returns first non-null identifier, preferring symbol or primary id"
  (if (= organism "S. cerevisiae")
    ;;if it's yeast, we want secondary identifier first
    (first (remove nil? [secondary symbol primary]))
    ;;else we want the symbol
    (first (remove nil? [symbol secondary primary]))
  ))
