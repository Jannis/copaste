(ns app.parsing.counter
  (:require [app.reconciler :refer [mutate read]]))

(defmethod read :app/counter
  [{:keys [state]} key _]
  (println "read :app/counter" "local state:" @state)
  {:value (or (get @state key) 0)
   :remote true})

(defmethod mutate 'app/increment-counter
  [{:keys [state]} _ _]
  {:value [:app/counter]
   :remote true})
   ;:action #(swap! state update :app/counter inc)})
