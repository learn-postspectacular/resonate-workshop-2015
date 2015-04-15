(ns resonate2015.day2.ecs
  (:require
    [clojure.set :as set]))

(defn make-ecs
  "Returns a fresh entity-component-system map."
  []
  {:eid      0
   :entities {}
   :systems  {}})

(defn set-component
  "Takes an ECS map, entity ID, component name and component state,
  sets component for entity and returns updated map."
  [ecs entity-id name state]
  (assoc-in ecs [:entities entity-id name] state))

(defn update-entity
  "Takes an ECS map, entity ID and an update fn with optional args.
  Applies fn and args via update-in to entity's current state."
  [ecs entity-id f & args]
  (apply update-in ecs [:entities entity-id] f args))

(defn register-entity
  "Takes an ECS map and optional map of component states.
  Creates a new unique entity ID and associates state with entity.
  Returns updated map."
  ([ecs]
    (register-entity ecs {}))
  ([ecs comps]
    (let [eid (inc (:eid ecs))]
      (-> ecs
          (assoc :eid eid)
          (update-entity eid merge comps)))))

(defn entity-validator
  "Takes a seq or set of component IDs, returns a predicate fn which,
  when called will check if an entity has given components.
  Used by run-system."
  [comp-ids]
  (let [valid-entity-keys? #(set/subset? comp-ids %)]
    (fn [[_ v]]
      (valid-entity-keys? (set (keys v))))))

(defn register-system
  "Defines a new system in the given ECS map. Asks for system name,
  a set of required component IDs and a system function to process
  a matching entity."
  [ecs name comp-ids sys-fn]
  (assoc-in ecs [:systems name]
            {:fn       sys-fn
             :required (filter (entity-validator comp-ids))}))
  
(defn run-system
  "Runs a single system fn on matching components"
  [{:keys [entities systems] :as ecs} name]
  (let [{sys-fn :fn filter-fn :required} (systems name)]
    (->> entities
         (transduce
           filter-fn
           (completing (fn [acc [eid]] (update acc eid sys-fn ecs)))
           entities)
         (assoc ecs :entities))))
