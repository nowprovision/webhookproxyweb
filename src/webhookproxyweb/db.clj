(ns webhookproxyweb.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [com.stuartsierra.component :as component]
            [jdbc.pool.c3p0 :as pool]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
    (let [conn (pool/make-datasource-spec 
                 (merge {:classname "org.postgresql.Driver" } db-spec))] 
      (assoc component :pool conn)))
  (stop [component]
    (-> component :datasource .close)
    (dissoc component :pool)))

(defmacro with-db [db# & forms#]
  nil)

(defn using-db [db query-fn query-arg]
  (query-fn query-arg { :connection (:pool db) }))



