(ns resonate2015.day2.components.add-shape
  (:require
   [re-frame.core :refer [dispatch]]))

(defn add-shape-button
  [type]
  [:button {:on-click #(dispatch [:add-shape type])} "+ " (name type)])
