(ns resonate2015.day2.handlers
  (:require-macros
   [cljs-log.core :refer [info warn]]
   [reagent.ratom :refer [reaction]])
  (:require
   [resonate2015.day2.tick :as tick]
   [resonate2015.day2.demo :as demo]
   [resonate2015.day2.components.fps :as fps]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.rect :as r]
   [thi.ng.geom.aabb :as a]
   [re-frame.core :refer [register-handler dispatch]]))

(defn window-size
  []
  [(.-innerWidth js/window)
   (.-innerHeight js/window)])

(defn dispatch-resize
  []
  (dispatch [:resize-window (window-size)]))

(defn dispatch-keydown
  [e]
  (dispatch [:keydown (-> e .-target .-keyCode)]))

(defn dispatch-mousemove
  [e])

(defn init-dom-events
  [db]
  (assoc db
         :resize  (or (:resize db)
                      (do (.addEventListener js/window "resize" dispatch-resize)
                          dispatch-resize))
         :keydown (or (:keydown db)
                      (do (.addEventListener js/window "keydown" dispatch-keydown)
                          dispatch-keydown))
         :mousemove (if-let [m (:mousemove db)]
                      m
                      (do (.addEventListener js/window "mousemove" dispatch-mousemove)
                          dispatch-mousemove))))

(register-handler
 :init-app
 (fn [db _]
   (let [size (window-size)
         db   (-> db
                  (init-dom-events)
                  (assoc :window-size  size
                         :view-rect    (apply r/rect size)
                         :world-bounds (g/center (a/aabb 150))
                         :curr-shader  :lambert
                         :initialized? true))
         db   (if-not (:tick/tick db)
                (tick/init-ticker db)
                db)]
     (fps/register-fps-counter :fps-counter)
     (demo/start)
     db)))

(register-handler
 :resize-window
 (fn [db [_ size]]
   (let [size (or size (window-size))]
     (-> db
         (assoc :window-size  size
                :view-rect    (apply r/rect size))
         (demo/update-shape-protos)))))

(register-handler
 :canvas-mounted
 (fn [db [_ ctx]]
   (demo/init-webgl db ctx)))

(register-handler
 :set-shader
 (fn [db [_ id]]
   (assoc db :curr-shader (keyword id))))

(register-handler
 :add-shape
 (fn [db [_ type]]
   (info :add-shape type)
   (case type
     :sphere    (demo/make-sphere db)
     :ico       (demo/make-pyramid db)
     :box       (demo/make-box db)
     :particles (demo/make-particles db)
     (do
       (warn "invalid shape type" type)
       db))))
