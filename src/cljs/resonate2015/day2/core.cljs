(ns resonate2015.day2.core
  (:require-macros
    [cljs-log.core :refer [info warn]]
    [reagent.ratom :refer [reaction]])
  (:require
    [resonate2015.day2.ecs :as ecs :refer [run-system]]
    [resonate2015.day2.handlers]
    [resonate2015.day2.derivedviews]
    [resonate2015.day2.components.counter :as counter]
    [resonate2015.day2.components.canvas :as canvas]
    [resonate2015.day2.components.fps :as fps]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [cljsjs.react :as react]
    [reagent.core :as reagent :refer [atom]]
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(defn main-panel
  []
  (let [init? (subscribe [:app-initialized?])]
    (fn []
      (if @init?
        [:div
         [canvas/canvas2]
         [fps/fps-panel {:id :fps :mode :fps :width 200 :col "limegreen"}]
         [:div#hud
          [counter/particle-count]
          [:button {:on-click #(dispatch [:add-particles 10])} "+ add"]]]
        [:div
         [:h1 "Loading..."]]))))

(defn main
  []
  (dispatch [:init-app])
  (reagent/render-component
    [main-panel] (.getElementById js/document "app")))
    
(main)
