(ns webhookproxyweb.domain.webhooks
  (:refer-clojure :exclude [update])
  (:require [clj-uuid :as uuid]
            [korma.core :refer [insert insert* limit 
                                update update* set-fields
                                select select* values where]]
            [webhookproxyweb.db :refer [webhook-entity with-db]]
            [webhookproxyweb.external.github :as github]))

(defrecord WebHooks [db])

(defn list-for-user [db user-id]
  (with-db db
    (-> webhook-entity
        select*
        (where { :userid user-id })
        select)))

(defn add-for-user [db user-id payload]
  (with-db db
    (-> webhook-entity
        insert*
        (values (merge payload {:active true 
                                :deleted false
                                :userid user-id }))
        insert)))

(defn update-for-user [db user-id payload]
  (let [payload (merge payload {:active true 
                                :deleted false
                                :userid user-id })]
    (with-db db
      (-> webhook-entity
          update*
          (where {:id (:id payload) :userid user-id })
          (set-fields payload)
          update)
      payload)))
