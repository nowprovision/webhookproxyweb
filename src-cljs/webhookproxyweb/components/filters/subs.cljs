(ns webhookproxyweb.components.filters.subs
  (:require [freeman.ospa.core 
             :refer [register-sub subscribe] 
             :refer-macros [reaction]]))

(register-sub :filters-changed 
              (fn [db [_ webhook-id]]
                (let [webhook (subscribe [:webhook-changed webhook-id])]
                  (reaction (:filters @webhook)))))

(register-sub :filter-changed
              (fn [db [_ webhook-id filter-id]]
                (let [filters (subscribe [:filters-changed webhook-id])]
                  (reaction
                    (first (filter #(= (:id %) filter-id) @filters))))))

