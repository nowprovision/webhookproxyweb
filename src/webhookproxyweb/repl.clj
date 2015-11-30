(ns webhookproxyweb.repl
  (:require webhookproxyweb.jdbc)
  (:require [webhookproxyweb.system :as system])
  (:require [webhookproxyweb.config :as config])
  (:require [ragtime.repl :as ragrepl])
  (:require [ragtime.jdbc :as jdbc]))

(defn help []
  (println "Forward migration run: (ragrepl/migrate (db-config))")
  (println "Rollback migration run: (ragrepl/rollback (db-config))"))

(defn db-config []
  (let [config (config/edn->config "config.edn")
        db-spec (:db config)]
    {:datastore (jdbc/sql-database db-spec)
     :migrations (jdbc/load-resources "migrations")}))

(defonce active-system (atom nil))

(defn start [& args]
  (let [init-system (system/main-system)]
    (reset! active-system init-system)
    (swap! active-system system/start)))

(defn stop [& args]
  (when @active-system
    (swap! active-system system/stop)))

(defn restart [& args]
  (stop)
  (start))
