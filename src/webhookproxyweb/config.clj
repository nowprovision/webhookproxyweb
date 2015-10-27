(ns webhookproxyweb.config
  (:require [clojure.edn :as edn]
            [schema.coerce :as coerce]
            [schema.core :as s]))

(def ^{:version 1} SystemConfig
  "Schema ~type for system config"
  {:http-server {:port s/Int}
   (s/optional-key :debug) s/Bool
   :static { :root-path s/Str }
   :github-auth {:client-id s/Str
                 :client-secret s/Str }
   :db          {:subprotocol s/Str
                 :subname     s/Str
                 :host        s/Str
                 :port        s/Int
                 :user        s/Str
                 :password    s/Str}})

(def ^:private string->int (coerce/safe #(if (string? %) (Integer/parseInt %) %)))

(defn edn->config 
  "parse edn file into a config map, type forgiving via coercing"
  ([file] (edn->config SystemConfig file))
  ([schema file]
   (let [r (slurp file)
         raw (edn/read-string r)
         coercer (coerce/coercer schema (fn [schema] ({s/Int string->int} schema)))
         env-config-coerced (coercer raw)]
     (when-let [parse-error (:error env-config-coerced)] 
       (throw (ex-info "Unable to parse config" parse-error)))
     env-config-coerced)))


(comment
  "dead environment variable style"
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
  )

