(ns webhookproxyweb.model
  (:require [clojure.string :as strings]
            [schema.core :as s]))

(def not-blank (with-meta
                 (s/pred #(and (string? %) (> (count (strings/trim %)) 0)))
                 {:constraint-msg "must not be blank"}))

(def WhitelistEntry 
  { :id not-blank
    :description not-blank
    :ip not-blank })

(def WebHookProxyEntry 
  {:name not-blank
   :id not-blank
   (s/optional-key :new) s/Bool
   (s/optional-key :active) s/Bool
   (s/optional-key :deleted) s/Bool
   (s/optional-key :userid) s/Str
   (s/optional-key :whitelists) [WhitelistEntry]
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

(defn error->friendly [error]
  (let [[error-key error-type] error]
    { :error (str (field->friendly error-key) " " (restriction->friendly error-type)) }))
