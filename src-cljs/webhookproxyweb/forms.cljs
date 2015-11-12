(ns webhookproxyweb.forms
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]) 
  (:require [cljs.core.async :as async :refer [chan <! >!]]
            [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [ajax.core :refer [GET POST]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

(register-sub :forms-changed (fn [db _] (reaction (:forms @db))))

(register-sub :form-changed (fn [db [_ form-id]]
                              (let [forms (subscribe [:forms-changed])]
                                (reaction (get @forms form-id)))))

(declare lookup-model add-form-errors sync-to-server)

(defn handle-form [{:keys [model action completed-event] :as opts}]
  (let [{:keys [schema sync-url]} (lookup-model model)]
    (fn [db [_ {:keys [id data form-id context] :as payload }]]
      (if-let [errors (and data (s/check schema data))]
        (add-form-errors db form-id errors)
        (let [xsrf-token (-> db 
                             :logged-in-user 
                             :ring.middleware.anti-forgery/anti-forgery-token)]
          (go (let [resolved-url (secretary.core/render-route sync-url (or context {}))
                    sync-resp (<! (sync-to-server resolved-url id action data xsrf-token))]
            (if (:ok sync-resp)
              (dispatch [:sync-confirmed form-id action (:value sync-resp) 
                         (concat completed-event (flatten (seq context)))])
              (dispatch [:sync-errored form-id sync-resp])
              )))
         db)))))

(defn- lookup-model [model-key] 
  (case model-key
  :webhook
  {:schema model/WebHookProxyEntry 
   :sync-url "/api/webhooks" }
  :filter
  {:schema model/WhitelistEntry 
   :sync-url "/api/webhooks/:webhook-id/filters" }
  ))

(defn- add-form-errors [db form-id errors]
  (let [form-path [:forms form-id]]
    (-> db
        (assoc-in (conj form-path :valid?) 
                  false)
        (assoc-in (conj form-path :errors) 
                  (map model/error->friendly errors)))))


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
                           (go (>! result-channel { :ok false :value err-resp }))) }
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

(defn merge-sync-payload [db [_ form-id action payload completed-event]]
  (let [items (:items db)]
    (when completed-event (dispatch completed-event))
    (assoc db :items 
           (if (or (= action :new) (= action :modify))
             (merge-item items payload)
             (remove-item items payload)
             ))))

(register-handler :sync-errored add-sync-errors) ; handle sync errors
(register-handler :sync-confirmed merge-sync-payload); once sync confirmed merge into ratom
