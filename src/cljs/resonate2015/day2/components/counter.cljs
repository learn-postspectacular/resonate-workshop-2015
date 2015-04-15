(ns resonate2015.day2.components.counter
  (:require-macros
    [cljs-log.core :refer [info warn]]
    [reagent.ratom :refer [reaction]])
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(defn particle-count
  []
  (let [num (subscribe [:particle-count])]
    [:div "Particles: " @num " "]))
