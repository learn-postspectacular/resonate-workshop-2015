(ns resonate2015.day2.ecs
  (:require-macros
   [cljs-log.core :refer [info warn]])
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

(defn update-entity-in-systems
  [ecs eid]
  (let [estate (get-in ecs [:entities eid])]
    (update ecs :systems
            #(reduce-kv
              (fn [acc sid sys]
                (let [in? ((:entities sys) eid)]
                  (if ((:valid? sys) estate)
                    (if-not in?
                      (update-in acc [sid :entities] conj eid)
                      acc)
                    (if in?
                      (update-in acc [sid :entities] disj eid)
                      acc))))
              % %))))

(defn set-entity
  "Takes an ECS map, entity ID and entity state which is to be used as
  the entity's new state."
  [ecs eid state]
  (assoc-in ecs [:entities eid] state))

(defn update-entity
  "Takes an ECS map, entity ID and an update fn with optional args.
  Applies fn and args via update-in to entity's current state."
  [ecs eid f & args]
  (-> (apply update-in ecs [:entities eid] f args)))

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
         (update-entity eid merge comps)
         (update-entity-in-systems eid)))))

(defn remove-entity
  "Takes an ECS map and removes given entity ID from entities."
  [ecs eid] (update ecs :entities dissoc eid))

(defn entity-validator
  "Takes a seq or set of component IDs, returns a predicate fn which,
  when called will check if an entity has given components.
  Used by run-system."
  [comp-ids]
  (let [valid-entity-keys? #(set/subset? comp-ids %)]
    (fn [v] (valid-entity-keys? (set (keys v))))))

(defn register-system
  "Defines a new system in the given ECS map. Asks for system name,
  a set of required component IDs and a system function to process
  a matching entity."
  [ecs name comp-ids sys-fn]
  (let [valid? (entity-validator comp-ids)
        ids    (into #{}
                     (comp (filter (comp valid? val))
                           (map key))
                     (:entities ecs))]
    (assoc-in ecs [:systems name]
              {:fn       sys-fn
               :valid?   valid?
               :entities ids})))

(defn run-system
  "Runs a single system fn on matching components."
  [{:keys [entities systems] :as ecs} name]
  (let [{sys-fn :fn sys-entity-ids :entities} (systems name)]
    (as-> ecs ecs
      (reduce
       (fn [acc eid] (sys-fn acc eid (entities eid)))
       ecs sys-entity-ids)
      (reduce
       update-entity-in-systems
       ecs sys-entity-ids))))
