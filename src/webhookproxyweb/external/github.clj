(ns webhookproxyweb.external.github
  (:require [clj-http.client :as client]
            [clj-http.conn-mgr :as conn-mgr]
            [com.stuartsierra.component :as component]))

(defrecord GithubConnectionManager [config cm]
  component/Lifecycle
  (start [this]
    (assoc this :cm (conn-mgr/make-reusable-conn-manager {:timeout 10 :threads 10})))
  (stop [this]
    (when-let [cm (:cm this)]
      (conn-mgr/shutdown-manager cm))
    (dissoc this :cm)))

(declare code->access-token access-token->identity)

(defn authenticate [github-cm code]
  (->> { :code code }
       (code->access-token github-cm)
       (access-token->identity github-cm)))

(defn default-options [cm m] 
  (merge {:connection-manager cm 
          :accept :json
          :as :json
          :throw-exceptions false
          } m))

(defn body->ex-info-map [body]
  (cond
    (map? body) body
    (string? body) { :error-msg body }
    :else { :error-msg "bad body" }))

(defn code->access-token [{:keys [cm config]} payload]
  (let [auth-resp (client/post "https://github.com/login/oauth/access_token" 
                               (default-options cm 
                                  {:form-params {:client_id (:client-id config)
                                                 :client_secret (:client-secret config)
                                                 :code (:code payload) 
                                                 :accept :json }}))
        body (:body auth-resp)]
    (if-let [access-token (and (= (auth-resp :status) 200) (:access_token body))]
      (assoc payload :access-token access-token)
      (throw (ex-info "Unable to swap code for access token" (body->ex-info-map body))))
    ))


(defn access-token->identity [{:keys [cm]} payload]
  (let [identity-resp (client/get "https://api.github.com/user"
                                  (default-options cm
                                    {:query-params { "access_token" (:access-token payload) }}))
        body (:body identity-resp)]
    (if (and (= (identity-resp :status) 200) (map? body))
      (assoc payload :identity body)
      (throw (ex-info "Unable to retrieve identity" (body->ex-info-map body)))
      )))

