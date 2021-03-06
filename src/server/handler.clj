(ns server.handler
  (:import [java.io ByteArrayOutputStream])
  (:require [cognitect.transit :as transit]
            [compojure.core :refer [defroutes OPTIONS POST]]
            [compojure.route :as route]
            [om.next.server :as om]
            [ring.util.response :refer [response header]]
            [ring.middleware.format-params :refer [wrap-transit-json-params]]
            [ring.middleware.format-response :refer [wrap-transit-json-response]]
            [reloaded.repl :refer [system]]
            [server.middleware :refer [wrap-access-headers]]
            [server.parser :refer [parser]]
            [server.parsing.copaste]))

;;;; App server

(defroutes app-routes
  (route/resources "/" {:root "."})
  (route/not-found "Not found"))

(def app-server
  (-> app-routes))

;;;; Backend server

(defn handle-query [params]
  (println "<<" params)
  (let [ret (parser {:consonant (:consonant system)} params)]
    (println ">>" ret)
    (response ret)))

(defn handle-echo [params]
  (println "<<" params (type params))
  (println ">>" params)
  (response params))

(defroutes backend-routes
  (OPTIONS "/query" {params :body-params} (handle-query params))
  (POST    "/query" {params :body-params} (handle-query params))
  (OPTIONS "/echo"  {params :body-params} (handle-echo params))
  (POST    "/echo"  {params :body-params} (handle-echo params))
  (route/not-found "Not found"))

(defn make-om-transit-decoder []
  (fn [in]
    (transit/read (om/reader in))))

(defn make-om-transit-encoder []
  (fn [in]
    (let [out (ByteArrayOutputStream.)]
      (transit/write (om/writer out) in)
      (.toByteArray out))))

(def backend-server
  (-> backend-routes
      (wrap-access-headers)
      (wrap-transit-json-params :decoder (make-om-transit-decoder)
                                :options {:verbose true})
      (wrap-transit-json-response :encoder (make-om-transit-encoder)
                                  :options {:verbose true})))
