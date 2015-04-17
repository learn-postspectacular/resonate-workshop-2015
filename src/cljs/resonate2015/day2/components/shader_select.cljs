(ns resonate2015.day2.components.shader-select
  (:require
   [clojure.string :as str]
   [re-frame.core :refer [subscribe dispatch]]))

(defn shader-selector
  [ids]
  (let [shader (subscribe [:current-shader])]
    (fn []
      [:select
       {:on-change #(dispatch [:set-shader (-> % .-target .-value)])
        :value @shader}
       (for [id ids]
         [:option {:key id :value id} (str/capitalize (name id))])])))
