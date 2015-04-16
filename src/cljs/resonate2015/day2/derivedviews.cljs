(ns resonate2015.day2.derivedviews
  (:require-macros
    [reagent.ratom :refer [reaction]])
  (:require
    [re-frame.core :refer [register-sub subscribe dispatch]]))

(register-sub
  :app-initialized?
  (fn [db _] (reaction (-> @db :initialized?))))

(register-sub
  :window-size
  (fn [db _] (reaction (-> @db :window-size))))

(register-sub
  :particle-count
  (fn [db _] (reaction (count (:entities @db)))))

(register-sub
  :current-shader
  (fn [db _] (reaction (:curr-shader @db))))
