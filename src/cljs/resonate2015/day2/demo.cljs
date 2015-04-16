(ns resonate2015.day2.demo
  (:require-macros
   [cljs-log.core :refer [info warn]])
  (:require
   [resonate2015.day2.ecs :as ecs]
   [resonate2015.day2.tick :as tick]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
   [thi.ng.geom.core.matrix :as mat :refer [M44]]
   [thi.ng.geom.circle :as c]
   [thi.ng.geom.rect :as r]
   [thi.ng.geom.polygon :as p]
   [thi.ng.geom.webgl.core :as gl]
   [thi.ng.geom.webgl.buffers :as buffers]
   [thi.ng.geom.webgl.shaders :as shaders]
   [thi.ng.geom.webgl.shaders.basic :as basic]
   [thi.ng.common.math.core :as m]
   [re-frame.core :refer [dispatch]]))

(defn webgl-shape-spec
  [ctx res]
  (info :webgl-spec)
  (let [spec (-> (c/circle 1)
                 (g/as-polygon res)
                 (gl/as-webgl-buffer-spec {:normals false})
                 (buffers/make-attribute-buffers-in-spec ctx gl/static-draw)
                 (assoc :shader
                        (->> (basic/make-color-shader-spec
                              {:use-attrib false :3d false})
                             (shaders/make-shader-from-spec ctx))))]
    spec))

(defn update-shape-protos
  [db]
  (let [[w h] (:window-size db)
        proj (mat/ortho 0 0 w h -1 1)]
    (update db :shape-protos
            #(reduce-kv
              (fn [acc id spec]
                (assoc-in acc [id :uniforms :proj] proj))
              % %))))

(defn make-particle
  [{[w h] :window-size :as db}]
  (let [origin (vec2 (m/random w) (m/random h))]
    (reduce
     (fn [db _]
       (ecs/register-entity
        db {:pos (g/+ origin (v/randvec2 10))
            :vel (v/randvec2 (m/random 2 10))
            :render :square
            :scale (m/random 2 20)
            :color [(rand) (rand) (rand) 1]}))
     db (range 10))))

(defn make-circle
  [{[w h] :window-size :as db}]
  (ecs/register-entity
   db {:pos (v/vec2 (m/random w) (m/random h))
       :render :circle
       :scale (m/random 5 50)
       }))

(defn make-triangle
  [{[w h] :window-size :as db}]
  (ecs/register-entity
   db {:pos (v/vec2 (m/random w) (m/random h))
       :render :triangle
       :scale (m/random 5 50)}))

(defn make-square
  [{[w h] :window-size :as db}]
  (ecs/register-entity
   db {:pos (v/vec2 (m/random w) (m/random h))
       :render :square
       :scale (m/random 5 50)}))

(defn move-entity
  [{:keys [pos vel] :as state} {bounds :view-rect}]
  (let [pos (g/+ pos vel (v/randvec2 5))]
    (if (g/contains-point? bounds pos)
      (assoc state :pos pos))))

(defn webgl-render-entity
  [{:keys [pos render color scale] :as state}
   {ctx :canvas-ctx [w h] :window-size :as db}]
  (when ctx
    (let [model (get-in db [:shape-protos render])]
      (buffers/draw-arrays
       ctx (-> model
               (update-in
                [:uniforms] merge
                {:model (-> M44 (g/translate (vec3 pos)) (g/scale scale))
                 :color (or color [0 0 0 1])})))))
  state)

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
        (gl/set-viewport ctx 0 0 w h)
        (gl/clear-color-buffer ctx 0.9 0.9 0.9 1.0))
      (-> db
          (ecs/run-system :movers)
          (ecs/run-system :render)))))

(defn start
  [] (dispatch [:add-tick-handlers {:ecs ecs-tick-handler}]))
