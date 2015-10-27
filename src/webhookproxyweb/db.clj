(ns webhookproxyweb.db
  (:require [com.stuartsierra.component :as component]
            [korma.core :refer [create-entity defentity table]]
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
  (create-entity)
  (table :whistlist))

(defentity webhook-entity
  (create-entity)
  (table :webhooks))

(defentity user-entity
  (create-entity)
  (table :users))

(defmacro with-db [db# & forms#]
  `(kormadb/with-db (:pool ~db#)
    ~@forms#))



