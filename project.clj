(defproject webhookproxyweb "0.1.0-SNAPSHOT"
  :description "Frontend for webhookproxy SaaS"
  :url "https://www.webhookproxy.com"
  :license {:name "MIT "
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0"]]
  :cljsbuild {:repl-listen-port 9000
              :builds {:dev {:source-paths ["src-cljs" "src-cljc"]
                              :figwheel {:on-jsload "webhookproxyweb.core/root-render" }
                              :compiler {:output-dir "resources/public/js"  
                                         :output-to "resources/public/js/app/main.debug.js"  
                                         :optimizations :none
                                         :pretty-print true}}
                       :prod {:source-paths ["src-cljs" "src-cljc"]
                              :compiler {:output-to "resources/public/js/app/main.js"
                                        :optimizations :advanced
                                        :pretty-print false
                                         }}}}
  :source-paths ["src" "src-cljc"]
  :main webhookproxyweb.core
  :repl-options {:init-ns webhookproxyweb.repl }
  :profiles {:repl {:dependencies [[ragtime "0.5.2"]] } 
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]] } }
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.371"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.1"]
                 [org.postgresql/postgresql "9.3-1104-jdbc41"]
                 [com.stuartsierra/component "0.3.0"]
                 [prismatic/schema "1.0.2"]
                 [com.taoensso/timbre "4.1.4"]
                 [yesql "0.5.1"]
                 [danlentz/clj-uuid "0.1.6"]
                 [clj-http "2.0.0"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]
                 [compojure "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-mock "0.3.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [http-kit "2.1.18"]
                 ;clojurescript dependencies 
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [cljs-ajax "0.5.0"]
                 [re-frame "0.4.1"]
                 [reagent-forms "0.5.12"]
                 [secretary "1.2.3"]])
