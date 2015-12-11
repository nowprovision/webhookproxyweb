(ns webhookproxyweb.middleware.error
  (:require [circleci.rollcage.core :as rollcage]
            [taoensso.timbre :as timbre :refer (log trace  debug  info  warn  error  fatal  report
                                                    logf tracef debugf infof warnf errorf fatalf reportf
                                                    spy get-env log-env)]
            [com.stuartsierra.component :as component]
            [compojure.core :as compojure]
            [clojure.core.async :refer [go-loop chan put! <!]]
            [clojure.java.io :as io]))

(declare wrap-error-handling-factory error-page start-exception-recorder)


(defrecord ErrorRouter [error-file]
  component/Lifecycle
  (start [this]
    (let [error-ch (chan)]
      (-> this
          (assoc :error-middleware (wrap-error-handling-factory error-ch error-file))
          (assoc :stop-fn (start-exception-recorder error-ch))))) 
  (stop [this]
    ((:stop-fn this))
    this))

(defn start-exception-recorder [error-ch]
  (let [continue (atom true)]
    (go-loop []
             (let [err (<! error-ch)]
                 (error (:error err) (:req err))
              (when @continue (recur))))
      #(reset! continue false)))

(defn error-page [error-file]
  {:headers { "Content-Type" "text/html"}
   :status 500 
   :body error-file })

(defn wrap-error-handling-factory [error-ch error-file]
  (fn [handler]
    (fn [req]
      (try (handler req)
           (catch Throwable e
             (if-let [friendly (some->> e ex-data :friendly)]
               { :body {:friendly true :error (.getMessage e) } :status 500  }
               (do
                 (when error-ch 
                   (put! error-ch {:error e :req req}))
                 (error-page error-file)
                 )))))))
