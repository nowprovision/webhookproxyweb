(ns webhookproxyweb.db
  (:require [com.stuartsierra.component :as component])
  (:require [korma.core :refer [belongs-to 
                                defentity
                                database 
                                modifier
                                pk
                                create-relation 
                                has-one 
                                table 
                                create-entity 
                                has-many
                                raw] :as korma])
  (:require [korma.db :as db])
  (:require [clj-time.core :as t])
  (:require [clj-time.coerce :as c]))

(defentity whitelist-entity
  (table :whitelist))

(defentity webhooks-entity
  (table :webhooks)
  (has-many whitelist-entity { :fk :webhookid }))

(defrecord Database [config]
  component/Lifecycle
  (start [component]
    (let [db-spec (-> config :root :db)
          conn (db/create-db (db/postgres db-spec))]
      (-> (assoc component :conn conn)
          (assoc :webhooks (database webhooks-entity conn)))))
  (stop [component]
    (-> component
        :conn
        :pool
        deref
        :datasource
        .close)
    (dissoc component :pool)
    ))

(defn for-user [db user-id]
  (-> db
      :webhooks
      (korma/select*)
      (korma/with whitelist-entity)
      (korma/where { :userid user-id })
      (korma/select)))



