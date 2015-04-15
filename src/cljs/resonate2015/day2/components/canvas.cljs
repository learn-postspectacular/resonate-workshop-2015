(ns resonate2015.day2.components.canvas
  (:require-macros
    [cljs-log.core :refer [info warn]]
    [reagent.ratom :refer [reaction]])
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [reagent.core :as reagent]
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(defn canvas2
  []
  (let [size (subscribe [:window-size])]
    (reagent/create-class
      {:display-name "canvas"
       :component-did-mount
       (fn [this]
         (dispatch [:canvas-mounted (.getContext (reagent/dom-node this) "2d")]))
       :reagent-render
       (fn []
         [:canvas
          {:key "main-canvas"
           :width (@size 0)
           :height (@size 1)}])})))