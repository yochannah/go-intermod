(ns gointermod.db)

(def default-db
  {:selected-organism :human
   :are-all-orthologs-selected? true
   :active-view :ortholog-summary
   :active-filter "biological_process"
   :max-p 0.05
   :test-correction "Holm-Bonferroni"
   :filters
     {"biological_process"
       {:pretty-name "Biological Process"
        :icon "#icon-biological-process"}
      "molecular_function"
       {:pretty-name "Molecular Function"
        :icon "#icon-molecular-function"}
      "cellular_component"
       {:pretty-name "Cellular Component"
        :icon "#icon-cellular-component"}
    }
   :organisms
   {:human
     {:id     :human
       :common "Human"
       :output? true
       :abbrev "H. sapiens"
       :status {:status :na}
       :mine
       {:name "HumanMine"
       :url "https://www.humanmine.org/humanmine"
       :service {:root "https://www.humanmine.org/humanmine"}}}
    :fly
     {:id     :fly
      :common "Fly"
      :status {:status :na}
      :output? true
      :abbrev "D. melanogaster"
      :mine
       {:name "FlyMine"
       :url "https://www.flymine.org/flymine"
       :service {:root "https://www.flymine.org/flymine"}}}
    :mouse
     {:id     :mouse
      :common "Mouse"
      :output? true
      :abbrev "M. musculus"
      :status {:status :na}
      :mine
       {:name "MouseMine"
        :url "http://www.mousemine.org/mousemine"
        :service {:root "http://www.mousemine.org/mousemine"}}}
    :rat
     {:id     :rat
      :common "Rat"
      :output? true
      :abbrev "R. norvegicus"
      :status {:status :na}
      :mine
       {:name "RatMine"
        :url "http://ratmine.mcw.edu/ratmine"
        :service {:root "http://ratmine.rgd.mcw.edu/ratmine"}}}
    :zebrafish
     {:id     :zebrafish
      :common "Zebrafish"
      :output? true
      :status {:status :na}
      :abbrev "D. rerio"
      :mine
       {:name "ZebraFishMine"
        :url "http://www.zebrafishmine.org"
        :service {:root "http://www.zebrafishmine.org"}}}
    :worm
     {:id     :worm
      :common "Worm"
      :output? true
      :abbrev "C. elegans"
      :status {:status :na}
      :mine
       {:name "WormMine"
       :url "http://intermine.wormbase.org/tools/wormmine"
       :service {:root "http://intermine.wormbase.org/tools/wormmine"}}}
    :yeast
     {:id     :yeast
      :output? true
      :common "Yeast"
      :abbrev "S. cerevisiae"
      :status {:status :na}
      :mine
       {:name "YeastMine"
       :url "https://yeastmine.yeastgenome.org/yeastmine"
       :service {:root "https://yeastmine.yeastgenome.org/yeastmine"}}}
    }
   :go-ontology {:nodes 0
                 :loading true}
   :evidence-codes
   [
    {:name "Inferred from Experiment (EXP)" :code "EXP" :checked true}
    {:name "Inferred from Direct Assay (IDA)" :code "IDA" :checked true}
    {:name "Inferred from Physical Interaction (IPI)"  :code "IPI" :checked true}
    {:name "Inferred from Mutant Phenotype (IMP)"  :code "IMP" :checked true}
    {:name "Inferred from Genetic Interaction (IGI)"  :code "IGI" :checked true}
    {:name "Inferred from Expression Pattern (IEP)"  :code "IEP" :checked true}
    {:name "Inferred from Sequence or structural Similarity (ISS)"  :code "ISS" :checked  false}
    {:name "Inferred from Sequence Orthology (ISO)"  :code "ISO" :checked false}
    {:name "Inferred from Sequence Alignment (ISA)"  :code "ISA" :checked false}
    {:name "Inferred from Sequence Model (ISM)"  :code "ISM" :checked false}
    {:name "Inferred from Genomic Context (IGC)"  :code "IGC" :checked false}
    {:name "Inferred from Biological aspect of Ancestor (IBA)" :code "IBA" :checked false}
    {:name "Inferred from Biological aspect of Descendant (IBD)" :code "IBD" :checked false}
    {:name "Inferred from Key Residues (IKR)" :code "IKR" :checked false}
    {:name "Inferred from Rapid Divergence(IRD)" :code "IRD" :checked false}
    {:name "Inferred from Reviewed Computational Analysis (RCA)" :code "RCA" :checked false}
    {:name "Traceable Author Statement (TAS)" :code "TAS" :checked true}
    {:name "Non-traceable Author Statement (NAS)" :code "NAS" :checked false}
    {:name "Inferred by Curator (IC)" :code "IC" :checked true}
    {:name "No biological Data available (ND) evidence code" :checked false :code "ND"}
    {:name "Inferred from Electronic Annotation (IEA)" :checked false :code "IEA"}
    ]
   :expand-evidence-codes? false
   :ontology-graph-max-limit 150
   :heatmap-expansion {}
})
