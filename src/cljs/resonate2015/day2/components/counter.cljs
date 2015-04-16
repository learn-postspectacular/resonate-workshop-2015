(ns resonate2015.day2.components.counter
  (:require
   [re-frame.core :refer [subscribe]]))

(defn particle-count
  []
  (let [num (subscribe [:particle-count])]
    [:span (str "Particles: " @num)]))
