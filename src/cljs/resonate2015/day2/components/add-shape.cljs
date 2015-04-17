(ns resonate2015.day2.components.add-shape
  (:require
   [resonate2015.day2.components.counter :refer [particle-count]]
   [re-frame.core :refer [dispatch]]))

(defn add-shape-button
  [type]
  [:button {:on-click #(dispatch [:add-shape type])} "+ " (name type)])

(defn add-shape-ops
  [types]
  [:div
   (for [type types] ^{:key (str "bt-" type)} [add-shape-button type])
   [particle-count]])
