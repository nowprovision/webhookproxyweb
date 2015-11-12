(ns webhookproxyweb.components.webhook.subs
  (:require [freeman.ospa.core 
             :refer [register-sub subscribe] 
             :refer-macros [reaction]]))

(register-sub :webhooks-changed (fn [db _] 
                                  (reaction (:items @db))))

(register-sub :webhook-changed (fn [db [_ webhook-id]]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction 
                                     (first (filter #(= (:id %) webhook-id)  @webhooks-changed))))))

(register-sub :webhooks-loaded (fn [db _]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction (not (nil? @webhooks-changed))))))
