(ns resonate2015.day2.components.counter
  (:require-macros
    [cljs-log.core :refer [info warn]]
    [reagent.ratom :refer [reaction]])
  (:require
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(defn particle-count
  []
  (let [num (subscribe [:particle-count])]
    [:div "Particles: " @num " "]))
