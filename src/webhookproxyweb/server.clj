(ns webhookproxyweb.server
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as http-kit]
            [webhookproxyweb.web :as web]))

(defrecord HttpKitServer [web-app port] 
  component/Lifecycle
  (start [{:keys [port web-app] :or { port 8080 } :as component}]
    (let [stopfn (http-kit/run-server (web/handler web-app) 
                                      {:port port} )]
      (assoc component :stopfn stopfn)))
  (stop [component]
    (when-let [stopfn (:stopfn component)]
      (stopfn))
    (dissoc component :stopfn)))

