(ns resonate2015.day1.core
  (:require-macros
    [cljs-log.core :refer [info warn]])
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [resonate2015.day1.ecs :as ecs :refer [run-system]]))

(defonce app (ecs/make-app))

(defn make-particles
  [n]
  (dotimes [i n]
    (ecs/add-components-for-eid!
      app
      {:pos (v/randvec2 100)
       :vel (v/randvec2 (m/random 1 6))})))
      
(defn main []
  (let [canvas (.createElement js/document "canvas")
        ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) "black")
    (set! (.-strokeStyle ctx) "white")
    (.fillRect ctx 0 0 100 100)
    (.appendChild (.-body js/document) canvas)))
    

(main)
