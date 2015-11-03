(ns webhookproxyweb.web
  "component for route aggregation and handler building"
  (:require [com.stuartsierra.component :as component]
            [compojure.core :as compojure]
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

(defn wrap-api-friendly-error [handler]
  "intercept friendly exception as excepted api errors"
  (fn [req]
    (try (handler req)
         (catch Throwable e
           (if-let [friendly (some->> e ex-data :friendly)]
             { :body {:friendly true :error (.getMessage e) } :status 500  }
             (throw e)
             )))))

(defn handler [{:keys [routes extra-middleware]}]
  "build a ring handler based on component routes"
  (let [handler (-> 
                  (apply compojure/routes (or routes []))
                  (wrap-api-friendly-error)
                  (wrap-json-body {:bigdecimals? true :keywords? true })
                  (wrap-anti-forgery)
                  (wrap-json-response)
                  (wrap-file "resources/public" { :index-files? false })
                  (wrap-defaults (assoc api-defaults :session true)))]
    (reduce (fn [acc middleware]
              (middleware acc)) handler (or extra-middleware []))))

