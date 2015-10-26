(ns webhookproxyweb.handlers.static
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [webhookproxyweb.handlers.shared :refer [with-security]]))

(declare build-routes)

(defrecord StaticHandlers [root-path]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes root-path)))
  (stop [component] component))

(defn with-headers [io-body]
  {:headers { "Content-Type" "text/html" }
   :body io-body })

(defn build-routes [root-path]
  (with-security [:open]
    (GET "/whoami" req { :body (or (-> req :session) {} ) })
    (GET "/loggedin" req (with-headers (io/file root-path "loggedin.html")))
    (GET "/" req (with-headers (io/file root-path "index.html")))))


