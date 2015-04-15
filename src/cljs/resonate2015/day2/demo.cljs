(ns resonate2015.day2.demo
  (:require-macros
    [cljs-log.core :refer [info warn]])
  (:require
    [resonate2015.day2.ecs :as ecs]
    [resonate2015.day2.tick :as tick]
    [thi.ng.geom.core :as g]
    [thi.ng.geom.core.vector :as v :refer [vec2]]
    [thi.ng.geom.core.matrix :as mat :refer [M44]]
    [thi.ng.geom.circle :as c]
    [thi.ng.geom.polygon :as p]
    [thi.ng.geom.webgl.core :as gl]
    [thi.ng.geom.webgl.buffers :as buffers]
    [thi.ng.geom.webgl.shaders :as shaders]    
    [thi.ng.geom.webgl.shaders.basic :as basic]    
    [thi.ng.common.math.core :as m]
    [re-frame.core :refer [dispatch]]))
  
(defn webgl-circle-spec
  [ctx radius]
  (let [spec (-> (c/circle radius)
                 (g/as-polygon 20)
                 (gl/as-webgl-buffer-spec {:normals false})
                 (buffers/make-attribute-buffers-in-spec ctx gl/static-draw)
                 (assoc :shader (shaders/make-shader-from-spec ctx
                                  (basic/make-color-shader-spec
                                    {:use-attrib false :3d false}))))]
    spec))

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

#_(comment
  
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
)

(defn webgl-render-entity
  [{:keys [pos render] :as state} {ctx :canvas-ctx :as db}]
  (let [model (get-in db [:shape-protos :circle])]
    (when ctx
      (buffers/draw-arrays
        ctx (-> model
                (update-in [:uniforms] merge
                           {:proj  (gl/ortho)
                            :model (-> M44 (g/translate 0 0 0))
                            :color [0 1 1 1]}))))))

(def ecs-tick-handler
  (reify tick/PTickHandler
    (init-state
      [_ db]
      (-> db
          (merge (ecs/make-ecs))
          (ecs/register-system :movers #{:pos :vel} move-entity)
          (ecs/register-system :render #{:render} webgl-render-entity)))
    (tick
      [_ {ctx :canvas-ctx [w h] :window-size :as db}]
      (when ctx
        ;;(.clearRect ctx 0 0 w h)
        ;;(set! (.-fillStyle ctx) "#f0f")
        (gl/set-viewport ctx 0 0 w h)
        (gl/clear-color-buffer ctx 1 1 0 1.0)
        )
      (-> db
          (ecs/run-system :movers)
          (ecs/run-system :render)))))

(defn start
  [] (dispatch [:add-tick-handlers {:ecs ecs-tick-handler}]))