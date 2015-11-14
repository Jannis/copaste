(ns app.om-ext
  (:import [goog.net XhrIo])
  (:require-macros app.om-ext)
  (:require [clojure.walk :as walk]
            [cognitect.transit :as transit]
            [om.next :as om]))

(defn txbind [c tx]
  {:pre [(om/component? c) (vector? tx)]}
  (letfn [(matches-read [q k]
            (or (= q k)
                (and (map? q) (contains? q k))
                (and (seq? q) (= (first q) k))
                (and (seq? q) (map? (first q)) (contains? (first q) k))))
          (resolve-read [cq k]
            (if (keyword? k)
              (if (map? cq)
                (if (matches-read cq k) cq k)
                (let [matches (filter (fn [q] (matches-read q k)) cq)]
                  (case (count matches)
                    0 k
                    1 (first matches)
                      (throw (str "Ambiguous transaction read " k " in "
                                  "component " c ". Matches: " matches)))))
              k))]
    (let [cq (om/get-query c)]
      (into []
        (map #(resolve-read cq %))
        tx))))

(defn merge-result-tree [a b]
  (letfn [(merge-tree [a b]
            (if (and (map? a) (map? b))
              (merge-with #(merge-tree %1 %2) a b)
              b))]
    (merge-tree a b)))

(defn- transit-post [url data cb]
  (.send XhrIo url
    (fn [e]
      (this-as this
        (cb (transit/read (om.transit/reader) (.getResponseText this)))))
    "POST"
    (transit/write (om.transit/writer) data)
    #js {"Content-Type" "application/transit+json"}))

(defn send-to-remotes [remotes sends merge-fn]
  (println ">> send" remotes "sends" sends)
  (doseq [[remote query] sends]
    (transit-post (get-in remotes [remote :url])
                  query
                  (fn [data]
                    (let [remote-cb (get-in remotes [remote :callback])]
                      (when remote-cb
                        (remote-cb data merge-fn)))))))
