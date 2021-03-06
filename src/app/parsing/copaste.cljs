(ns app.parsing.copaste
  (:require [om.next :as om]
            [app.reconciler :refer [mutate read]]))

(defmethod read :copaste/ref
  [{:keys [state query]} _ {:keys [ident]}]
  (let [st @state]
    {:value (get-in st ident)
     :remote true}))

(defmethod read :copaste/refs
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote true}))

(defmethod read :copaste/topics
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote true}))

(defmethod read :copaste/snippets
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (let [st @state] (om/db->tree query (get st key) st))
     :remote true}))

(defmethod mutate 'copaste/create-snippet
  [{:keys [state]} _ _]
  {:value {:keys [:copaste/snippets]}
   :action
   (fn []
     (let [id (om/tempid)
           ident [:snippet/by-uuid id]]
       (swap! state (fn [st]
                      (-> st
                          (assoc-in ident
                                    {:uuid id
                                     :property/title ""
                                     :property/code ""
                                     :app/editing true})
                          (update :copaste/snippets conj ident))))))})

(defmethod mutate 'copaste/delete-snippet
  [{:keys [state]} _ {:keys [ref ident]}]
  {:value {:keys [ref ident]}
   :remote true})

(defmethod mutate 'copaste/save-snippet
 [{:keys [state]} _ {:keys [ref snippet]}]
 (let [ident [:snippet/by-uuid (:uuid snippet)]]
   {:value {:keys [ref ident]}
    :action #(swap! state update-in ident
                    (fn [props] (-> props
                                  (assoc :app/editing false)
                                  (assoc :app/expanded false))))
    :remote true}))
