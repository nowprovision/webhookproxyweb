(ns webhookproxyweb.components.webhook.routes
  (:require-macros  [secretary.core :refer [defroute]])
  (:require [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary]))

(defroute list-webhooks-path "/" [] 
  (dispatch [:change-screen :webhooks :listing]))

(defroute add-webhook-path "/tasks/new-webhook" []
  (dispatch [:change-screen :webhooks :update-add]))

(defroute edit-webhook-path "/webhooks/:webhook-id" [webhook-id] 
  (dispatch [:change-screen :webhooks :update-add webhook-id]))
