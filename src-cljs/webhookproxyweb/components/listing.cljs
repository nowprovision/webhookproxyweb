(ns webhookproxyweb.components.listing
  (:require [schema.core :as s]
            [re-frame.core :refer [subscribe]]))

(defn component []
  (let [items (subscribe [:items-changed]) ]
    (fn [] 
      [:div
       (for [item @items]
         ^{:key (:name item)} 
         [:div (str (:name item) ": " (:description item)) ])])))


