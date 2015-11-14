(ns app.parsing.app
  (:require [om.next :as om]
            [app.reconciler :refer [mutate read]]))

(defmethod read :app/ref
  [{:keys [state]} key _]
  (let [st @state]
    {:value (some->> key (get st) (get-in st))}))

(defmethod mutate 'app/set-ref
  [{:keys [state]} _ {:keys [ident]}]
  {:value {:keys [:app/ref]}
   :action #(swap! state assoc :app/ref ident)})

(defmethod read :app/snippet
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod mutate 'app/set-snippet
  [{:keys [state]} _ {:keys [ident]}]
  {:value {:keys [:app/snippet]}
   :action
   (fn []
     (swap! state (fn [st]
                    (let [snippet (if ident (get-in st ident) nil)]
                      (assoc st :app/snippet snippet)))))})

(defmethod mutate 'app/toggle-expanded
  [{:keys [state]} _ {:keys [ident]}]
  {:value {:keys [ident]}
   :action #(swap! state update-in (conj ident :app/expanded) not)})

(defmethod mutate 'app/update-snippet
  [{:keys [state]} _ {:keys [ident props]}]
  {:value {:keys [ident]}
   :action #(swap! state update-in ident merge props)})

(defmethod read :app/edit-snippet
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod mutate 'app/edit-snippet
  [{:keys [state]} _ {:keys [edit]}]
  {:value {:keys [:app/edit-snippet]}
   :action #(swap! state assoc :app/edit-snippet (or edit false))})
