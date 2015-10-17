(defproject webhookproxyweb "0.1.0-SNAPSHOT"
  :description "Frontend for webhookproxy"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.1"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
              :repl-listen-port 9000
              :builds [{
                        ; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                        ; The standard ClojureScript compiler options:
                        ; (See the ClojureScript compiler documentation for details.)
                        :figwheel { :on-jsload "webhookproxyweb.core/root-render" }
                        :compiler {
                                   :output-dir "resources/public/js"  ; default: target/cljsbuild-main.js
                                   :output-to "resources/public/js/app/main.js"  ; default: target/cljsbuild-main.js
                                   :optimizations :none
                                   :pretty-print true}}]}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [re-frame "0.4.1"]
                 [reagent-forms "0.5.12"]
                 [prismatic/schema "1.0.1"]
                 ])
