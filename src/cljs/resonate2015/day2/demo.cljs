(ns resonate2015.day2.demo
  (:require-macros
    [cljs-log.core :refer [info warn]])
  (:require
    [resonate2015.day2.ecs :as ecs]
    [resonate2015.day2.tick :as tick]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.geom.circle :as c]
    [thi.ng.geom.polygon :as p]
    [thi.ng.common.math.core :as m]
    [re-frame.core :refer [dispatch]]))

(defn make-particle
  [{[w h] :window-size :as db}]
  (ecs/register-entity
    db {:pos (g/+ (v/randvec2 (m/random 10)) (/ w 2) (/ h 2))
        :vel (v/randvec2 (m/random 1 6))
        :render :particle}))

(defn make-circle
  [{[w h] :window-size :as db}]
  (ecs/register-entity
    db {:pos (v/vec2 (m/random w) (m/random h))
        :render :circle}))

(defn make-triangle
  [{[w h] :window-size :as db}]
  (ecs/register-entity
    db {:pos (v/vec2 (m/random w) (m/random h))
        :render :triangle}))

(defn make-square
  [{[w h] :window-size :as db}]
  (ecs/register-entity
    db {:pos (v/vec2 (m/random w) (m/random h))
        :render :square}))

(defn move-entity
  [state _]
  (update state :pos g/+ (:vel state) (v/randvec2 5)))

(defn draw-circle
  [ctx [x y]]
  (.beginPath ctx)
  (.arc ctx x y 50 0 m/TWO_PI true)
  (.fill ctx))

(defn draw-poly
  [ctx [x y] res]
  (let [[[vx vy] :as verts] (-> (c/circle x y 50) (g/as-polygon res) (g/vertices))]
    (.beginPath ctx)
    (.moveTo ctx vx vy)
    (loop [i 1]
      (when (< i res)
        (.lineTo ctx ((verts i) 0) ((verts i) 1))
        (recur (inc i))))
    (.fill ctx)))

(defn draw-poly______
  [ctx [x y] res]
  (let [[[vx vy] & more] (-> (c/circle x y 50) (g/as-polygon res) (g/vertices))]
    (.beginPath ctx)
    (.moveTo ctx vx vy)
    (loop [[v & less] more]
      (when v
        (.lineTo ctx (v 0) (v 1))
        (recur less)))
    (.fill ctx)))

(defn render-entity
  [{:keys [pos render] :as state} {ctx :canvas-ctx :as db}]
  (info :render pos render)
  (when ctx
    (condp = render
      :particle (.fillRect ctx (:x pos) (:y pos) 3 3)
      :circle   (draw-circle ctx pos)
      :triangle (draw-poly ctx pos 3)
      :square   (draw-poly ctx pos 4)
      nil))
  state)

(def ecs-tick-handler
  (reify tick/PTickHandler
    (init-state
      [_ db]
      (-> db
          (merge (ecs/make-ecs))
          (ecs/register-system :movers #{:pos :vel} move-entity)
          (ecs/register-system :render #{:render} render-entity)))
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