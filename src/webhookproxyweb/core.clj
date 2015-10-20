(ns webhookproxyweb.core
  (:require [webhookproxyweb.system :as system]))

(defn -main [& args]
  (println "Starting")
  (let [system (system/main-system)
        started-system (system/start system)]
    (.addShutdownHook (Runtime/getRuntime) 
                      (Thread. (fn [] 
                                 (println "Shutting down")
                                 (system/stop started-system)
                                 (println "Shut down")
                                 )))
    (println "Started")))

