(ns app.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [app.parsing.app]
            [app.parsing.copaste]
            [app.reconciler :refer [reconciler]]
            [app.components.snippet :refer [Snippet]]
            [app.components.snippet-list :refer [snippet-list]]
            [app.components.refs-menu :refer [RefMenuItem refs-menu]]
            [app.om-ext :refer [txbind]]))

(defui App
  static om/IQueryParams
  (params [this]
    {:ref [:ref/by-name "HEAD"]})
  static om/IQuery
  (query [this]
    `[:app/ref
      {:copaste/refs ~(om/get-query RefMenuItem)}
      ({:copaste/snippets ~(om/get-query Snippet)} {:ref ?ref})])
  Object
  (activate-ref [this ident]
    (om/set-params! this {:ref ident})
    (om/transact! this (txbind this `[(app/set-ref {:ident ~ident})
                                      :copaste/snippets])))

  (reset-ui [this]
    (om/transact! this `[(app/set-snippet nil)
                         (app/edit-snippet {:edit false})]))

  (set-snippet [this ident]
    (om/transact! this `[(app/set-snippet {:ident ~ident})]))

  (toggle-expanded [this ident]
    (om/transact! this (txbind this `[(app/toggle-expanded {:ident ~ident})])))

  (create-snippet [this]
    (om/transact! this (txbind this `[(copaste/create-snippet)
                                      (app/edit-snippet {:edit true})])))

  (update-snippet [this ident props]
    (om/transact! this `[(app/update-snippet {:ident ~ident :props ~props})]))

  (save-snippet [this ident]
    (println "save-snippet" ident)
    (let [ref (:app/ref (om/props this))
          snippet (->> (:copaste/snippets (om/props this))
                       (filter #(= (second ident) (:uuid %)))
                       (first))]
      (om/transact!
        this (txbind this `[(copaste/save-snippet {:ref ~ref :snippet ~snippet})
                            :copaste/refs]))))

  (render [this]
    (println "Render App")
    (println "  query" (om/get-query this))
    (println "  txbind" (txbind this `[(app/do-something)
                                       :app/ref
                                       :copaste/refs
                                       :copaste/snippets]))
    (dom/div #js {:className "app"}
      (dom/header #js {:className "header"}
        (dom/h1 #js {:className "header-title"
                     :onClick #(.reset-ui this nil)}
          (dom/a nil "copaste")
          (dom/span #js {:className "header-subtitle"}
            "Store and recover code snippets"))
        (dom/nav #js {:className "header-nav"}
          (let [refs (:copaste/refs (om/props this))]
            (refs-menu
              (om/computed {:refs refs}
                           {:create-fn #(.create-ref this)
                            :activate-fn #(.activate-ref this %)})))))
      (dom/main #js {:className "main"}
        (if-let [ref (:app/ref (om/props this))]
          (dom/p #js {:className "ref-header"}
            (:name ref) (some->> ref :head :sha1 (str " @ "))))
        (let [snippets (:copaste/snippets (om/props this))]
          (snippet-list
            (om/computed {:snippets snippets}
                         {:toggle-fn #(.toggle-expanded this %)
                          :create-fn #(.create-snippet this %)
                          :update-fn #(.update-snippet this %1 %2)
                          :save-fn #(.save-snippet this %1)})))))))

(defn run []
  (enable-console-print!)
  (om/add-root! reconciler App (gdom/getElement "app")))
