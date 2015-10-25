(ns webhookproxyweb.web
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.file :refer :all]
            [ring.middleware.json :refer :all]
            [webhookproxyweb.handlers.users :as user-handlers]
            [webhookproxyweb.auth :as auth]
            [webhookproxyweb.db :as db]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit 
             :refer [sente-web-server-adapter]]
            ))


(declare add-webhook apiroutes)

(defrecord WebApp [users] 
  component/Lifecycle
  (start [component]
    (assoc component :handler (-> (apply routes (apiroutes users))
                                  (wrap-json-body {:bigdecimals? true :keywords? true })
                                  wrap-json-response
                                  (wrap-file "resources/public" { :index-files? false })
                                  (wrap-defaults api-defaults))))
  (stop [component]
    (dissoc component :handler)))


(defn handler [web-app]
  (:handler web-app))

(defn add-webhook [db-inst session payload]
  (let [user-id (or (:userid session) 1)
        payload (assoc payload :userid user-id)]
    (db/add db-inst payload)))

(defn get-webhooks [db-inst session]
  (let [user-id (or (:userid session) 1)]
    (db/for-user db-inst user-id)))

(defn apiroutes [users]
  [(GET "/" req (io/file "resources/public/index.html"))
   (GET "/callback" req (user-handlers/github-callback users req))])


