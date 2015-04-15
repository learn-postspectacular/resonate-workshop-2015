(ns resonate2015.day2.demo
  (:require-macros
    [cljs-log.core :refer [info warn]])
  (:require
    [resonate2015.day2.ecs :as ecs]
    [resonate2015.day2.tick :as tick]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.common.math.core :as m]
    [re-frame.core :refer [dispatch]]))

(defn make-particle
  [db]
  (ecs/register-entity
    db {:pos (g/+ (v/randvec2 (m/random 10)) 320 240)
        :vel (v/randvec2 (m/random 1 6))
        :render? true}))

(defn move-entity
  [state _]
  (update state :pos g/+ (:vel state)))

(defn render-entity
  [{:keys [pos] :as state} {ctx :canvas-ctx :as db}]
  (when ctx
    (.fillRect ctx (:x pos) (:y pos) 3 3))
  state)

(def ecs-tick-handler
  (reify tick/PTickHandler
    (init-state
      [_ db]
      (-> db
          (merge (ecs/make-ecs))
          (ecs/register-system :movers #{:pos :vel} move-entity)
          (ecs/register-system :render #{:render?} render-entity)))
    (tick
      [_ {ctx :canvas-ctx [w h] :window-size :as db}]
      (when ctx
        (.clearRect ctx 0 0 w h)
        (set! (.-fillStyle ctx) "#f0f"))
      (-> db
          (ecs/run-system :movers)
          (ecs/run-system :render)))))

(defn start
  [] (dispatch [:add-tick-handlers {:ecs ecs-tick-handler}]))