(ns webhookproxyweb.forms
  (:require-macros [cljs.core.async.macros :refer [go]]) 
  (:require [freeman.ospa.core 
             :refer [register-sub 
                     register-handler
                     subscribe 
                     dispatch 
                     resolve-route] 
             :refer-macros [reaction]]
            [webhookproxyweb.schema :as schema]
            [ajax.core :refer [POST]]
            [cljs.core.async :as async :refer [chan <! >!]]))

(register-sub :forms-changed (fn [db _] (reaction (:forms @db))))

(register-sub :form-changed (fn [db [_ form-id]]
                              (let [forms (subscribe [:forms-changed])]
                                (reaction (get @forms form-id)))))

(declare lookup-model add-form-errors sync-to-server)

(defn handle-form [{:keys [model action completed-event] :as opts}]
  (let [{:keys [schema sync-url root?]} (lookup-model model)]
    (fn [db [_ {:keys [id data form-id context] :as payload }]]
      (if-let [errors (and data (schema/check schema data))]
        (add-form-errors db form-id errors)
        (let [xsrf-token (-> db 
                             :logged-in-user 
                             :ring.middleware.anti-forgery/anti-forgery-token)]
          (go (let [resolved-url (resolve-route sync-url context)
                    sync-resp (<! (sync-to-server resolved-url id action data xsrf-token))]
            (if (:ok sync-resp)
              (dispatch [:sync-confirmed form-id action (:value sync-resp) root?
                         (vec (concat completed-event (flatten (seq context))))])
              (dispatch [:sync-errored form-id (:value sync-resp)])
              )))
         db)))))

(defn- lookup-model [model-key] 
  (case model-key
  :webhook
  {:schema schema/webhook-schema
   :sync-url "/api/webhooks" 
   :root? true }
  :filter
  {:schema schema/filter-schema
   :sync-url "/api/webhooks/:webhook-id/filters" }
  ))

(defn- add-form-errors [db form-id errors]
  (let [form-path [:forms form-id]]
    (-> db
        (assoc-in (conj form-path :valid?) 
                  false)
        (assoc-in (conj form-path :errors) 
                  (map schema/error->friendly errors)))))


(defn- sync-to-server [sync-url id action payload xsrf-token]
  (let [result-channel (chan)]
    (->> {:format :json 
          :params { :id id :action action :data payload }
          :response-format :json
          :keywords? true
          :headers {:X-XSRF-Token xsrf-token} 
          :handler (fn [resp]
                     (go (>! result-channel { :ok true :value resp })))
          :error-handler (fn [err-resp]
                           (go (>! result-channel {:ok false 
                                                   :value (:response err-resp) }))) }
         (POST sync-url))
    result-channel))

(defn add-sync-errors [db [_ form-id error]]
  (let [form-path [:forms form-id]]
    (-> db
        (assoc-in (conj form-path :valid?) false)
        (assoc-in (conj form-path :errors) [(if (:error error) error 
                                              { :error "Unexpected server error" })]))))


(defn- merge-item [coll item]
  (conj (filter #(not= (:id %) (:id item)) coll) item))

(defn- remove-item [coll item]
  (filter #(not= (:id %) (:id item)) coll))

(defn merge-sync-payload [db [_ form-id action payload root? completed-event]]
  (let [items (:items db)]
    (when completed-event (dispatch completed-event))
    (assoc db :items 
           (if (or (= action :new) (= action :modify) (not root?))
             (merge-item items payload)
             (remove-item items payload)
             ))))

(register-handler :sync-errored add-sync-errors) ; handle sync errors
(register-handler :sync-confirmed merge-sync-payload); once sync confirmed merge into ratom
