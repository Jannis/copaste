(ns server.transaction
  (:refer-clojure :exclude [update])
  (:require [clj-consonant.actions :as actions]
            [clj-consonant.store :as s]))

(defrecord Transaction [actions])

(defn begin [& options]
  (->> (apply actions/begin options)
       (conj [])
       (->Transaction)))

(defn commit [ta & options]
  (->> (apply actions/commit options)
       (conj (:actions ta))
       (->Transaction)))

(defn create [ta & options]
  (->> (apply actions/create options)
       (conj (:actions ta))
       (->Transaction)))

(defn update [ta & options]
  (->> (apply actions/update options)
       (conj (:actions ta))
       (->Transaction)))

(defn transact! [store ta]
  (s/transact! store (:actions ta)))
