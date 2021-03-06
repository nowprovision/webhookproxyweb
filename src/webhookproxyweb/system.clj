(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [webhookproxyweb.config :as config]
            ; component dependencies
            [webhookproxyweb.db :refer [map->Database]]
            [webhookproxyweb.domain.users :refer [map->Users]]
            [webhookproxyweb.domain.webhooks :refer [map->WebHooks]]
            [webhookproxyweb.handlers.webhooks :refer [map->WebHookHandlers]]
            [webhookproxyweb.handlers.core :refer [map->CoreHandlers]]
            [webhookproxyweb.middleware.error :refer [map->ErrorRouter]]
            [webhookproxyweb.external.github :refer [map->GithubConnectionManager]]
            [webhookproxyweb.web :refer [map->WebApp]]
            [webhookproxyweb.server :refer [map->HttpKitServer]]))


(defn main-system 
  ([] (main-system (config/edn->config "config.edn")))
  ([config]
   (component/system-map 
     ;; simple config map
     :config config
     ;; infrastructure, external, db, outer onion, outer spaces services
     :gh (map->GithubConnectionManager {:config (:github-auth config)})
     :db (map->Database { :db-spec (-> config :db) }) 
     ;; domain services, controllers, integrators whatever the fuck its call nowadays
     :users (component/using (map->Users {}) [:db :gh])
     :webhooks (component/using (map->WebHooks {}) [:db])
     ;; error router for req
     :error-router (component/using (map->ErrorRouter {:error-file (io/file
                                                        (-> config :static :root-path)            
                                                        "error.html") }) [])
     ;; register handlers
     :webhook-handlers (component/using (map->WebHookHandlers {}) [:webhooks])
     :core-handlers (component/using
                        (map->CoreHandlers {:root-path (-> config :static :root-path)
                                            :debug? (or (-> config :debug) false) })
                        [:users])
     ;; middleware placeholder
     :extra-middleware []
     ;; pass handlers to web-app
     :web-app (component/using (map->WebApp {}) [:extra-middleware
                                                 :error-router
                                                 :webhook-handlers
                                                 :core-handlers ])
     ;; pass web-app to web server
     :http-server (component/using 
                    (map->HttpKitServer (-> config :http-server))
                    [:web-app])
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



