(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component]
            [webhookproxyweb.config :as config]
            [webhookproxyweb.db :as db]
            [webhookproxyweb.auth :as auth]
            [webhookproxyweb.server :as server]
            [webhookproxyweb.web :as web]))

(defn main-system 
  ([] (main-system (config/edn->config "config.edn")))
  ([config]
   (component/system-map 
     :config config
     :github-cm (auth/map->GithubConnectionManager {:config (:github-auth config)})
     :http-server (component/using 
                    (server/map->HttpKitServer (-> config :http-server))
                    [:web-app])
     :web-app (component/using (web/map->WebApp {}) [:db :github-cm])
     :db (db/map->Database { :db-spec (-> config :db) }) 
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



