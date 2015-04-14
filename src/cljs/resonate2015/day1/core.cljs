(ns resonate2015.day1.core
  (:require-macros
    [cljs-log.core :refer [info warn]]
    [reagent.ratom :refer [reaction]])
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [thi.ng.geom.webgl.animator :refer [animate]]
    [resonate2015.day1.ecs :as ecs :refer [run-system]]
    [cljsjs.react :as react]
    [reagent.core :as reagent :refer [atom]]
    [re-frame.core :refer [register-handler
                           register-sub
                           subscribe
                           dispatch
                           trim-v]]))

(register-handler
  :add-particles
  (fn [db [_ n]]
    (reduce
      (fn [db _]
        (ecs/create-entity
          db
          {:pos (g/+ (v/randvec2 100) 320 240)
           :vel (v/randvec2 (m/random 1 6))
           :render? true}))
      db (range n))))

(register-sub
  :particle-count
  (fn [db _] (reaction (count (:entities @db)))))

(defn particle-count
  []
  (let [num (subscribe [:particle-count])]
    [:div
     "Particles: " @num
     [:button
      {:on-click (fn [] (dispatch [:add-particles 10]))}
      "Click me!"]]))


#_(defn make-particles
   [app n]
   (dotimes [i n]
     (ecs/add-components-for-eid!
       app
       {:pos (g/+ (v/randvec2 100) 320 240)
        :vel (v/randvec2 (m/random 1 6))
        :render? true})))

(defn move-particle
  [app id state]
  (swap! app update-in [:entities id :pos] g/+ (:vel state)))

(defn draw-particle
  [ctx]
  (fn [_ _ {:keys [pos] :as state}]
   (.fillRect ctx (:x pos) (:y pos) 3 3)))

(defn main
  []
  (dispatch [:add-particles 100])
  (reagent/render-component
      [particle-count]
      (.getElementById js/document "app")))

#_(defn main []
  (let [canvas (.createElement js/document "canvas")
        ctx (.getContext canvas "2d")
        w 640
        h 480]
    (set! (.-width canvas) w)
    (set! (.-height canvas) h)
    (set! (.-fillStyle ctx) "black")
    (set! (.-strokeStyle ctx) "white")
    (.appendChild (.-body js/document) canvas)
    (make-particles app 100)
    (ecs/new-system! app :movers #{:pos :vel} move-particle)
    (ecs/new-system! app :render #{:render?} (draw-particle ctx))
    
    (reagent/render-component
      [particle-count] (.getElementById js/document "app"))
    
    #_(animate
       (fn []
         (set! (.-fillStyle ctx) "black")
         (.fillRect ctx 0 0 w h)
         (set! (.-fillStyle ctx) "#f0f")
         (run-system app :movers)
         (run-system app :render)
         true))))
    
(main)
