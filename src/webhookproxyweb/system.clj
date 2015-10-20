(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component])
  (:require [webhookproxyweb.config :as config])
  (:require [webhookproxyweb.db :as db])
  (:require [webhookproxyweb.web :as web])
  (:require [webhookproxyweb.figwheel :as figwheel])
  (:require [webhookproxyweb.server :as server]))

(defn main-system 
  ([] (main-system (config/edn->config "config.edn")))
  ([config]
   (component/system-map 
     :config config
     :figwheel (figwheel/map->Figwheel (-> config :figwheel))
     :http-server (component/using 
                    (server/map->HttpKitServer (-> config :http-server))
                    [:web-app])
     :web-app (component/using (web/map->WebApp {}) [:db])
     :db (db/map->Database { :db-spec (-> config :db) }) 
     )))

; alias component stop/start
(def start component/start)
(def stop component/stop)



