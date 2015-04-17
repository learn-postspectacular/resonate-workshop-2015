(ns resonate2015.day2.components.canvas
  (:require-macros
   [cljs-log.core :refer [info warn]])
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [subscribe dispatch]]))

(defn canvas
  []
  (let [size (subscribe [:window-size])]
    (reagent/create-class
     {:display-name "canvas"
      :component-did-mount
      (fn [this]
        (dispatch [:canvas-mounted (.getContext (reagent/dom-node this) "webgl")]))
      :reagent-render
      (fn []
        [:canvas
         {:key "main-canvas"
          :width (@size 0)
          :height (@size 1)}])})))
