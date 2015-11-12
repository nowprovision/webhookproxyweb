(ns webhookproxyweb.components.webhook.handlers
  (:require [freeman.ospa.core :refer [register-handler]]
            [webhookproxyweb.forms :refer [handle-form]]))

(register-handler :webhook-spec-created
                  (handle-form {:model :webhook
                                :action :new 
                                :completed-event [:redirect :list-webhooks] }))

(register-handler :webhook-spec-changed
                  (handle-form {:model :webhook 
                                :action :modify 
                                :completed-event [:redirect :list-webhooks] }))

(register-handler :webhook-removed
                  (handle-form {:model :webhoook 
                                :action :delete 
                                :completed-event [:redirect :list-webhooks] }))
