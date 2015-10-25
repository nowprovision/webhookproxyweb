(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component]
            [webhookproxyweb.config :as config]
            [webhookproxyweb.db :as db]
            [webhookproxyweb.external.github :as github]
            [webhookproxyweb.domain.users :as users]
            [webhookproxyweb.server :as server]
            [webhookproxyweb.web :as web]))

(defn main-system 
  ([] (main-system (config/edn->config "config.edn")))
  ([config]
   (component/system-map 
     :config config
     :gh (github/map->GithubConnectionManager {:config (:github-auth config)})
     :db (db/map->Database { :db-spec (-> config :db) }) 
     :users (component/using (users/map->Users {}) [:db :gh])
     :web-app (component/using (web/map->WebApp {}) [:users])
     :http-server (component/using 
                    (server/map->HttpKitServer (-> config :http-server))
                    [:web-app])
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



