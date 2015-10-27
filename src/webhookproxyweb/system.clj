(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component]
            [webhookproxyweb.config :as config]
            ; component dependencies
            [webhookproxyweb.db :refer [map->Database]]
            [webhookproxyweb.domain.users :refer [map->Users]]
            [webhookproxyweb.domain.webhooks :refer [map->WebHooks]]
            [webhookproxyweb.handlers.webhooks :refer [map->WebHookHandlers]]
            [webhookproxyweb.handlers.static :refer [map->StaticHandlers]]
            [webhookproxyweb.handlers.users :refer [map->UserHandlers]]
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
     ;; register handlers
     :webhook-handlers (component/using (map->WebHookHandlers {}) [:webhooks])
     :users-handlers (component/using (map->UserHandlers {}) [:users])
     :static-handlers (map->StaticHandlers { :root-path (-> config :static :root-path) })
     ;; middleware placeholder
     :extra-middleware []
     ;; pass handlers to web-app
     :web-app (component/using (map->WebApp {}) [:extra-middleware
                                                     :webhook-handlers
                                                     :users-handlers
                                                     :static-handlers])
     ;; pass web-app to web server
     :http-server (component/using 
                    (map->HttpKitServer (-> config :http-server))
                    [:web-app])
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



