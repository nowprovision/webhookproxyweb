(ns webhookproxyweb.components.filters.routes
  (:require-macros  [secretary.core :refer [defroute]])
  (:require [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary]))

(defroute list-filters-path "/webhooks/:webhook-id/filters" [webhook-id] 
  (dispatch [:change-screen :filters :listing webhook-id]))

(defroute add-filter-path "/tasks/:webhook-id/new-filter" [webhook-id] 
  (dispatch [:change-screen :filters :update-add webhook-id]))

(defroute edit-filter-path "/webhooks/:webhook-id/filters/:filter-id" [webhook-id filter-id] 
  (dispatch [:change-screen :filters :update-add webhook-id filter-id]))
