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
  
(def uuid-str (s/pred (and (fn [x] (string? x) (= (count x) 36)))))

(def ip-str s/Str)

(def filter-schema 
  { :id uuid-str
    :description (size-between 1 200)
    :webhookid uuid-str
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
