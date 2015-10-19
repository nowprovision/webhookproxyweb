(ns webhookproxyweb.config
  (:require [com.stuartsierra.component :as component])
  (:require [clojure.string :as strings])
  (:require [schema.core :as s :include-macros true])
  (:require [schema.coerce :as coerce]))


(defrecord Config  [schema provider]
  component/Lifecycle
  (start [this]
    (assoc this :root (provider schema)))
  (stop [this]
    this))

(def SystemConfig
  { :db { :subprotocol s/Str
          :subname s/Str
          :host s/Str
          :port s/Int
          :user s/Str
          :password s/Str } })

(declare env->config)

(defn make-system-config []
  (Config. SystemConfig env->config))


(defn env_name [k]
  (-> k
      name
      strings/upper-case
      (strings/replace "-" "")))

(defn schema->env-map 
  ([schema] (schema->env-map schema [] []))
  ([schema prefix acc] 
   (mapcat (fn [[k v]]
          (let [path (conj prefix k)]
             (if (and (map? v) (nil? (:pred-name v)))
               (schema->env-map v path acc)
               [[(strings/join "_"  (map env_name path)) path]]))) schema)))

(defn env-config [schema] 
  (let [env-map (schema->env-map schema)]
    (loop [env-map env-map
           acc {}]
      (if-let [[env-key env-path] (first env-map)]
        (recur (rest env-map) (assoc-in acc env-path (System/getenv env-key)))
        acc)))) 

(def string->int (coerce/safe #(if (string? %) (Integer/parseInt %) %)))

(defn env->config [schema]
  (let [env-config (env-config schema)
        coercer (coerce/coercer schema (fn [schema] ({s/Int string->int} schema)))
        env-config-coerced (coercer env-config)]
    (when-let [parse-error (:error env-config-coerced)] 
      (throw (ex-info "Unable to parse config" parse-error)))
    env-config-coerced))

