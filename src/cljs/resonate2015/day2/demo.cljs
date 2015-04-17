(ns resonate2015.day2.demo
  (:require-macros
   [cljs-log.core :refer [info warn]])
  (:require
   [resonate2015.day2.ecs :as ecs]
   [resonate2015.day2.tick :as tick]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec3 randvec3]]
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
   [thi.ng.geom.webgl.shaders.phong :as phong]
   [thi.ng.color.core :as col]
   [thi.ng.common.math.core :as m]
   [re-frame.core :refer [dispatch]]))

(def shape-types [:particles :sphere :ico :box])

(def shader-uniforms
  {:lambert {:lightCol      [1 0.8 0.5]
             :lightDir      (g/normalize (vec3 0 1 0.5))
             :alpha         1}
   :phong   {:shininess     32
             :specularCol   [1 1 1]
             :lightPos      [-100 100 150]
             :useBlinnPhong true
             :wrap          0}})

(defn box-mesh
  [] (->> (a/aabb 1) (g/center) (g/faces) (g/into (bm/basic-mesh))))

(defn ico-mesh
  [iter] (polyhedra/polyhedron-mesh polyhedra/icosahedron sd/catmull-clark 1 iter))

(defn init-shaders
  [ctx]
  {:lambert (shaders/make-shader-from-spec ctx lambert/shader-spec)
   :phong   (shaders/make-shader-from-spec ctx phong/shader-spec)})

(defn webgl-shape-spec
  [ctx mesh]
  (-> mesh
      (gl/as-webgl-buffer-spec {:fnormals true :tessellate true})
      (buffers/make-attribute-buffers-in-spec ctx gl/static-draw)
      (assoc :uniforms {:ambientCol [0.2 0.2 0.25]})))

(defn update-shape-protos
  [{[x y] :mouse-pos [w h] :window-size :as db}]
  (let [proj  (gl/perspective 45 (/ w h) 0.1 1000)
        view  (-> (mat/look-at (vec3 0 0 100) (vec3 0 1 0) (vec3 0 1 0))
                  (g/rotate-x (* 0.01 y))
                  (g/rotate-y (* 0.01 x)))]
    (update db :shape-protos
            #(reduce-kv
              (fn [acc id spec]
                (update-in acc [id :uniforms] merge
                           {:proj proj
                            :view view}))
              % %))))

(defn make-hover-shape
  [db type color]
  (ecs/register-entity
   db {:orig-pos    (randvec3 (m/random 80))
       :render      type
       :color       color
       :scale       (m/random 1 10)
       :hover       (m/random m/TWO_PI)
       :hover-speed (m/random 0.01 0.05)}))

(defmulti make-shape (fn [db type] type))

(defmethod make-shape :default
  [db type] (warn "unknown shape type:" type) db)

(defmethod make-shape :particles
  [{[w h] :window-size :as db} _]
  (let [origin (randvec3)]
    (reduce
     (fn [db _]
       (ecs/register-entity
        db {:pos    (g/+ origin (randvec3 10))
            :vel    (randvec3 (m/random 0.25 1))
            :render (rand-nth [:sphere :ico :box])
            :scale  (m/random 1 5)
            :color  (col/hsv->rgb (rand) 1 1)
            :spin   {:axis (randvec3)
                     :theta (m/random m/TWO_PI)
                     :speed (m/random -0.2 0.2)}}))
     db (range 10))))

(defmethod make-shape :sphere
  [db type] (make-hover-shape db type (col/hsv->rgb 0.08 0.33 1)))

(defmethod make-shape :ico
  [db type] (make-hover-shape db type (col/hsv->rgb 0.5 0.33 1)))

(defmethod make-shape :box
  [db type] (make-hover-shape db type (col/hsv->rgb 0.92 0.33 1)))

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
    (let [model       (get-in db [:shape-protos render])
          shader-type (db :curr-shader :lambert)
          model-mat   (if spin
                        (-> M44
                            (g/translate pos)
                            (g/rotate-around-axis (:axis spin) (:theta spin))
                            (g/scale scale))
                        (-> M44
                            (g/translate pos)
                            (g/scale scale)))]
      (lambert/draw
       ctx
       (-> model
           (assoc :shader (get-in db [:shaders shader-type]))
           (update-in [:uniforms] merge
                      (shader-uniforms shader-type)
                      {:model      model-mat
                       :diffuseCol color})))))
  state)

(def demo-handler
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
      (if ctx
        (do
          (gl/set-viewport ctx 0 0 w h)
          (gl/clear-color-buffer ctx 0.9 0.9 0.9 1.0)
          (gl/enable ctx gl/depth-test)
          (reduce
           ecs/run-system
           (update-shape-protos db)
           [:move :spin :hover :render]))
        db))))

(defn start
  [db ctx]
  (dispatch [:add-tick-handlers {:demo demo-handler}])
  (-> db
      (assoc :canvas-ctx   ctx
             :curr-shader  :phong
             :view-rect    (apply r/rect (:window-size db))
             :world-bounds (g/center (a/aabb 150))
             :shaders      (init-shaders ctx)
             :shape-protos {:sphere (webgl-shape-spec ctx (ico-mesh 1))
                            :ico    (webgl-shape-spec ctx (ico-mesh 0))
                            :box    (webgl-shape-spec ctx (box-mesh))})))
