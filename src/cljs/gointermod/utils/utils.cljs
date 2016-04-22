(ns gointermod.utils.utils)

(defn get-id [primary secondary symbol]
  "returns first non-null identifier, preferring symbol or primary id"
  (first (remove nil? [symbol primary secondary]))
  )
