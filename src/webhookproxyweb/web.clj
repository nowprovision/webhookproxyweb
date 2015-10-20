(ns webhookproxyweb.web
  (:use compojure.core)
  (:require [webhookproxyweb.db :as db])
  (:require [com.stuartsierra.component :as component])
  (:require [ring.middleware.defaults :refer :all])
  (:require [ring.middleware.json :refer :all])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))


(declare add-webhook apiroutes)

(defrecord WebApp [db] 
  component/Lifecycle
  (start [component]
    (assoc component :handler (-> (apply routes (apiroutes db))
                                  (wrap-json-body {:bigdecimals? true :keywords? true })
                                  wrap-json-response
                                  (wrap-defaults api-defaults))))
  (stop [component]
    (dissoc component :handler)))


(defn handler [web-app]
  (:handler web-app))

(defn add-webhook [db-inst session payload]
  (let [user-id (or (:userid session) 1)
        payload (assoc payload :userid user-id)]
    (db/add db-inst payload)))

(defn apiroutes [db-inst]
  [(POST "/api/webhooks" req {:body (add-webhook db-inst 
                                                 (:session req)
                                                 (:body req)) })])


