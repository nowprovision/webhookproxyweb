(ns webhookproxyweb.web
  "component for route aggregation and handler building"
  (:require [com.stuartsierra.component :as component]
            [compojure.core :as compojure]
            [clojure.core.async :refer [put!]]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(defrecord WebApp [] 
  component/Lifecycle
  (start [component]
    ;; iterate over passed-in components building combined list of routes
    (let [route-providers (map second 
                               (filter (fn [[k v]] (seq? (seq (:routes v))))
                                       component))
          routes (flatten (map :routes route-providers))]
      (assoc component :routes routes)))
  (stop [component]
    (dissoc component :routes)))

(defn with-headers [io-body]
  {:headers { "Content-Type" "text/html" }
   :body io-body })


(defn handler [{:keys [routes extra-middleware error-ch error-router]}]
  "build a ring handler based on component routes"
  (let [error-middleware (:error-middleware error-router)]
    (let [handler (-> 
                    (apply compojure/routes (or routes []))
                    (wrap-json-body {:bigdecimals? true :keywords? true })
                    (wrap-anti-forgery)
                    error-middleware
                    (wrap-json-response)
                    (wrap-file "resources/public" { :index-files? false })
                    (wrap-defaults (assoc api-defaults :session true)))]
      (reduce (fn [acc middleware]
                (middleware acc)) handler (or extra-middleware [])))))

