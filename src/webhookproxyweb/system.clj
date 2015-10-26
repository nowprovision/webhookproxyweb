(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component]
            [webhookproxyweb.config :as config]
            [webhookproxyweb.db :as db]
            [webhookproxyweb.domain.users :as users]
            [webhookproxyweb.domain.webhooks :as webhooks]
            [webhookproxyweb.handlers.webhooks :refer [map->WebHookHandlers]]
            [webhookproxyweb.handlers.static :refer [map->StaticHandlers]]
            [webhookproxyweb.handlers.users :refer [map->UserHandlers]]
            [webhookproxyweb.external.github :as github]
            [webhookproxyweb.server :as server]
            [webhookproxyweb.web :as web]))


(defn main-system 
  ([] (main-system (config/edn->config "config.edn")))
  ([config]
   (component/system-map 
     ;; simple config map
     :config config
     ;; infrastructure, external, db, outer onion, outer spaces services
     :gh (github/map->GithubConnectionManager {:config (:github-auth config)})
     :db (db/map->Database { :db-spec (-> config :db) }) 
     ;; domain services, controllers, integrators whatever the fuck its call nowadays
     :users (component/using (users/map->Users {}) [:db :gh])
     :webhooks (component/using (webhooks/map->WebHooks {}) [:db])
     ;; register handlers
     :webhook-handlers (component/using (map->WebHookHandlers {}) [:webhooks])
     :users-handlers (component/using (map->UserHandlers {}) [:users])
     :static-handlers (map->StaticHandlers { :root-path (-> config :static :root-path) })
     ;; pass handlers to web-app
     :web-app (component/using (web/map->WebApp {}) [:webhook-handlers
                                                     :users-handlers
                                                     :static-handlers])
     ;; pass web-app to web server
     :http-server (component/using 
                    (server/map->HttpKitServer (-> config :http-server))
                    [:web-app])
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



