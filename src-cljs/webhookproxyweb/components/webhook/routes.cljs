(ns webhookproxyweb.components.webhook.routes
  (:require [freeman.ospa.core :refer [dispatch
                                       register-route]]))

(register-route :list-webhooks
                "/" 
                (fn [] (dispatch [:change-screen :webhooks :listing])))

(register-route :add-webhook
                "/tasks/new-webhook" 
                (fn [] (dispatch [:change-screen :webhooks :update-add])))

(register-route :edit-webhook
                "/webhooks/:webhook-id" 
                (fn [{:keys [webhook-id]}]
                  (dispatch [:change-screen :webhooks :update-add webhook-id])))
