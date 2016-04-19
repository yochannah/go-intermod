(ns go-intermod.db)

(def default-db
  {:name "intermod"
   :organisms
   {:human
     {:id     :human
       :common "Human"
       :abbrev "H. sapiens"}
    :fly
     {:id     :fly
      :common "Fly"
      :abbrev "D. melanogaster"}
    :mouse
     {:id     :mouse
      :common "Mouse"
      :abbrev "M. musculus"}
    :rat
     {:id     :rat
      :common "Rat"
      :abbrev "R. norvegicus"}
    :zebrafish
     {:id     :zebrafish
      :common "Zebrafish"
      :abbrev "D. rerio"}
    :worm
     {:id     :worm
      :common "Worm"
      :abbrev "C. elegans"}
    :yeast
     {:id     :yeast
      :common "Yeast"
      :abbrev "S. cerevisiae"}}
   :evidence-codes
   [
    ;format: "Code name (CODE)" is-checked?
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
})
