(ns app.reconciler
  (:require [ajax.core :refer [POST]]
            [om.next :as om]
            [om.transit :as om-transit]
            [app.om-ext :refer [merge-result-tree]]
            [app.state :refer [initial-state]]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

(defn send-completed-handler [merge-fn results]
  (println "<<" results)
  (merge-fn results))

(defn send-error-handler [merge-fn error]
  (println "<<" "error" error))

(defn send [reqs merge-fn]
  (let [query (:remote reqs)]
    (println ">>" query)
    (POST "http://localhost:3001/query"
          {:params query
           :reader (om-transit/reader)
           :writer (om-transit/writer)
           :response-format :transit
           :handler #(send-completed-handler merge-fn %)
           :error-handler #(send-error-handler merge-fn %)})))

(def parser
  (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler {:state initial-state
                  :parser parser
                  :send send
                  :merge-tree merge-result-tree
                  :id-key :uuid}))
