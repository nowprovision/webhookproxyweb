(ns webhookproxyweb.sync
  (:require [ajax.core :refer [GET DELETE]]
            [re-frame.core :refer [dispatch]]))


(defn json-get [url opts]
  (GET url (merge {:response-format :json :keywords? true  } opts)))

(defn fetch-webhooks [db _]
  (->> {:handler (fn [webhooks] 
                   (dispatch [:reset-webhooks webhooks]))
        :error-handler (fn [] nil) }
       (json-get "/api/webhooks"))
  db) 

(defn delete-webhook [db payload success-handler error-handler]
  (->> {:handler success-handler
        :error-handler error-handler 
        :format :json 
        :params payload
        :headers  { :X-XSRF-Token (-> db :logged-in-user :ring.middleware.anti-forgery/anti-forgery-token) }
        :response-format :json
        :keywords? true }
       (DELETE "/api/webhooks"))
  db) 


(defn reset-webhooks [db [_ payload]]
  (assoc db :items payload))

