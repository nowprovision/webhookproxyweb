(ns webhookproxyweb.components.webhook.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe register-sub]]))

(register-sub :webhooks-changed (fn [db _] 
                                  (reaction (:items @db))))

(register-sub :webhook-changed (fn [db [_ webhook-id]]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction 
                                     (first (filter #(= (:id %) webhook-id)  @webhooks-changed))))))

(register-sub :webhooks-loaded (fn [db _]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction (not (nil? @webhooks-changed))))))
