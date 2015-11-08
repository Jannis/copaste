(ns server.handler
  (:require [compojure.core :refer [defroutes OPTIONS POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response header]]
            [ring.middleware.format-params :refer [wrap-transit-json-params]]
            [ring.middleware.format-response :refer [wrap-transit-json-response]]
            [server.middleware :refer [wrap-access-headers]]
            [server.parser :refer [parser]]))

;;;; App server

(defroutes app-routes
  (route/resources "/" {:root "."})
  (route/not-found "Not found"))

(def app-server
  (-> app-routes))

;;;; Backend server

(defonce server-state (atom {:app/counter 0}))

(defn handle-query [params]
  (response (parser {:state server-state} params)))

(defroutes backend-routes
  (OPTIONS "/query" {params :body-params} (handle-query params))
  (POST    "/query" {params :body-params} (handle-query params))
  (route/not-found "Not found"))

(def backend-server
  (-> backend-routes
      (wrap-access-headers)
      (wrap-transit-json-params)
      (wrap-transit-json-response :options {:verbose true})))
