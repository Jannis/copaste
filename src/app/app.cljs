(ns app.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [app.parsing.app]
            [app.parsing.copaste]
            [app.reconciler :refer [reconciler]]
            [app.components.snippet :refer [Snippet]]
            [app.components.snippet-list :refer [snippet-list]]
            [app.components.ref-header :refer [RefHeader ref-header]]
            [app.components.refs-menu :refer [RefMenuItem refs-menu]]))

(defui App
  static om/IQueryParams
  (params [this]
    {:ref [:ref/by-name "HEAD"]})
  static om/IQuery
  (query [this]
    `[({:copaste/ref ~(om/get-query RefHeader)} {:ident ?ref})
      {:copaste/refs ~(om/get-query RefMenuItem)}
      ({:copaste/snippets ~(om/get-query Snippet)} {:ref ?ref})])
  Object
  (activate-ref [this ident]
    (om/set-params! this {:ref ident})
    (om/transact! this `[(app/set-ref {:ident ~ident})
                         :copaste/snippets]))

  (reset-ui [this]
    (om/transact! this `[(app/set-snippet nil)]))

  (set-snippet [this ident]
    (om/transact! this `[(app/set-snippet {:ident ~ident})]))

  (toggle-expanded [this ident]
    (om/transact! this `[(app/toggle-expanded {:ident ~ident})]))

  (create-snippet [this]
    (om/transact! this `[(copaste/create-snippet)]))

  (toggle-editing [this ident]
    (om/transact! this `[(app/toggle-editing {:ident ~ident})]))

  (update-snippet [this ident props]
    (om/transact! this `[(app/update-snippet {:ident ~ident :props ~props})]))

  (save-snippet [this ident]
    (let [ref (:app/ref (om/props this))
          snippet (->> (:copaste/snippets (om/props this))
                       (filter #(= (second ident) (:uuid %)))
                       (first))]
      (om/transact! this
                    `[(copaste/save-snippet {:ref ~ref :snippet ~snippet})
                      :copaste/ref
                      :copaste/refs
                      :copaste/snippets])))

  (render [this]
    (println "Render App")
    (dom/div #js {:className "app"}
      (dom/header #js {:className "header"}
        (dom/h1 #js {:className "header-title"
                     :onClick #(.reset-ui this nil)}
          (dom/a nil "copaste")
          (dom/span #js {:className "header-subtitle"}
            "Store and recover code snippets")))
      (dom/main #js {:className "main"}
        (when-let [ref (:copaste/ref (om/props this))]
          (ref-header ref))
        (let [snippets (:copaste/snippets (om/props this))]
          (snippet-list
            (om/computed {:snippets snippets}
                         {:toggle-fn #(.toggle-expanded this %)
                          :create-fn #(.create-snippet this %)
                          :edit-fn #(.toggle-editing this %)
                          :update-fn #(.update-snippet this %1 %2)
                          :save-fn #(.save-snippet this %1)})))))))

(defn run []
  (enable-console-print!)
  (om/add-root! reconciler App (gdom/getElement "app")))
