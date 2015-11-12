(defproject webhookproxyweb "0.1.0-SNAPSHOT"
  :description "Frontend for webhookproxy SaaS"
  :url "https://www.webhookproxy.com"
  :license {:name "MIT "
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.4.1"]]
  :cljsbuild {
              :repl-listen-port 9000
              :builds [{
                        :source-paths ["src-cljs" "src-cljc"]
                        :figwheel { :on-jsload "webhookproxyweb.core/root-render" }
                        :compiler {
                                   :output-dir "resources/public/js"  
                                   :output-to "resources/public/js/app/main.js"  
                                   :optimizations :none
                                   :pretty-print true}}]}
  :source-paths ["src" "src-cljc"]
  :main webhookproxyweb.core
  :repl-options { :init-ns webhookproxyweb.repl }
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.2.371"]
                 [slamhound "1.5.5"]
                 [com.taoensso/sente "1.6.0"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [com.stuartsierra/component "0.3.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [figwheel-sidecar "0.4.1"]
                 [cljs-ajax "0.5.0"]
                 [korma "0.4.2"]
                 [ragtime "0.5.2"]
                 [clj-time "0.11.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [compojure "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-mock "0.3.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.postgresql/postgresql "9.3-1104-jdbc41"]
                 [re-frame "0.4.1"]
                 [reagent-forms "0.5.12"]
                 [venantius/accountant "0.1.1"]
                 [prismatic/schema "1.0.2"]
                 [secretary "1.2.3"]
                 [midje "1.7.0"]
                 [spyscope "0.1.5"]
                 ])
