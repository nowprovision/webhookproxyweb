(ns webhookproxyweb.system
  (:require [com.stuartsierra.component :as component])
  (:require [webhookproxyweb.config :as config])
  (:require [webhookproxyweb.db :as db]))

(defn main-system [config] 
  (component/system-map 
    :config (config/make-system-config)
    :db (component/using 
          (db/map->Database {})
          [:config])))

; alias component stop/start
(def start component/start)
(def stop component/stop)



