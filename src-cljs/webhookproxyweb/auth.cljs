(ns webhookproxyweb.auth
  (:require-macros [webhookproxyweb.config :refer [from-config]])
  (:require [freeman.ospa.core 
             :refer [register-sub 
                     register-handler
                     register-route
                     subscribe 
                     dispatch 
                     resolve-route] 
             :refer-macros [reaction]]
            [webhookproxyweb.schema :as schema]
            [ajax.core :refer [GET POST]]
            [cljs.core.async :as async :refer [chan <! >!]]))

(register-sub :logged-in (fn [db _] (reaction (:logged-in-user @db))))

(register-sub :login-started (fn [db _] (reaction (:login-started @db))))

(register-handler :logout (fn [db _] 
                            (let [xsrf-token (-> db 
                                                 :logged-in-user 
                                                 :ring.middleware.anti-forgery/anti-forgery-token)]
                              (POST "/logout" {:format :json
                                               :params {}
                                               :headers { "X-XSRF-Token" xsrf-token } 
                                               :response-format :json
                                               :error-handler (fn [_] nil)
                                               :handler (fn [_] nil) })
                              { } ;reset app state to empty
                              )))

(register-route :logout "/admin/logout" #(dispatch [:logout]))

(defn json-get [url opts]
  (GET url (merge {:response-format :json :keywords? true  } opts)))

(defn fetch-identity [db _]
  (->> { :handler (fn [user]
                    (if (:authenticated? user)
                      (do
                        (dispatch [:reset-identity user])
                        (dispatch [:fetch-webhooks]))
                      (dispatch [:start-auth-flow])
                      ))
        :headers {"Cache-Control" "no-cache, no-store, must-revalidate"
                  "Pragma" "no-cache"
                  "Expires" "0" }
        :error-handler (fn [] nil) }
       (json-get "/whoami"))
  db)

(defn reset-identity [db [_ payload]]
  (-> db
      (assoc :login-started false)
      (assoc :logged-in-user payload)))

(defn github-login-url [redirect-uri] 
  (str "https://github.com/login/oauth/authorize?"
       "client_id=" 
       (from-config [:github-auth :client-id])
       "&redirect_uri=" redirect-uri))

(defn start-auth-flow [db [_ payload]]
  (.setTimeout js/window (fn []
                           (set! (.-location js/window) 
                                 (github-login-url (-> js/window .-location .-href)))) 
               200)
  ;;bye bye
  (assoc db :login-started true))


