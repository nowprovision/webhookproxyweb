(ns freeman.ospa.core
  (:require [reagent.ratom]))

(defmacro reaction [& args]
  `(reagent.ratom/reaction ~@args))
