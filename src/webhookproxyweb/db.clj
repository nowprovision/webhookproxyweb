(ns webhookproxyweb.db
  (:require [com.stuartsierra.component :as component]
            [korma.core :refer [create-entity defentity table has-many]]
            [korma.db :as kormadb]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
    (let [conn (kormadb/create-db (kormadb/postgres db-spec))] 
      ; todo: eager init pool for fast failure
      (-> component (assoc :pool (-> conn :pool)))))
  (stop [component]
    (-> component
        :pool
        deref
        :datasource
        .close)
    (dissoc component :pool)))

(defentity whitelist-entity
  (table :whitelist))

(defentity webhook-entity
  (table :webhooks)
  (has-many whitelist-entity {:fk :webhookid }))

(defentity user-entity
  (table :users))

(defmacro with-db [db# & forms#]
  `(kormadb/with-db (:pool ~db#)
    ~@forms#))

(defn using-db [db query-fn query-arg]
  (query-fn query-arg { :connection @(:pool db) }))



