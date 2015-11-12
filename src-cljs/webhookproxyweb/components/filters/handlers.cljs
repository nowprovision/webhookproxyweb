(ns webhookproxyweb.components.filters.handlers
  (:require [freeman.ospa.core :refer [register-handler]]
            [webhookproxyweb.forms :refer [handle-form]]))

(register-handler :filter-spec-created
                  (handle-form {:model :filter
                                :action :new 
                                :completed-event [:redirect :list-filters] }))

(register-handler :filter-spec-changed
                  (handle-form {:model :filter 
                                :action :modify 
                                :completed-event [:redirect :list-webhooks] }))

(register-handler :filter-removed
                  (handle-form {:model :filter 
                                :action :delete 
                                :completed-event [:redirect :list-webhooks] }))
