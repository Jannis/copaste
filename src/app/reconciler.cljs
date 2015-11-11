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

(defmulti merge-subtree (fn [a [k v]] k))

(defmethod merge-subtree :snippet/by-uuid
  [res [k v]]
  (merge-with #(merge-with merge %1 %2) res {k v}))

(defmethod merge-subtree :default
  [res [k v]]
  (om/default-merge-tree res {k v}))

(defn merge-tree [a b]
  (reduce merge-subtree a b))

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
                  :send send
                  :merge-tree merge-tree}))
