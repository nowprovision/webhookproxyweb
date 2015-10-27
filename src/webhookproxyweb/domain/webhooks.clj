(ns webhookproxyweb.domain.webhooks
  (:refer-clojure :exclude [update])
  (:require [clj-uuid :as uuid]
            [korma.core :refer [insert limit 
                                update set-fields
                                select values where]]
            [webhookproxyweb.db :refer [webhook-entity with-db]]
            [webhookproxyweb.external.github :as github]))

(defrecord WebHooks [db])

(defn list-for-user [db user-id]
  (with-db db
    (select webhook-entity
            (where { :userid user-id }))))


(defmacro wrap-pgsql-errors [& forms]
  `(try
     ~@forms
     (catch org.postgresql.util.PSQLException pe#
       (let [msg# (some->> pe# .getServerErrorMessage .getDetail)]
         (println msg#)
         (if (not= (.indexOf msg# "Key (subdomain)=") -1)
           (throw (ex-info "Subdomain already exists" {:friendly true :type :pgsql }))
           (throw))))))

(defn add-for-user [db user-id payload]
  (wrap-pgsql-errors (with-db db
                       (insert webhook-entity
                               (values (merge payload {:active true 
                                                       :deleted false
                                                       :userid user-id }))))))

(defn update-for-user [db user-id payload]
  (let [payload (merge payload {:active true 
                                :deleted false
                                :userid user-id })]
    (wrap-pgsql-errors
      (with-db db
        (update webhook-entity
                (where {:id (:id payload) :userid user-id })
                (set-fields payload))))
      payload))
