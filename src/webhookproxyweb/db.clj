(ns webhookproxyweb.db
  (:require [com.stuartsierra.component :as component])
  (:require [clj-uuid :as uuid])
  (:require [korma.core :refer [database 
                                insert
                                table 
                                create-entity 
                                values] :as korma])
  (:require [korma.db :as db]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
    (let [conn (db/create-db (db/postgres db-spec))
          ; when using relations sqlkorma has enforced var/ns/macro pain to deal with
          whitelist-entity (-> "whitelist-entity" create-entity (table :whitelist))
          webhooks-entity (-> "webhooks-entity" create-entity (table :webhooks))] 
      (-> component
          (assoc :write-lock (Object.))
          (assoc :pool (-> conn :pool))
          (assoc :webhooks (database webhooks-entity conn))
          (assoc :whitelist (database whitelist-entity conn)))))
  (stop [component]
    (-> component
        :pool
        deref
        :datasource
        .close)
    (dissoc component :pool)))

(defn for-user [db user-id]
  (-> db
      :webhooks
      (korma/select*)
      (korma/where { :userid user-id })
      (korma/select)))

(defn find-subdomain [db subdomain]
  (seq (-> db
           :webhooks
           (korma/select*)
           (korma/where { :subdomain subdomain })
           (korma/limit 1)
           (korma/select))))

(defn add [db item]
  (locking (:write-lock db)
    (let [subdomain-available (not (find-subdomain db (:subdomain item)))]
      (when-not subdomain-available
        (throw (ex-info "Subdomain already exists" 
                        { :subdomain (:subdomain item) } )))
      (let [item (if (:id item) 
                   item
                   (assoc item :id (str (uuid/v4))))]
        (-> db
            :webhooks
            (korma/insert*)
            (values item)
            (insert))))))

