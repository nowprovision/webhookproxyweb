(ns webhookproxyweb.db
  (:require [clj-uuid :as uuid]
            [com.stuartsierra.component :as component]
            [korma.core :as korma :refer [create-entity defentity insert table
                                          values]]
            [korma.db :as db]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
    (let [conn (db/create-db (db/postgres db-spec))
          _ @(:pool conn)] ; eager init pool for fast failure
      (-> component
          (assoc :write-lock (Object.)) 
          (assoc :pool (-> conn :pool)))))
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

(defn for-user [db user-id]
  (db/with-db (:pool db)
    (->
      webhook-entity
      (korma/select*)
      (korma/where { :userid user-id })
      (korma/select))))

(defn find-subdomain [db subdomain]
  (seq (db/with-db (:pool db)
         (-> webhook-entity
             (korma/select*)
             (korma/where { :subdomain subdomain })
             (korma/limit 1)
             (korma/select)))))

(defn add [db item]
  (locking (:write-lock db)
    (let [subdomain-available (not (find-subdomain db (:subdomain item)))]
      (when-not subdomain-available
        (throw (ex-info "Subdomain already exists" 
                        { :subdomain (:subdomain item) } )))
      (let [item (if (:id item) 
                   item
                   (assoc item :id (str (uuid/v4))))]
        (db/with-db (:pool db)
          (-> webhook-entity
              (korma/insert*)
              (values item)
              (insert)))))))
