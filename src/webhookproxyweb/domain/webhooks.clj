(ns webhookproxyweb.domain.webhooks
  (:refer-clojure :exclude [update])
  (:require [clj-uuid :as uuid]
            [webhookproxyweb.schema :as schema]
            [korma.core :refer [insert limit 
                                update set-fields
                                delete with
                                select values where]]
            [webhookproxyweb.db :refer [webhook-entity 
                                        whitelist-entity
                                        with-db]]
            [webhookproxyweb.external.github :as github]))


(defrecord WebHooks [db])

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
  (with-db db
    (select webhook-entity
            (with whitelist-entity)
            (where {:deleted false 
                    :userid user-id }))))

(defn get-webhook [{:keys [db]} user-id webhook-id]
  (with-db db
    (first (select webhook-entity
            (with whitelist-entity)
            (where {:id webhook-id 
                    :userid user-id })))))

(defn delete-webhook [{:keys [db]} user-id webhook-id]
  (with-db db
    (let [_ (delete whitelist-entity
                    (where {:webhookid webhook-id 
                            :userid user-id }))
          _ (delete webhook-entity
                    (where {:id webhook-id 
                            :userid user-id }))]
      { :id webhook-id })))

(defn add-webhook [{:keys [db]} user-id payload]
  {:pre [(nil? (schema/check schema/WebHookProxyEntry payload))] 
   :post [(nil? (schema/check schema/WebHookProxyEntry %))] }
  (-> (wrap-pgsql-errors (with-db db
                           (insert webhook-entity
                                   (values (merge (dissoc payload :whitelist)
                                                  {:active true :deleted false :userid user-id })))))))

(defn update-webhook [{:keys [db] :as webhooks} user-id payload]
  {:pre [(nil? (schema/check schema/WebHookProxyEntry payload))] 
   :post [(nil? (schema/check schema/WebHookProxyEntry %))] }
  (let [payload (merge (dissoc payload :whitelist)
                       {:active true :deleted false :userid user-id })]
    (wrap-pgsql-errors
      (with-db db
        (update webhook-entity
                (where {:id (:id payload) :userid user-id })
                (set-fields payload))))
    (get-webhook webhooks user-id (:id payload))))

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
  {:pre [(nil? (schema/check schema/WhitelistEntry payload))] }
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
  {:pre [(nil? (schema/check schema/WhitelistEntry payload))] }
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
