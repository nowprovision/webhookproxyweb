(ns webhookproxyweb.handlers.webhooks
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [com.stuartsierra.component :as component]
            [webhookproxyweb.handlers.shared :refer [with-security]]
            [webhookproxyweb.domain.webhooks :as webhooks]
            [webhookproxyweb.schema :as schema]))

(declare build-routes)

(defrecord WebHookHandlers [webhooks]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes webhooks)))
  (stop [component] component))

(declare webhook-action filter-action list-webhooks delete-webhook add-edit-webhook add-edit-whitelist)


(defn request-schema [data-schema]
  {:action (schema/enum "new" "modify" "delete")
   :id schema/uuid-str
   :data (schema/maybe data-schema) })

(defn sanitize-against-schema [data-schema data]
  (select-keys data (keys data-schema))) 

(defn build-routes [webhooks]
  (with-security [:account-admin]
    (GET "/api/webhooks" req (list-webhooks webhooks req))
    (POST "/api/webhooks" req (webhook-action webhooks req))
    (POST "/api/webhooks/:id/filters" req (filter-action webhooks req))))

(defn list-webhooks [webhooks req]
  (let [user-id (-> req :session :uid)
        result (webhooks/list-webhooks webhooks user-id)]
    { :body (map (partial sanitize-against-schema schema/webhook-schema) 
                 result) }))

(defmulti webhook-action (fn [webhooks req] (keyword (-> req :body :action))))

(defmethod webhook-action :new  [webhooks req]
  (schema/validate (request-schema schema/webhook-schema) (-> req :body))
  (let [user-id (-> req :session :uid)
        payload (-> req :body :data)
        result (webhooks/add-webhook webhooks user-id payload)]
    { :body (sanitize-against-schema schema/webhook-schema result) }))

(defmethod webhook-action :modify  [webhooks req]
  (schema/validate (request-schema schema/webhook-schema) (-> req :body))
  (let [user-id (-> req :session :uid)
        payload (-> req :body :data)
        result (webhooks/update-webhook webhooks user-id payload)]
    { :body (sanitize-against-schema schema/webhook-schema result) }))

(defmethod webhook-action :delete [webhooks req]
  (schema/validate (request-schema schema/webhook-schema) (-> req :body))
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :body :id)
        result (webhooks/delete-webhook webhooks user-id webhook-id)]
    { :body (sanitize-against-schema schema/webhook-schema result) }))

(defmulti filter-action (fn [webhooks req] (keyword (-> req :body :action))))

(defmethod filter-action :new [webhooks req]
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :params :id)
        payload (-> req :body :data)
        op-result (webhooks/add-filter webhooks user-id webhook-id payload)]
    { :body (webhooks/get-webhook webhooks user-id webhook-id) }))

(defmethod filter-action :modify [webhooks req]
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :params :id)
        payload (-> req :body :data)
        op-result (webhooks/update-filter webhooks user-id webhook-id payload)]
    { :body (webhooks/get-webhook webhooks user-id webhook-id) }))

(defmethod filter-action :delete [webhooks req]
  (let [user-id (-> req :session :uid)
        filter-id (-> req :body :id)
        webhook-id (-> req :params :id)
        result (webhooks/delete-filter webhooks user-id filter-id)]
    { :body (webhooks/get-webhook webhooks user-id webhook-id) }))
