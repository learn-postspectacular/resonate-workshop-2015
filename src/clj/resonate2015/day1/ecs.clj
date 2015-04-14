(ns resonate2015.day1.ecs)

;; (component :position {:x 0 :y 10})

;; 1 [pos col vel renderable] ;; player
;; 2 [skill pos vel]

;; 1 {:pos {:x 1 :y 0} :color :red :renderable true}
;; 2 {:pos {:x ...} :vel {....}

;; color
;; vel

;; {1 [{:name :x :y} {:name :color} ...]
;;  2 [{:name :x :y}]
;;  }

(defn make-app
  []
  (atom {:uuid-counter 0
         :entities     {} 
         :systems      {}}))

(defn component
  "Defines a new component"
  [app-state entity-id name state]
  (assoc-in app-state [:entities entity-id name] state))

(defn add-components-for-eid
  "Updates a app state map with new entity states."
  [app-state entity-id states]
  (update-in app-state [:entities entity-id]
         merge states))

(defn add-components-for-eid!
  "Swaps a app state atom with new entity states."
  [app-state entity-id states]
  (swap! app-state
         update-in [:entities entity-id]
         merge states))


(defn new-entity!
  "Defines a new entity ID"
  [app-state]
  (-> app-state
      (swap! update-in [:uuid-counter] inc)
      (:uuid-counter)))

(defn system
  "Defines a new system"
  [name f]
  )
