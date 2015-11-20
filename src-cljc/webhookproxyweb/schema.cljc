(ns webhookproxyweb.schema
  (:require [clojure.string :as strings]
            [schema.core :as s]))

(def not-blank (with-meta
                 (s/pred #(and (string? %) (> (count (strings/trim %)) 0)))
                 {:constraint-msg "must not be blank"}))

(defn size-between [len-min len-max]
  (with-meta
    (s/pred (fn [s] (and (string? s) (>= (count s) len-min) (<= (count s) len-max))))
    { :constraint-msg (str "must be between " len-min " and " len-max " in length") } ))
  
(def uuid-str (s/pred (fn [x] (and (string? x) (= (count x) 36)))))


;; TODO: better validation for ipv4, consider ipv6 too
(def ip-str (with-meta
              (s/pred (fn [x] (and (string? x) (re-find #"^(\d{1,3}\.){3}\d{1,3}$" x))))
              { :constraint-msg "must ipv4 and in form 1.2.3.4" }))

(def filter-schema 
  { :id uuid-str
    :description (size-between 1 200)
    :ip ip-str })

(def webhook-schema 
  {:id uuid-str
   :name (size-between 1 50) 
   :subdomain (size-between 1 200)
   :secret (size-between 8 50) 
   :description (size-between 1 200)
   (s/optional-key :filters) [filter-schema]
   })


#?(:clj (def webhook-db-schema (merge webhook-schema { :userid uuid-str } )))

; alias check
(def check s/check)
(def validate s/validate)
(def maybe s/maybe)
(def enum s/enum)

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
