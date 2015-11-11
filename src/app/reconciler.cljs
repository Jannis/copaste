(ns app.reconciler
  (:require [ajax.core :refer [POST]]
            [om.next :as om]
            [app.state :refer [initial-state]]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

(defn send-completed-handler [merge-fn results]
  (println "<<" results)
  (merge-fn results))
  ; (println "copaste" (om/app-state reconciler)))

(defn send-error-handler [merge-fn error]
  (println "<<" "error" error))

(defn send [reqs merge-fn]
  (let [query (:remote reqs)]
    (println ">>" query)
    (POST "http://localhost:3001/query"
          {:params query
           :handler #(send-completed-handler merge-fn %)
           :error-handler #(send-error-handler merge-fn %)})))

(def parser
  (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler {:state initial-state
                  :parser parser
                  :send send}))
