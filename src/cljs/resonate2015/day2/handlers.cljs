(ns resonate2015.day2.handlers
  (:require-macros
   [cljs-log.core :refer [info warn]]
   [reagent.ratom :refer [reaction]])
  (:require
   [resonate2015.day2.tick :as tick]
   [resonate2015.day2.demo :as demo]
   [resonate2015.day2.components.fps :as fps]
   [thi.ng.geom.core.vector :refer [vec2]]
   [thi.ng.geom.rect :as r]
   [re-frame.core :refer [register-handler dispatch]]))

(defn window-size
  []
  [(.-innerWidth js/window)
   (.-innerHeight js/window)])

(defn dispatch-resize
  [e] (dispatch [:resize-window (window-size)]))

(defn dispatch-keydown
  [e] (dispatch [:keydown (.-keyCode e)]))

(defn dispatch-mousemove
  [e] (dispatch [:set-mouse-pos (.-clientX e) (.-clientY e)]))

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
                         :initialized? true))
         db   (if-not (:tick/tick db)
                (tick/init-ticker db)
                db)]
     (fps/register-fps-counter :fps-counter)
     db)))

(register-handler
 :resize-window
 (fn [db [_ size]]
   (let [size (or size (window-size))]
     (assoc db
            :window-size size
            :view-rect   (apply r/rect size)))))

(register-handler
 :canvas-mounted
 (fn [db [_ ctx]]
   (demo/start db ctx)))

(register-handler
 :set-shader
 (fn [db [_ id]]
   (assoc db :curr-shader (keyword id))))

(register-handler
 :set-mouse-pos
 (fn [db [_ x y]]
   (assoc db :mouse-pos (vec2 x y))))

(register-handler
 :keydown
 (fn [db [_ id]]
   (info :key id)
   (case id
     0x20 (dispatch [:add-shape :particles])
     0x4c (dispatch [:set-shader :lambert])
     0x50 (dispatch [:set-shader :phong])
     nil)
   db))

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
