(ns webhookproxyweb.handlers.static
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [webhookproxyweb.domain.users :as users]
            [webhookproxyweb.handlers.shared :refer [with-security with-no-cache]]))

(declare build-routes)

(defrecord StaticHandlers [users root-path]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes users root-path)))
  (stop [component] component))

(defn with-headers [io-body]
  {:headers { "Content-Type" "text/html" }
   :body io-body })


(defn with-github-code-check [users root-path req]
  (if-let [code (-> req :params :code)]
    (let [uid (users/github-enrollment-and-identify users code)]
      {:status 302 
       ;:headers (with-no-cache { "Location" (:uri req) })
       :headers { "Location" (:uri req) } ; 90% sure no cache no req if query code is unique
       :body "" 
       :session {:authenticated? true :uid uid :roles [:account-admin]} })
    (with-headers (io/file root-path "index.html"))))

(defn build-routes [users root-path]
  (with-security [:open]
    (GET "/whoami" req { :body (or (-> req :session) {} ) })
    (GET "/loggedin" req (with-headers (io/file root-path "loggedin.html")))
    (GET "*" req (with-github-code-check users root-path req))))


