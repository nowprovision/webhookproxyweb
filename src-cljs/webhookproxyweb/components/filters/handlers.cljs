(ns webhookproxyweb.components.filters.handlers
  (:require [re-frame.core :refer [dispatch register-handler]]
            [webhookproxyweb.components.filters.routes :refer [list-filters-path]]
            [webhookproxyweb.model :as model]))

(register-handler :filter-change-submitted
                  (fn [db [_ webhook-id payload]]
                    (println "HERE")
                    (dispatch [:submitted (merge {:schema model/WhitelistEntry
                                                  :sync-path (str "/api/webhooks/" webhook-id "/filters")
                                                  :done-path  (list-filters-path
                                                                { :webhook-id webhook-id})
                                                  } payload)])
                    db))
