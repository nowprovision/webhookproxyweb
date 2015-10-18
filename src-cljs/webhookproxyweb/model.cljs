(ns webhookproxyweb.model
  (:require [clojure.string :as strings]
            [schema.core :as s]))

(def not-blank (with-meta
                 (s/pred #(and (string? %) (> (count (strings/trim %)) 0)))
                 {:constraint-msg "must not be blank"}))

(def WebHookProxyEntry 
  {:name not-blank
   :id cljs.core/UUID
   :subdomain not-blank
   :description not-blank })

(defn field->friendly [field]
  (-> field
      name
      strings/lower-case
      (strings/replace "-" " ")
      (strings/capitalize)))

(defn restriction->friendly [restriction]
  (case restriction
    'missing-required-key "required"
    (if-let [msg (:constraint-msg (meta (.-schema restriction)))]
      msg
      "not valid")))

(defn errors->friendly [errors]
  (for [[error-key error-type] errors]
    (str (field->friendly error-key) " " (restriction->friendly error-type))))
