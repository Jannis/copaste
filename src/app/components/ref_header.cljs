(ns app.components.ref-header
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui RefHeader
  static om/Ident
  (ident [this props]
    [:ref/by-name (:name props)])
  static om/IQuery
  (query [this]
    [:name :head])
  Object
  (render [this]
    (let [{:keys [name head]} (om/props this)]
      (dom/p #js {:className "ref-header"}
        (dom/strong nil "Branch:")
        (dom/span nil name)
        (dom/span nil (some->> head :sha1 (str "@")))))))

(def ref-header (om/factory RefHeader {:keyfn :name}))
