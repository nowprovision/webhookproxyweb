(ns webhookproxyweb.web
  (:use compojure.core)
  (:require [com.stuartsierra.component :as component])
  (:require [ring.middleware.defaults :refer :all])
  (:require [ring.middleware.json :refer :all])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))


(def apiroutes 
  [(GET "/api/webhooks" req { :body  "HELLO WORLD" }) ])

(defrecord WebApi [] 
  component/Lifecycle
  (start [component]
    (assoc component :handler (-> (apply routes apiroutes)
                                  (wrap-json-body {:bigdecimals? true :keywords? true })
                                  wrap-json-response
                                  (wrap-defaults api-defaults))))
  (stop [component]
    (dissoc component :handler)))
