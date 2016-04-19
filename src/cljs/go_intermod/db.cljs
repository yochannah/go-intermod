(ns go-intermod.db)

(def default-db
  {:name "intermod"
   :species
   {:human
     {:id     :human
       :common "Human"
       :abbrev "H. sapiens"}}
    {:fly
     {:id     :fly
      :common "Fly"
      :abbrev "D. melanogaster"}}
    {:mouse
     {:id     :mouse
      :common "Mouse"
      :abbrev "M. musculus"}}
    {:rat
     {:id     :rat
      :common "Rat"
      :abbrev "R. norvegicus"}}
    {:zebrafish
     {:id     :zebrafish
      :common "Zebrafish"
      :abbrev "D. rerio"}}
    {:worm
     {:id     :worm
      :common "Worm"
      :abbrev "C. elegans"}}
    {:yeast
     {:id     :yeast
      :common "Yeast"
      :abbrev "S. cerevisiae"}}
})
