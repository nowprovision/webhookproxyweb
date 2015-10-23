(ns webhookproxyweb.web
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.file :refer :all]
            [ring.middleware.json :refer :all]
            [webhookproxyweb.auth :as auth]
            [webhookproxyweb.db :as db]))


(declare add-webhook apiroutes)

(defrecord WebApp [db github-cm] 
  component/Lifecycle
  (start [component]
    (assoc component :handler (-> (apply routes (apiroutes db github-cm))
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

(defn apiroutes [db-inst github-cm]
  [(GET "/" req (io/file "resources/public/index.html"))
   (GET "/callback" req {:headers { "Content-Type" "text/html" }
                         :body (auth/authenticate github-cm {:code (-> req :params :code) }) 
                         })
   (GET "/api/webhooks" req {:body (get-webhooks db-inst (:session req)) })
   (POST "/api/webhooks" req {:body (add-webhook db-inst 
                                                 (:session req)
                                                 (:body req)) })])


