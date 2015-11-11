(ns app.parsing.app
  (:require [om.next :as om]
            [app.reconciler :refer [mutate read]]))

(defmethod read :app/ref
  [{:keys [state selector]} key _]
  (let [st @state]
    {:value (some->> key (get st) (get-in st))}))

(defmethod mutate 'app/set-ref
  [{:keys [state]} _ {:keys [ident]}]
  {:value [:app/ref]
   :action #(swap! state assoc :app/ref ident)})

(defmethod read :app/snippet
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod mutate 'app/set-snippet
  [{:keys [state]} _ {:keys [ident]}]
  {:value [:app/snippet]
   :action
   (fn []
     (swap! state (fn [st]
                    (let [snippet (if ident (get-in st ident) nil)]
                      (assoc st :app/snippet snippet)))))})

(defmethod mutate 'app/create-snippet
  [{:keys [state]} _ _]
  {:value [:app/snippet :app/edit-snippet]
   :action
   (fn []
     (swap! state (fn [st] (-> st
                               (assoc :app/snippet {:uuid nil})
                               (assoc :app/edit-snippet true)))))})

(defmethod mutate 'app/update-snippet
  [{:keys [state]} _ {:keys [props]}]
  {:value [:app/snippet]
   :action #(swap! state update :app/snippet merge props)})

(defmethod read :app/edit-snippet
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod mutate 'app/edit-snippet
  [{:keys [state]} _ {:keys [edit]}]
  {:value [:app/edit-snippet]
   :action #(swap! state assoc :app/edit-snippet (or edit false))})
