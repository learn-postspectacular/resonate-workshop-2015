(ns resonate2015.day2.core
  (:require-macros
   [cljs-log.core :refer [info warn]])
  (:require
   [resonate2015.day2.handlers]
   [resonate2015.day2.derivedviews]
   [resonate2015.day2.components.shader-select :refer [shader-selector]]
   [resonate2015.day2.components.counter :refer [particle-count]]
   [resonate2015.day2.components.canvas :as canvas]
   [resonate2015.day2.components.fps :as fps]
   [resonate2015.day2.demo :as demo]
   [cljsjs.react :as react]
   [reagent.core :as reagent]
   [re-frame.core :refer [subscribe dispatch]]))

(defn add-shape-button
  [type]
  [:button {:on-click #(dispatch [:add-shape type])} "+ " (name type)])

(defn main-panel
  []
  (let [init?  (subscribe [:app-initialized?])]
    #(if @init?
       [:div
        [canvas/canvas2]
        [fps/fps-panel {:id :fps :mode :fps :width 200 :col "limegreen"}]
        [:div#hud.animated.bounceInDown
         [:h1 "Resonate workshop 2015"]
         [:div
          (for [type demo/shape-types]
            ^{:key (str "bt-" type)} [add-shape-button type])
          [particle-count]]
         [:div
          [shader-selector (keys demo/shader-uniforms)]]]]
       [:div#loading
        [:h1 "Loading..."]])))

(defn main
  []
  (dispatch [:init-app])
  (reagent/render-component
   [main-panel] (.getElementById js/document "app")))

(main)
