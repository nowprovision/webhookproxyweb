(ns webhookproxyweb.components.shared)

(defn form-input [label input-attrs]
  [:tr
   [:td
   [:label label]]
   [:td [:input.form-control input-attrs]]])
