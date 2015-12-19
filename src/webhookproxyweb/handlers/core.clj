(ns webhookproxyweb.handlers.core
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [webhookproxyweb.domain.users :as users]
            [webhookproxyweb.handlers.shared :refer [static-html
                                                     with-security 
                                                     with-no-cache]]))

(declare build-routes)

(defrecord CoreHandlers [users root-path debug?]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes users root-path debug?)))
  (stop [component] component))


(defn with-github-code-check [users root-path debug? req]
  (if-let [code (-> req :params :code)]
    (let [user-details (users/github-enrollment-and-identify users code)]
      ;; redirect without query params so ?code= querystring is not bookmarked
      (do
        (println "Code" code "Redirecting" (:uri req))
        {:status 302 
         ;:headers (with-no-cache { "Location" (:uri req) })
         ; weird nginx seems to reapply querystring without leading ?
         :headers (with-no-cache { "Location" (str (:uri req) "?") }) ; 90% sure no cache no req if query code is unique
         :body "302. Moved"
         :session (merge user-details
                         {:authenticated? true :roles [:account-admin] } )}))
    (static-html (io/file root-path 
                          (if debug?
                            "index.debug.html"
                            "index.prod.html")))))

(defn build-routes [users root-path debug?]
  (with-security [:open]
    (GET "/whoami" req { :body (or (-> req :session) {} ) })
    (GET "/loggedin" req (static-html (io/file root-path "loggedin.html")))
    (POST "/logout" req { :body {} :session {} }) ;make it idempotic hence :open
    ;;(GET "/callback" req (github-auth-callback users req))
    (GET "/admin/*" req (with-github-code-check users root-path debug? req))))


