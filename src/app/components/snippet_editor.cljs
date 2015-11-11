(ns app.components.snippet-editor
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui SnippetEditor
  static om/Ident
  (ident [this props]
    [:snippet/by-uuid (:uuid props)])
  static om/IQuery
  (query [this]
    [:uuid :property/title :property/code])
  Object
  (render [this]
    (println "Render SnippetEditor" (:uuid (om/props this)))
    (let [{:keys [property/title property/code]} (om/props this)
          {:keys [save-fn update-fn]} (om/get-computed this)]
      (dom/div #js {:className "snippet-editor"}
        (dom/h2 #js {:className "snippet-editor-title"} "Edit Snippet")
        (dom/form #js {:className "snippet-editor-form form"
                       :onSubmit #(.preventDefault %)}
          (dom/p #js {:className "form-row"}
            (dom/label #js {:className "form-label"} "Title")
            (dom/input #js {:className "form-input"
                            :type "text"
                            :placeholder "..."
                            :value title
                            :onChange
                            #(when update-fn
                               (let [title (.. % -target -value)]
                                 (update-fn {:property/title title})))}))
          (dom/p #js {:className "form-row"}
            (dom/label #js {:className "form-label"} "Code")
            (dom/textarea #js {:className "form-input"
                               :value code
                               :onChange
                               #(when update-fn
                                  (let [code (.. % -target -value)]
                                    (update-fn {:property/code code})))}))
          (dom/p #js {:className "form-row"}
            (dom/span #js {:className "form-label"} " ")
            (dom/span #js {:className "form-buttons"}
              (dom/button #js {:className "form-button"
                               :onClick #(when save-fn (save-fn))}
                "Save"))))))))

(def snippet-editor (om/factory SnippetEditor {:keyfn :uuid}))
