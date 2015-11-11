(ns app.parsing.copaste
  (:require [om.next :as om]
            [app.reconciler :refer [mutate read]]))

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
  {:value (let [st @state] (om/db->tree query (get st key) st))
   :remote true})

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
                          (update :copaste/snippets conj ident)))))
       (println "new state" @state))})

(defmethod mutate 'copaste/save-snippet
 [{:keys [state]} _ {:keys [ref snippet]}]
 {:value {:keys [:copaste/refs :copaste/snippets]}
  :remote true})
