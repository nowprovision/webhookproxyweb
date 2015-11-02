(ns webhookproxyweb.routes
  (:require-macros  [secretary.core :refer [defroute]])
  (:require [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary]))

(defroute list-webhooks-path "/" [] 
  (dispatch [:change-screen :webhooks :listing]))

(defroute add-webhook-path "/tasks/new-webhook" []
  (dispatch [:change-screen :webhooks :update-add]))

(defroute edit-webhook-path "/webhooks/:webhook-id" [webhook-id] 
  (dispatch [:change-screen :webhooks :update-add webhook-id]))


(defroute list-whitelists-path "/webhooks/:webhook-id/whitelists" [webhook-id] 
  (dispatch [:change-screen :whitelists :listing webhook-id]))

(defroute add-whitelist-path "/tasks/:webhook-id/new-whitelist" [webhook-id] 
  (dispatch [:change-screen :whitelists :update-add webhook-id]))

(defroute edit-whitelist-path "/webhooks/:webhook-id/whitelists/:whitelist-id" [webhook-id whitelist-id] 
  (dispatch [:change-screen :whitelists :update-add webhook-id whitelist-id]))
