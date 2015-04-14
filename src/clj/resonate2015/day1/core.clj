(ns resonate2015.day1.core)

(defn hello
  [] (prn "Zdravo!"))

(def my-map {:a {:b {:c [1 2 3] :d "string"}}})

(get-in my-map [:a :b :d 2])

(def my-set #{})

;; 
(defn test-it
  [coll]
  (filter #{:age :name} (keys coll)))


[]  ; vector
()  ; list
{}  ; hash-map / array-map
#{} ; hash-set

(filter odd? #{1 2 3 4 5})
(take 3 #{1 2 3 4 5}) ;; takes from beginning
(drop 3 #{1 2 3 4 5})
(take-last 10 (range 100))
(drop-while (fn [x] (< x 20)) (range 100))

;;(last (take 5 (iterate subdivision mesh)))

(->> mesh (iterate subdiv) (take 5) last)

(defprotocol Proto
  (as-mesh [this opts]))

(deftype FooType [a b c]
  Proto
  (as-mesh [this opts] :as-mesh))

(require '[thi.ng.geom.core :as g])
(require '[thi.ng.geom.aabb :as a])
(require '[thi.ng.geom.circle :as c])
(require '[thi.ng.geom.basicmesh :as bm])
(require '[thi.ng.geom.gmesh :as gm])
(require '[thi.ng.geom.mesh.io :as mio])
(require '[thi.ng.geom.mesh.subdivision :as sd])
(require '[clojure.java.io :as io])

(with-open [o (io/output-stream "foo.stl")]
  (let [mesh (-> (c/circle 1)
                 (g/extrude {:depth 1 :res 6 :mesh (gm/gmesh)}))]
  (->> mesh
       ;;(iterate sd/catmull-clark)
       (iterate sd/doo-sabin)
       (take 4)
       (last)
       (g/tessellate)
       (mio/write-stl o))))


(reductions (fn [acc x] (+ acc x)) 0 (filter f (range 10)))

(apply + (range 10))

(hash-seq)
