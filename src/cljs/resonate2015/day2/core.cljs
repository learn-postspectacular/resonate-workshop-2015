(ns resonate2015.day2.core
  (:require
   [resonate2015.day2.handlers]
   [resonate2015.day2.derivedviews]
   [resonate2015.day2.demo :as demo]
   [resonate2015.day2.components.add-shape :refer [add-shape-ops]]
   [resonate2015.day2.components.shader-select :refer [shader-selector]]
   [resonate2015.day2.components.canvas :refer [canvas]]
   [resonate2015.day2.components.fps :refer [fps-panel]]
   [cljsjs.react :as react]
   [reagent.core :as reagent]
   [re-frame.core :refer [subscribe dispatch]]))

(defn main-panel
  []
  (let [init?  (subscribe [:app-initialized?])]
    #(if @init?
       [:div
        [canvas]
        [fps-panel {:id :fps :mode :fps :width 100 :col "limegreen"}]
        [:div#hud.animated.bounceInDown
         [:h1 "Resonate workshop 2015"
          [:span.small
           "[" [:a {:href "https://github.com/learn-postspectacular/resonate-workshop-2015"}
                "GitHub"] "]"]]
         [add-shape-ops demo/shape-types]
         [:div [shader-selector (keys demo/shader-uniforms)]]]]
       [:div#loading
        [:h1 "Loading..."]])))

(defn main
  []
  (dispatch [:init-app])
  (reagent/render-component
   [main-panel] (.getElementById js/document "app")))

(main)
