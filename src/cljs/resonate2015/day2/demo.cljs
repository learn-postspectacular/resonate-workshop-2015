(ns resonate2015.day2.demo
  (:require-macros
   [cljs-log.core :refer [info warn]])
  (:require
   [resonate2015.day2.ecs :as ecs]
   [resonate2015.day2.tick :as tick]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
   [thi.ng.geom.core.matrix :as mat :refer [M44]]
   [thi.ng.geom.aabb :as a]
   [thi.ng.geom.basicmesh :as bm]
   [thi.ng.geom.rect :as r]
   [thi.ng.geom.polygon :as p]
   [thi.ng.geom.mesh.polyhedra :as polyhedra]
   [thi.ng.geom.mesh.subdivision :as sd]
   [thi.ng.geom.webgl.core :as gl]
   [thi.ng.geom.webgl.buffers :as buffers]
   [thi.ng.geom.webgl.shaders :as shaders]
   [thi.ng.geom.webgl.shaders.lambert :as lambert]
   [thi.ng.color.core :as col]
   [thi.ng.common.math.core :as m]
   [re-frame.core :refer [dispatch]]))

(defn box-mesh
  [] (g/into (bm/basic-mesh) (g/faces (g/center (a/aabb 1)))))

(defn ico-mesh
  [iter] (polyhedra/polyhedron-mesh polyhedra/icosahedron sd/catmull-clark 1 iter))

(defn webgl-shape-spec
  [ctx mesh]
  (let [spec (-> mesh
                 (gl/as-webgl-buffer-spec {:fnormals true :tessellate true})
                 (buffers/make-attribute-buffers-in-spec ctx gl/static-draw)
                 (assoc :shader
                        (shaders/make-shader-from-spec ctx lambert/shader-spec)
                        :uniforms {:view       (mat/look-at (vec3 0 0 100) (vec3 0 1 0) (vec3 0 1 0))
                                   :ambientCol [0.2 0.2 0.2]
                                   :lightCol   [1 1 1]
                                   :lightDir   (g/normalize (vec3 0 1 0.5))
                                   :alpha      1}))]
    spec))

(defn update-shape-protos
  [db]
  (let [[w h] (:window-size db)
        proj  (gl/perspective 45 (/ w h) 0.1 1000)]
    (update db :shape-protos
            #(reduce-kv
              (fn [acc id spec] (assoc-in acc [id :uniforms :proj] proj))
              % %))))

(defn make-particles
  [{[w h] :window-size :as db}]
  (let [origin (v/randvec3)]
    (reduce
     (fn [db _]
       (ecs/register-entity
        db {:pos    (g/+ origin (v/randvec3 10))
            :vel    (v/randvec3 (m/random 0.25 1))
            :render (rand-nth [:sphere :ico :box])
            :scale  (m/random 1 5)
            :color  (col/hsv->rgb (rand) 1 1)
            :spin   {:axis (v/randvec3)
                     :theta (m/random m/TWO_PI)
                     :speed (m/random -0.2 0.2)}}))
     db (range 10))))

(defn make-hover-shape
  [db type color]
  (ecs/register-entity
   db {:orig-pos (v/randvec3 (m/random 100))
       :render type
       :color  color
       :scale  (m/random 1 10)
       :hover  (m/random m/TWO_PI)
       :hover-speed (m/random 0.01 0.05)}))

(defn make-sphere
  [db] (make-hover-shape db :sphere (col/hsv->rgb 0.08 0.33 1)))

(defn make-pyramid
  [db] (make-hover-shape db :ico (col/hsv->rgb 0.5 0.33 1)))

(defn make-box
  [db] (make-hover-shape db :box (col/hsv->rgb 0.92 0.33 1)))

(defn move-entity
  [{:keys [pos vel] :as state} {bounds :world-bounds}]
  (let [pos (g/+ pos vel)]
    (if (g/contains-point? bounds pos)
      (assoc state :pos pos))))

(defn spin-entity
  [{{:keys [speed]} :spin :as state} _]
  (update-in state [:spin :theta] + speed))

(defn hover-entity
  [{:keys [orig-pos hover hover-speed] :as state} _]
  (assoc state
         :pos   (g/+ orig-pos 0 (* (Math/sin hover) 5) 0)
         :hover (+ hover hover-speed)))

(defn webgl-render-entity
  [{:keys [pos render color scale spin] :as state}
   {ctx :canvas-ctx [w h] :window-size :as db}]
  (when ctx
    (let [model     (get-in db [:shape-protos render])
          model-mat (if spin
                      (-> M44
                          (g/translate pos)
                          (g/rotate-around-axis (:axis spin) (:theta spin))
                          (g/scale scale))
                      (-> M44
                          (g/translate pos)
                          (g/scale scale)))]
      (lambert/draw
       ctx (update-in model [:uniforms] merge
                      {:model      model-mat
                       :diffuseCol (or color [0 0 0])}))))
  state)

(def ecs-tick-handler
  (reify tick/PTickHandler
    (init-state
      [_ db]
      (-> db
          (merge (ecs/make-ecs))
          (ecs/register-system :move #{:pos :vel} move-entity)
          (ecs/register-system :spin #{:spin} spin-entity)
          (ecs/register-system :hover #{:hover} hover-entity)
          (ecs/register-system :render #{:render} webgl-render-entity)))
    (tick
      [_ {ctx :canvas-ctx [w h] :window-size :as db}]
      (when ctx
        (gl/set-viewport ctx 0 0 w h)
        (gl/clear-color-buffer ctx 0.9 0.9 0.9 1.0)
        (gl/enable ctx gl/depth-test)
        (reduce ecs/run-system db [:move :spin :hover :render])))))

(defn start
  [] (dispatch [:add-tick-handlers {:ecs ecs-tick-handler}]))
