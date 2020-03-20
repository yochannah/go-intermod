(defproject gointermod "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [json-html "0.4.7"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.10.0"]
                 [org.clojure/core.async "1.0.567"]
                 [cljs-http "0.1.46"]
                 [secretary "1.2.3"]
                 [re-frame "0.12.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.19"]
            [lein-less "1.7.5"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}


  :less {:source-paths ["less"]
         :target-path  "resources/public/css/compiled"}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "gointermod.core/mount-root"}
                        :compiler {:main gointermod.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true}}

                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main gointermod.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}]})
