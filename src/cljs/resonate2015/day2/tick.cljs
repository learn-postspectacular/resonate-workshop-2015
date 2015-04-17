(ns resonate2015.day2.tick
  (:require-macros
    [cljs-log.core :refer [info warn]])
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [register-handler dispatch trim-v]]))

(defprotocol PTickHandler
  (init-state
    [_ db]
    "Called to during :add-tick-handlers event handling to allow
    modification of app-db. Must return updated db.")
  (tick
    [_ db]
    "Called at each tick w/ current app-db.
    Must return updated db."))

(defn- re-trigger-ticker
  "Dispatches :next-tick event at next React redraw cycle (usually
  every 16ms, but depending on CPU load)."
  [] (reagent/next-tick (fn [] (dispatch [:next-tick]))))

(defn init-ticker
  "MUST be called with app-db map from a pure re-frame handler (e.g.
  during initial app init event). Registers tick related events &
  handlers and adds initial ::tick state to given db map.

  Tick handlers can be added via :add-tick-handlers event. These
  handlers are NOT re-frame handlers and MUST implement the
  PTickHandler protocol instead, for example:

  (dispatch
    [:add-tick-handlers
     {:foo (reify PTickHandler
             (init-state [_ db] (assoc db :foo {:state 0}))
             (tick [_ db] (update-in db [:foo :state] inc)))}])"
  [db]
  (register-handler
   :add-tick-handlers trim-v
   (fn [db [handlers]]
     (info "adding tick handlers:" (keys handlers))
     (reduce-kv
      (fn [db id handler] (init-state handler db))
      (update-in db [::tick :handlers] merge handlers)
      handlers)))

  (register-handler
   :remove-tick-handlers trim-v
   (fn [db [ids]]
     (info "removing tick handlers:" ids)
     (update-in db [::tick :handlers] #(apply dissoc % ids))))

  (register-handler
   :next-tick trim-v
   (fn [{ticker ::tick :as db} _]
     (if-not (:paused? ticker)
       (do (re-trigger-ticker)
           (reduce-kv
            (fn [db id handler] (tick handler db))
            db (:handlers ticker)))
       db)))

  (re-trigger-ticker)
  
  (assoc db ::tick {:handlers {} :paused? false}))
