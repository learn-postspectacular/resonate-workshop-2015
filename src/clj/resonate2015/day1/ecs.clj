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

(defn new-entity!
  "Defines a new entity ID"
  [app-state]
  (-> app-state
      (swap! update-in [:uuid-counter] inc)
      (:uuid-counter)))

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
  ([app-state entity-id states]
   (swap! app-state
          update-in [:entities entity-id]
          merge states))
  ([app-state states]
    (add-components-for-eid!
      app-state
      (new-entity! app-state)
      states)))

(defn new-system!
  "Defines a new system in the given app-state atom.
  Asks for system name, a set of required component IDs and
  a system function to process all matching entities."
  [app-state name comp-ids f]
  (swap! app-state assoc-in
         [:systems name]
         {:fn f
          :comp-ids comp-ids}))

(defn make-entity-validator
  [comp-ids]
  (let [valid-entity-keys? (partial set/subset? comp-ids)]
    (fn [[_ v]]
      (valid-entity-keys? (set (keys v))))))
  
(defn run-system
  "Runs a single system fn on matching components"
  [app-state name]
  (let [{:keys [entities systems]} (deref app-state)
        {sys-fn :fn comp-ids :comp-ids} (systems name)        
        valid-entity? (make-entity-validator comp-ids)        
        matching-entities (filter valid-entity? entities)]
     (dorun
       (map
         (fn [[eid estate]] (sys-fn app-state eid estate))
         matching-entities))))


(let (a "hello" b "whatever")

(defn foo [{c :a d :b}]
  )

(run-system app-state :render)

(def a 23)



(let [a 42
       (+ a 10)
      a :foo]
  b)