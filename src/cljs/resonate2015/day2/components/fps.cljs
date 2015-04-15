(ns resonate2015.day2.components.fps
  (:require-macros
    [reagent.ratom :refer [reaction]]
    [cljs-log.core :refer [info]])
  (:require
    [resonate2015.day2.tick :as tick]
    [reagent.core :as reagent]
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(defn register-fps-counter
  "Sets up an FPS counter tick handler & subscriptions for current &
  average framerates. Current framerate (:fps) is updated every
  second, the average (:avg-fps) every tick and is the total avg. rate
  since the beginning."
  [db-root]
  (register-sub
   :fps (fn [db _] (reaction (get-in @db [db-root :fps]))))
  (register-sub
   :avg-fps (fn [db _] (reaction (get-in @db [db-root :avg-fps]))))
  (dispatch
   [:add-tick-handlers
    {:update-fps
     (reify tick/PTickHandler
       (init-state [_ db]
         (assoc db db-root
                {:frame 0 :total 0 :fps 0 :avg-fps 0
                 :start (.getTime (js/Date.))
                 :last  (.getTime (js/Date.))}))
       (tick [_ db]
         (let [{:keys [frame total start last]}  (db db-root)
               now   (.getTime (js/Date.))
               age   (- now start)
               frame (inc frame)
               total (inc total)
               db    (update db db-root assoc
                             :frame frame
                             :total total
                             :avg-fps (/ total (* age 1e-3)))]
           (if (> (- now last) 1000)
             (update db db-root assoc
                     :last now
                     :frame 0
                     :fps (/ frame (* (- now last) 1e-3)))
             db))))}]))

(defn- fps-graph
  "Updates canvas visualization using given component state & opts.
  Called from fps-panel render fn."
  [state fps width height col grid-col]
  (let [w' (- width 4)
        s  (/ (- height 20) 60)
        h' (- height 3)]
    (when-let [ctx (:ctx @state)]
      (swap! state update :history
             #(conj (if (< (count %) w') % (->> % (drop 1) vec)) (- h' (* s fps))))
      (let [history (:history @state)
            x    (dec (count history))
            w''  (inc w')
            g1   (- h' (* s 20))
            g2   (- h' (* s 40))
            g3   (- h' (* s 60))
            fps' (.toFixed (js/Number. fps) 2)]
        (.clearRect ctx 0 0 width height)
        (set! (.-strokeStyle ctx) grid-col)
        (set! (.-fillStyle ctx) col)
        (doto ctx
          (.beginPath)
          (.moveTo 2 h') (.lineTo w'' h')
          (.moveTo 2 g1) (.lineTo w'' g1)
          (.moveTo 2 g2) (.lineTo w'' g2)
          (.moveTo 2 g3) (.lineTo w'' g3)
          (.stroke))
        (set! (.-strokeStyle ctx) col)
        (.beginPath ctx)
        (.moveTo ctx (+ x 2) (get history x))
        (loop [x (dec x)]
          (when-not (neg? x)
            (.lineTo ctx (+ x 2) (get history x))
            (recur (dec x))))
        (.stroke ctx)
        (.fillText ctx (str "fps: " fps') 4 11)))))

(defn fps-panel
  "Framerate visualization component. Takes options map with following
  supported keys:

  :mode     - visualization mode (:fps or :avg-fps)
  :width    - canvas width (default 100)
  :height   - canvas height (default 76)
  :col      - graph color (#f0f)
  :grid-col - grid color (20/40/60 fps markers, #ccc)

  Furthermore, the map can contain :id, :class or :style keys. All
  other keys will be ignored.

  Example: [fps-panel {:mode :fps :width 200 :col \"limegreen\"}]"
  [& [opts]]
  (let [fps   (subscribe [(opts :mode :fps)])
        state (atom {})]
    (reagent/create-class
     {:display-name "fps-panel"
      :component-did-mount
      #(reset! state
               {:ctx     (.getContext (reagent/dom-node %) "2d")
                :history []})
      :reagent-render
      (fn [& [{:keys [width height col grid-col]
               :or   {width 100 height 76 col "#f0f" grid-col "#ccc"}
               :as   opts}]]
        (fps-graph state @fps width height col grid-col)
        [:canvas
         (merge {:width width :height height}
                (select-keys opts [:id :class :style]))])})))
