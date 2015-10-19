(ns webhookproxyweb.repl
  (:require [com.stuartsierra.component :as component])
  (:require [webhookproxyweb.system :as system])
  (:require [webhookproxyweb.config :as config])
  (:require [ragtime.repl :as ragrepl])
  (:require [ragtime.jdbc :as jdbc]))

(defn help []
  (println "Forward migration run: (ragrepl/migrate (db-config))")
  (println "Rollback migration run: (ragrepl/rollback (db-config))"))

(defn db-config []
  (let [main-system (system/main-system {})
        db-spec (-> main-system
                    :config
                    system/start
                    :root
                    :db)]
    {:datastore (jdbc/sql-database db-spec)
     :migrations (jdbc/load-resources "migrations")}))

(def active-system (atom nil))

(defn start [& args]
  (let [init-system (system/main-system {})]
    (reset! active-system init-system)
    (swap! active-system component/start)))

(defn stop [& args]
  (swap! active-system component/stop))

(defn restart [& args]
  (when @active-system (stop))
  (start))

(defn -main [& args]
  (start args))
