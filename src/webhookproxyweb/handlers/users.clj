(ns webhookproxyweb.handlers.users
  (:require [webhookproxyweb.domain.users :as users]))

(defn github-callback [users req]
  (let [code (-> req :params :code)
        result (users/github-login users code)]
    {:session { :authenticated? true :uid (:id result) }
     :headers { "Content-Type" "text/html" }
     :body (str "Logged in: " result)}))

          


      

