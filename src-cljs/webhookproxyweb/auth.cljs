(ns webhookproxyweb.auth
  (:require-macros [reagent.ratom :refer [reaction]]  
                   [webhookproxyweb.config :refer [from-config]]
                   [secretary.core :refer [defroute]])
  (:require [ajax.core :refer [GET POST]]
            [re-frame.core :refer [dispatch]]))

(defn json-get [url opts]
  (GET url (merge {:response-format :json :keywords? true  } opts)))

(defn logout [db [_ payload]]
  (POST "/api/logout" {:format :json
                       :params {}
                       :response-format :json
                       :handler (fn [resp]
                                  (dispatch [:reset-identity nil]))
                       }))

(defn fetch-identity [db _]
  (->> { :handler (fn [user]
                    (if (:authenticated? user)
                      (dispatch [:reset-identity user])
                      (dispatch [:start-auth-flow])
                      ))
        :error-handler (fn [] nil) }
       (json-get "/whoami"))
  db)

(defn reset-identity [db [_ payload]]
  (assoc db :logged-in-user payload))

(defn github-login-url [redirect-uri] 
  (str "https://github.com/login/oauth/authorize?"
       "scope=user:email&client_id=" 
       (from-config [:github-auth :client-id])
       "&redirect_uri=" redirect-uri))

(defn start-auth-flow [db [_ payload]]
  (set! (.-location js/window) 
        (github-login-url (-> js/window .-location .-href)))
  ;;bye bye
  db)


