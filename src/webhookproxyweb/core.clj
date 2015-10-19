(ns webhookproxyweb.core
  (:require [com.stuartsierra.component :as component])
  (:require [webhookproxyweb.system :as system]))

(defn -main [& args]
  (let [system (system/main-system {})
        started-system (component/start system)]
    (println "YEAH")))
