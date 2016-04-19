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
   (array-map
    ;format: "Code name (CODE)" is-checked?
    "Inferred from Experiment (EXP)" true
    "Inferred from Direct Assay (IDA)" true
    "Inferred from Physical Interaction (IPI)" true
    "Inferred from Mutant Phenotype (IMP)" true
    "Inferred from Genetic Interaction (IGI)" true
    "Inferred from Expression Pattern (IEP)" true
    "Inferred from Sequence or structural Similarity (ISS)" false
    "Inferred from Sequence Orthology (ISO)" false
    "Inferred from Sequence Alignment (ISA)" false
    "Inferred from Sequence Model (ISM)" false
    "Inferred from Genomic Context (IGC)" false
    "Inferred from Biological aspect of Ancestor (IBA)" false
    "Inferred from Biological aspect of Descendant (IBD)" false
    "Inferred from Key Residues (IKR)" false
    "Inferred from Rapid Divergence(IRD)" false
    "Inferred from Reviewed Computational Analysis (RCA)" false
    "Traceable Author Statement (TAS)" true
    "Non-traceable Author Statement (NAS)" false
    "Inferred by Curator (IC)" true
    "No biological Data available (ND) evidence code" false
    "Inferred from Electronic Annotation (IEA)" false
    )
})
