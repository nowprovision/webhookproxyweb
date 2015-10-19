(ns webhookproxyweb.webserver
  (:require [org.httpkit.server :as http-kit])
  (:require [com.stuartsierra.component :as component]))

(defrecord WebServer [handler port] 
  component/Lifecycle
  (start [component]
    (let [stopfn (http-kit/run-server (:handler handler) { :port port } )]
      (assoc component :stopfn stopfn)))
  (stop [component]
    (if-let [stopfn (:stopfn component)]
      (do
        (stopfn)
      component))))

