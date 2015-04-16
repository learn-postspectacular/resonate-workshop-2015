(ns resonate2015.day2.handlers
  (:require-macros
   [cljs-log.core :refer [info warn]]
   [reagent.ratom :refer [reaction]])
  (:require
   [resonate2015.day2.tick :as tick]
   [resonate2015.day2.demo :as demo]
   [resonate2015.day2.components.fps :as fps]
   [thi.ng.geom.rect :as r]
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
         (assoc :window-size size
                :view-rect   (apply r/rect size))
         (demo/update-shape-protos)))))

(register-handler
 :canvas-mounted
 (fn [db [_ ctx]]
   (info :webgl-ready)
   (-> db
       (assoc :canvas-ctx ctx
              :shape-protos {:circle   (demo/webgl-shape-spec ctx 20)
                             :triangle (demo/webgl-shape-spec ctx 3)
                             :square   (demo/webgl-shape-spec ctx 4)})
       (demo/update-shape-protos))))

(register-handler
 :add-particles
 (fn [db [_ n]]
   (reduce
    (fn [db _] (demo/make-particle db))
    db (range n))))

(register-handler
 :add-shape
 (fn [db [_ type]]
   (info :add-shape type)
   (case type
     :circle   (demo/make-circle db)
     :triangle (demo/make-triangle db)
     :square   (demo/make-square db)
     :particle (demo/make-particle db)
     (warn "invalid shape type" type))))
