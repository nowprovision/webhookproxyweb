(ns webhookproxyweb.figwheel
  (:require [figwheel-sidecar.repl-api :as ra]
            [com.stuartsierra.component :as component]))

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (ra/start-figwheel! config)
    config)
  (stop [config]
    (ra/stop-figwheel!)
    config))
