(ns app.components.snippet-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [app.components.snippet :refer [snippet]]))

(defn sort-snippets [snippets]
  (sort-by :uuid
           (fn [id1 id2] (instance? om.tempid/TempId id1))
           snippets))

(defui SnippetList
  Object
  (render [this]
    (let [{:keys [snippets]} (om/props this)
          {:keys [toggle-fn create-fn edit-fn delete-fn update-fn save-fn]}
          (om/get-computed this)]
      (dom/div #js {:className "snippet-list"}
        (dom/h2 #js {:className "snippet-list-title"}
          (dom/span #js {:className "snippet-list-title-text"} "Snippets")
          (dom/span #js {:className "snippet-list-title-buttons"}
            (dom/button #js {:className "snippet-list-title-button"
                             :onClick #(when create-fn (create-fn))}
              "+ Create")))
        (for [sn (sort-snippets snippets)]
          (snippet (om/computed sn {:toggle-fn toggle-fn
                                    :edit-fn edit-fn
                                    :delete-fn delete-fn
                                    :update-fn update-fn
                                    :save-fn save-fn})))))))

(def snippet-list (om/factory SnippetList))
