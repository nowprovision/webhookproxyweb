(ns webhookproxyweb.repl
  (:require [webhookproxyweb.system :as system])
  (:require [webhookproxyweb.config :as config])
  (:require [ragtime.repl :as ragrepl])
  (:require [ragtime.jdbc :as jdbc]))

(defn help []
  (println "Forward migration run: (ragrepl/migrate (db-config))")
  (println "Rollback migration run: (ragrepl/rollback (db-config))"))

(defn db-config []
  (let [db-spec {}]
    {:datastore (jdbc/sql-database db-spec)
     :migrations (jdbc/load-resources "migrations")}))

(def active-system (atom nil))

(defn start [& args]
  (let [init-system (system/main-system)]
    (reset! active-system init-system)
    (swap! active-system system/start)))

(defn stop [& args]
  (swap! active-system system/stop))

(defn restart [& args]
  (when @active-system (stop))
  (start))

(defn -main [& args]
  (start args))
