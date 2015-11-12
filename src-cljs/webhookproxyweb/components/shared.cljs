(ns webhookproxyweb.components.shared
  (:require [reagent-forms.core :as reagent-forms]))

(defn form-input [label input-attrs]
  [:tr
   [:td
   [:label label]]
   [:td [:input.form-control input-attrs]]])

(defn mask-loading [loaded-atom component-fn]
  (if @loaded-atom
    (component-fn)
    [:div "Loading"]))

(def bind-fields reagent-forms/bind-fields)
