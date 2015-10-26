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

(declare list-webhooks add-webhook)

(defn build-routes [webhooks]
  (with-security [:account-admin]
    (POST "/api/webhooks" req (add-webhook webhooks req))
    (GET "/api/webhooks" req (list-webhooks webhooks req))))

(defn list-webhooks [webhooks req]
  (let [user-id (-> req :session :uid)
        result (webhooks/list-for-user (:db webhooks) user-id)]
    { :body result }))

(defn add-webhook [webhooks req]
  (let [user-id (-> req :session :uid)
        is-new (-> req :body :is-new)
        payload (-> req :body :data)
        db-fn (if is-new webhooks/add-for-user webhooks/update-for-user)
        result (db-fn (:db webhooks) user-id payload)]
    { :body result }))

