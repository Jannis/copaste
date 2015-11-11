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
            [app.queries :refer [txbind]]))

(enable-console-print!)

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
    (om/transact! this (txbind this `[(app/toggle-expanded {:ident ~ident})
                                      :copaste/snippets])))

  (create-snippet [this]
    (om/transact! this (txbind this `[(app/create-snippet)
                                      (app/edit-snippet {:edit true})])))

  (update-snippet [this props]
    (om/transact! this `[(app/update-snippet {:props ~props})]))

  (save-snippet [this]
    (let [ref (:app/ref (om/props this))
          snippet (:app/snippet (om/props this))]
      (om/transact!
        this (txbind this `[(copaste/save-snippet {:ref ~ref :snippet ~snippet})
                            (app/set-snippet {:ident nil})
                            (app/edit-snippet {:edit false})
                            :copaste/refs
                            :copaste/snippets]))))

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
                          :create-fn #(.create-snippet this %)})))))))
        ; (when-let [sn (:app/snippet (om/props this))]
        ;   (println "SN" sn)
        ;   (if-let [edit (:app/edit-snippet (om/props this))]
        ;     (snippet-editor
        ;       (om/computed sn
        ;                    {:save-fn #(.save-snippet this)
        ;                     :update-fn #(.update-snippet this %)}))
        ;     (snippet sn)))))))


(defn run []
  (om/add-root! reconciler App (gdom/getElement "app")))
