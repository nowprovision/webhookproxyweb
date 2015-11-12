(ns webhookproxyweb.handlers.webhooks
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [com.stuartsierra.component :as component]
            [webhookproxyweb.handlers.shared :refer [with-security]]
            [webhookproxyweb.domain.webhooks :as webhooks]))

(declare build-routes)

(defrecord WebHookHandlers [webhooks]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes webhooks)))
  (stop [component] component))

(declare webhook-action list-webhooks delete-webhook add-edit-webhook add-edit-whitelist)

(defn build-routes [webhooks]
  (with-security [:account-admin]
    (GET "/api/webhooks" req (list-webhooks webhooks req))
    (POST "/api/webhooks" req (webhook-action webhooks req))
    (POST "/api/webhooks/:id/filters" req (add-edit-whitelist webhooks req))))

(defn list-webhooks [webhooks req]
  (let [user-id (-> req :session :uid)
        result (webhooks/list-webhooks webhooks user-id)]
    { :body result }))

(defmulti webhook-action (fn [webhooks req] (keyword (-> req :body :action))))

(defmethod webhook-action :new  [webhooks req]
  (let [user-id (-> req :session :uid)
        payload (-> req :body :data)
        result (webhooks/add-webhook webhooks user-id payload)]
    { :body result }))

(defmethod webhook-action :modify  [webhooks req]
  (let [user-id (-> req :session :uid)
        payload (-> req :body :data)
        result (webhooks/update-webhook webhooks user-id payload)]
    { :body result }))

(defmethod webhook-action :delete [webhooks req]
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :body :id)
        result (webhooks/delete-webhook webhooks user-id webhook-id)]
    { :body result }))

(defn add-edit-whitelist [webhooks req]
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :params :id)
        is-new (-> req :body :is-new)
        payload (-> req :body :data)
        db-fn (if is-new webhooks/add-whitelist webhooks/update-whitelist)
        op-result (db-fn webhooks user-id webhook-id payload)]
    { :body (webhooks/get-webhook webhooks user-id webhook-id) }))
