(ns webhookproxyweb.domain.webhooks
  (:require [yesql.core :refer [defquery]]
            [com.stuartsierra.component :as component]
            [webhookproxyweb.schema :as schema]
            [webhookproxyweb.db :refer [using-db]]))

(defrecord WebHooks [db]
  component/Lifecycle
  (start [component]
      (assoc component :update-lock (Object.)))
  (stop [component]
    component))

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

(defn get-webhook [{:keys [db] :as webhooks} user-id webhook-id]
  (let [r (first (filter #(= (:id %) webhook-id) (list-webhooks webhooks user-id)))]
    (if (nil? r)
      (throw (ex-info "Unable to get webhook" {:friendly true :type :pgsql }))
      r)))

(defn delete-webhook [{:keys [db]} user-id webhook-id]  
  (let [deleted (using-db db delete-webhook! 
                          {:userid user-id :id webhook-id })]
    (if (= deleted 1) 
      {:id webhook-id }
      (throw (ex-info "Unable to delete webhook" {:friendly true :type :pgsql })))))

(defn add-webhook [{:keys [db update-lock]} user-id webhook-id payload]
  (wrap-pgsql-errors
    (locking update-lock
      (:blob (using-db db insert-webhook<! {:userid user-id
                                            :blob payload
                                            :subdomain (:subdomain payload)
                                            :id webhook-id })))))

(defn update-webhook [{:keys [db update-lock] :as webhooks} user-id webhook-id payload]
  (wrap-pgsql-errors
    (locking update-lock
      (:blob (using-db db update-webhook<! {:blob payload
                                            :id webhook-id 
                                            :subdomain (:subdomain payload)
                                            :userid user-id })))))

(defn add-filter [{:keys [db update-lock] :as webhooks} user-id webhook-id payload]
  {:pre [(nil? (schema/check schema/filter-schema payload))] 
   :post [(nil? (schema/check schema/filter-schema %))] }
  (locking update-lock
    (let [filter-id (:id payload)
          webhook (get-webhook webhooks user-id webhook-id)]
      (->> (update webhook :filters (fn [filters] (conj filters payload)))
           (update-webhook webhooks user-id webhook-id)
           :filters
           (filter #(= (:id %) filter-id))
           first))))

(defn update-filter [{:keys [db update-lock] :as webhooks} user-id webhook-id payload]
  {:pre [(nil? (schema/check schema/filter-schema payload))] 
   :post [(nil? (schema/check schema/filter-schema %))] }
  (locking update-lock
    (let [filter-id (:id payload)
          webhook (get-webhook webhooks user-id webhook-id)]
      (->> (update webhook :filters (fn [filters] 
                                      (conj 
                                        (filter #(not= (:id %) filter-id) filters)
                                        payload)))
           (update-webhook webhooks user-id webhook-id)
           :filters
           (filter #(= (:id %) filter-id))
           first))))

(defn delete-filter [{:keys [db update-lock] :as webhooks} user-id webhook-id filter-id]
  (let [webhook (get-webhook webhooks user-id webhook-id)]
    (locking update-lock
      (->> (update webhook :filters (fn [filters] 
                                      (filter #(not= (:id %) filter-id) filters)))
           (update-webhook webhooks user-id webhook-id)))))
