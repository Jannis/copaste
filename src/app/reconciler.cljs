(ns app.reconciler
  (:require [om.next :as om]
            [app.om-ext :refer [merge-result-tree send-to-remotes]]
            [app.state :refer [initial-state]]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

(defn merge-remote [results merge-fn]
  (println "<<" results)
  (merge-fn results))

(def parser
  (om/parser {:read read :mutate mutate}))

(def remotes {:remote {:url "http://localhost:3001/query"
                       :callback merge-remote}})

(def reconciler
  (om/reconciler {:state initial-state
                  :parser parser
                  :send #(send-to-remotes remotes %1 %2)
                  :remotes (keys remotes)
                  :merge-tree merge-result-tree
                  :id-key :uuid}))
