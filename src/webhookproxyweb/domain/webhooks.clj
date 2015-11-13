(ns webhookproxyweb.domain.webhooks
  (:refer-clojure :exclude [update])
  (:require [yesql.core :refer [defquery]])
  (:require [clj-uuid :as uuid]
            [webhookproxyweb.schema :as schema]
            [korma.core :refer [insert limit 
                                update set-fields
                                delete with
                                select values where]]
            [webhookproxyweb.db :refer [webhook-entity 
                                        whitelist-entity
                                        using-db
                                        with-db]]
            [webhookproxyweb.external.github :as github]))


(defrecord WebHooks [db])

(defquery insert-webhook<! "sql/webhook-insert.sql")
(defquery get-webhooks "sql/webhook-listing.sql")
(defquery update-webhook<! "sql/webhook-update.sql")
(defquery delete-webhook! "sql/webhook-delete.sql")

(defmacro wrap-pgsql-errors [& forms]
  `(try
     (try
       ~@forms
       (catch java.sql.BatchUpdateException batche#
         (throw (.getNextException batche#))))
     (catch org.postgresql.util.PSQLException pe#
       (let [msg# (some->> pe# .getServerErrorMessage .getDetail)]
         (if (and (string? msg#) (not= (.indexOf msg# "Key (subdomain)=") -1))

           (throw (ex-info "Subdomain already exists" {:friendly true :type :pgsql }))
           (throw pe#))))))

(defn list-webhooks [{:keys [db]} user-id]
  (map :blob (using-db db get-webhooks { :userid user-id })))

(defn get-webhook [{:keys [db]} user-id webhook-id]
  (first (filter #(= (:id %) webhook-id) (list-webhooks db user-id))))

(defn delete-webhook [{:keys [db]} user-id webhook-id]  
  (let [deleted (using-db db delete-webhook! 
                          {:userid user-id :id webhook-id })]
    (if (= deleted 1) 
      {:id webhook-id }
      (throw (ex-info "Unable to delete" {:friendly true :type :pgsql })))))

(defn add-webhook [{:keys [db]} user-id webhook-id payload]
  (wrap-pgsql-errors
    (:blob (using-db db insert-webhook<! {:userid user-id
                                          :blob payload
                                          :subdomain (:subdomain payload)
                                          :id webhook-id }))))

(defn update-webhook [{:keys [db] :as webhooks} user-id webhook-id payload]
  (wrap-pgsql-errors
    (:blob (using-db db update-webhook<! {:blob payload
                                          :id webhook-id 
                                          :subdomain (:subdomain payload)
                                          :userid user-id }))))

(defn add-filter [{:keys [db] :as webhooks} user-id webhook-id payload]
  {:pre [(nil? (schema/check schema/filter-schema payload))] 
   :post [(nil? (schema/check schema/filter-schema %))] }
  (let [webhook-ids (map :id (list-webhooks webhooks user-id))
        correct-owner (boolean (some (set webhook-ids) [webhook-id]))]
    (if-not correct-owner
      (throw (ex-info (str "No webhook found for " webhook-id) 
                      { :friendly true :type :security }))
      (wrap-pgsql-errors 
        (with-db db
          (insert whitelist-entity
                  (values (merge payload {:userid user-id 
                                          :webhookid webhook-id
                                          }))))))))

(defn update-filter [{:keys [db] :as webhooks} user-id webhook-id payload]
  {:pre [(nil? (schema/check schema/filter-schema payload))] }
  (let [webhook-ids (map :id (list-webhooks webhooks user-id))
        correct-owner (boolean (some (set webhook-ids) [webhook-id]))]
    (if-not correct-owner
      (throw (ex-info (str "No webhook found for " webhook-id) 
                      { :friendly true :type :security }))
      (wrap-pgsql-errors 
        (with-db db
          (update whitelist-entity
                  (where {:id (:id payload)
                          :userid user-id
                          :webhookid webhook-id })
                  (set-fields (merge payload {:userid user-id :webhookid webhook-id }))))))))

(defn update-filter [{:keys [db] :as webhooks} user-id webhook-id payload]
  {:pre [(nil? (schema/check schema/filter-schema payload))] }
  (let [webhook-ids (map :id (list-webhooks webhooks user-id))
        correct-owner (boolean (some (set webhook-ids) [webhook-id]))]
    (if-not correct-owner
      (throw (ex-info (str "No webhook found for " webhook-id) 
                      { :friendly true :type :security }))
      (wrap-pgsql-errors 
        (with-db db
          (update whitelist-entity
                  (where {:id (:id payload)
                          :userid user-id
                          :webhookid webhook-id })
                  (set-fields (merge payload {:userid user-id :webhookid webhook-id }))))))))

(defn delete-filter [{:keys [db]} user-id filter-id]
  (with-db db
    (let [_ (delete whitelist-entity
                    (where {:id filter-id
                            :userid user-id }))]
      { :ok true })))
