(ns webhookproxyweb.handlers.users
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [com.stuartsierra.component :as component]
            [webhookproxyweb.handlers.shared :refer [with-security with-no-cache]]
            [webhookproxyweb.domain.users :as users]))

(declare build-routes)

(defrecord UserHandlers [users]
  component/Lifecycle
  (start [component]
    (assoc component :routes (build-routes users)))
  (stop [component] component))

(declare github-auth-callback)

(defn build-routes [users]
  (with-security [:open]
    (POST "/logout" req { :body {} :session {} }) ;make it idempotic hence :open
    (GET "/callback" req (github-auth-callback users req))))
             
(defn github-auth-callback [users req]
  (let [code (-> req :params :code)
        user-id (users/github-enrollment-and-identify users code)]
    {:session {:authenticated? true 
               :uid user-id
               :roles [:account-admin]}
     :status 302
     :headers (with-no-cache { "Location" "/loggedin" } )
     :body "" }
    ))


