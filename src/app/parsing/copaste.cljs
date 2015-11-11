(ns app.parsing.copaste
  (:require [om.next :as om]
            [app.reconciler :refer [mutate read]]))

(defmethod read :copaste/refs
  [{:keys [state selector]} key _]
  (let [st @state]
    {:value (om/db->tree selector (get st key) st)
     :remote true}))

(defmethod read :copaste/topics
  [{:keys [state selector]} key _]
  (let [st @state]
    {:value (om/db->tree selector (get st key) st)
     :remote true}))

(defmethod read :copaste/snippets
  [{:keys [state selector]} key _]
  {:value (let [st @state] (om/db->tree selector (get st key) st))
   :remote true})

(defmethod mutate 'copaste/save-snippet
 [{:keys [state]} _ {:keys [ref snippet]}]
 {:value [:copaste/refs :copaste/snippets]
  :remote true})
