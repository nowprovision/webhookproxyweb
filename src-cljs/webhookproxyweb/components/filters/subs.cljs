(ns webhookproxyweb.components.filters.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe register-sub]]))

(register-sub :filters-changed 
              (fn [db [_ webhook-id]]
                (let [webhook (subscribe [:webhook-changed webhook-id])]
                  (reaction (:whitelist @webhook)))))

(register-sub :filter-changed
              (fn [db [_ webhook-id filter-id]]
                (let [filters (subscribe [:filters-changed webhook-id])]
                  (reaction
                    (first (filter #(= (:id %) filter-id) @filters))))))

