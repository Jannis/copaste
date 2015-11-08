(ns server.systems
  (:require [system.core :refer [defsystem]]
            (system.components
              [http-kit :refer [new-web-server]])
            [server.handler :refer [app-server backend-server]]))

(defsystem development-system
  [:app-server (new-web-server 3000 app-server)
   :backend-server (new-web-server 3001 backend-server)])

(defsystem production-system
  [:app-server (new-web-server 3000 app-server)
   :backend-server (new-web-server 3001 backend-server)])
