(ns app.components.snippet-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui SnippetListItem
  static om/Ident
  (ident [this props]
    [:snippet/by-uuid (:uuid props)])
  static om/IQuery
  (query [this]
    [:uuid :property/title])
  Object
  (render [this]
    (println "Render SnippetListItem" (:uuid (om/props this)))
    (let [{:keys [uuid property/title]} (om/props this)
          {:keys [activate-fn]} (om/get-computed this)]
      (dom/div #js {:className "snippet-list-item"
                    :onClick #(when activate-fn
                                (activate-fn (om/get-ident this)))}
        (dom/span #js {:className "snippet-list-item-id"} (subs uuid 0 8))
        (dom/span #js {:className "snippet-list-item-title"} title)))))

(def snippet-list-item (om/factory SnippetListItem {:keyfn :uuid}))

(defui SnippetList
  Object
  (render [this]
    (let [{:keys [snippets]} (om/props this)
          {:keys [activate-fn create-fn]} (om/get-computed this)]
      (dom/div #js {:className "snippet-list"}
        (dom/h2 #js {:className "snippet-list-title"}
          (dom/span #js {:className "snippet-list-title-text"} "Snippets")
          (dom/span #js {:className "snippet-list-title-buttons"}
            (dom/button #js {:className "snippet-list-title-button"
                             :onClick #(when create-fn (create-fn))}
              "+ Create")))
        (for [snippet snippets]
          (snippet-list-item
            (om/computed snippet {:activate-fn activate-fn})))))))

(def snippet-list (om/factory SnippetList))
