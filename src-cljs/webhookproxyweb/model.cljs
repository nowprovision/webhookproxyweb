(ns webhookproxyweb.model
  (:require [webhookproxyweb.schema :as schema]
            [clojure.string :as strings]
            [schema.core :as s]))


; alias to cljc migration for now
(def WhitelistEntry schema/WhitelistEntry)
(def WebHookProxyEntry schema/WebHookProxyEntry)

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
