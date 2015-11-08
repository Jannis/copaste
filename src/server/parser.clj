(ns server.parser
  (:require [om.next.server :as om]))

(defmulti readf (fn [env key params] key))

(defmethod readf :app/counter
  [{:keys [state]} key _]
  {:value (get @state key)})

(defmulti mutatef (fn [env key params] key))

(defmethod mutatef 'app/increment-counter
  [{:keys [state]} _ _]
  {:value [:app/counter]
   :action #(swap! state update :app/counter inc)})

(def parser (om/parser {:read readf :mutate mutatef}))
