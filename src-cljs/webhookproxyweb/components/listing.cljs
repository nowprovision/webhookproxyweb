(ns webhookproxyweb.components.listing
  (:require [schema.core :as s]
            [re-frame.core :refer [subscribe dispatch]]))

(defn component []
  (let [items (subscribe [:items-changed]) ]
    (fn [] 
      [:div
       (for [item @items]
         ^{:key (:name item)} 
         [:div 
          [:div (str (:name item) " | " (:description item) " | " (:subdomain item))]
          [:button {:on-click #(dispatch [:change-screen :add-form item])} "Edit Details"]
          [:button {:on-click #(dispatch [:change-screen 
                                          :whitelists 
                                          :listing (:id item)])} "Edit IP Filters"]]
          )])))


