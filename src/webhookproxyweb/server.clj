(ns webhookproxyweb.server
  (:require [webhookproxyweb.web :as web])
  (:require [org.httpkit.server :as http-kit])
  (:require [com.stuartsierra.component :as component]))

(defrecord HttpKitServer [web-app port] 
  component/Lifecycle
  (start [component]
    (let [port (or port 8080)]
      (let [stopfn (http-kit/run-server (web/handler web-app) 
                                        {:port port} )]
        (assoc component :stopfn stopfn))))
  (stop [component]
    (if-let [stopfn (:stopfn component)]
      (do
        (stopfn)
      component))))

