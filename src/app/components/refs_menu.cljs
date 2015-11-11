(ns app.components.refs-menu
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui RefMenuItem
  static om/Ident
  (ident [this props]
    [:ref/by-name (:name props)])
  static om/IQuery
  (query [this]
    [:name :head])
  Object
  (render [this]
    (println "Render RefMenuItem" (:name (om/props this)))
    (let [{:keys [name]} (om/props this)
          {:keys [activate-fn]} (om/get-computed this)]
      (dom/li nil
        (dom/a #js {:onClick #(when activate-fn
                                (activate-fn (om/get-ident this)))}
          name)))))

(def ref-menu-item (om/factory RefMenuItem {:keyfn :name}))

(defui RefsMenu
  Object
  (render [this]
    (println "Render RefsMenu")
    (let [{:keys [refs]} (om/props this)
          {:keys [create-fn activate-fn]} (om/get-computed this)]
      (dom/div #js {:className "header-menu"}
        (dom/a nil "Branches")
        (dom/ul nil
          (for [ref refs]
            (ref-menu-item (om/computed ref {:activate-fn activate-fn})))
          (dom/li #js {:className "header-menu-separator"})
          (dom/li nil
            (dom/a #js {:onClick #(when create-fn (create-fn))}
              "Create")))))))

(def refs-menu (om/factory RefsMenu))
