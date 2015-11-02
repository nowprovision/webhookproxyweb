(ns webhookproxyweb.components.webhook.handlers
  (:require [re-frame.core :refer [dispatch register-handler]]
            [webhookproxyweb.model :as model]))

(register-handler :webhook-change-submitted
                  (fn [db [_ payload]]
                       (dispatch [:submitted (merge {:schema model/WebHookProxyEntry
                                                     :sync-path "/api/webhooks"
                                                     :done-path "/" } payload)])
                       
                       db))
  
