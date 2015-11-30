(ns webhookproxyweb.handlers.core
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [webhookproxyweb.domain.users :as users]
            [webhookproxyweb.handlers.shared :refer [with-security with-no-cache]]))

(declare build-routes)

(defrecord CoreHandlers [users root-path]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes users root-path)))
  (stop [component] component))

(defn with-headers [io-body]
  {:headers { "Content-Type" "text/html" }
   :body io-body })


(defn with-github-code-check [users root-path req]
  (if-let [code (-> req :params :code)]
    (let [user-details (users/github-enrollment-and-identify users code)]
      ;; redirect without query params so ?code= querystring is not bookmarked
      (do
        (println "Redirecting" (:uri req))
        {:status 302 
         ;:headers (with-no-cache { "Location" (:uri req) })
         :headers (with-no-cache { "Location" (:uri req) }) ; 90% sure no cache no req if query code is unique
         :body ""
         :session (merge user-details
                         {:authenticated? true :roles [:account-admin] } )}))
    (with-headers (io/file root-path "index.html"))))

(defn build-routes [users root-path]
  (with-security [:open]
    (GET "/whoami" req { :body (or (-> req :session) {} ) })
    (GET "/loggedin" req (with-headers (io/file root-path "loggedin.html")))
    (POST "/logout" req { :body {} :session {} }) ;make it idempotic hence :open
    ;;(GET "/callback" req (github-auth-callback users req))
    (GET "*" req (with-github-code-check users root-path req))))


