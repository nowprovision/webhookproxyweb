(ns webhookproxyweb.sync
  (:require [ajax.core :refer [GET]]
            [re-frame.core :refer [dispatch]]))


(defn json-get [url opts]
  (GET url (merge {:response-format :json :keywords? true  } opts)))

(defn fetch-webhooks [db _]
  (->> {:handler (fn [webhooks] 
                   (dispatch [:reset-webhooks webhooks]))
        :error-handler (fn [] nil) }
       (json-get "/api/webhooks"))
  db) 

(defn reset-webhooks [db [_ payload]]
  (assoc db :items payload))

