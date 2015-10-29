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

(declare list-webhooks add-edit-webhook add-edit-whitelist)

(defn build-routes [webhooks]
  (with-security [:account-admin]
    (GET "/api/webhooks" req (list-webhooks webhooks req))
    (POST "/api/webhooks" req (add-edit-webhook webhooks req))
    (POST "/api/webhooks/:id/whitelists" req (add-edit-whitelist webhooks req))))

(defn list-webhooks [webhooks req]
  (let [user-id (-> req :session :uid)
        result (webhooks/list-webhooks webhooks user-id)]
    { :body result }))

(defn add-edit-webhook [webhooks req]
  (let [user-id (-> req :session :uid)
        is-new (-> req :body :is-new)
        payload (-> req :body :data)
        db-fn (if is-new webhooks/add-webhook webhooks/update-webhook)
        result (db-fn webhooks user-id payload)]
    { :body result }))

(defn add-edit-whitelist [webhooks req]
  (let [user-id (-> req :session :uid)
        webhook-id (-> req :params :id)
        is-new (-> req :body :is-new)
        payload (-> req :body :data)
        db-fn webhooks/add-whitelist
        op-result (db-fn webhooks user-id webhook-id payload)]
    { :body (webhooks/get-webhook webhooks user-id webhook-id) }))
