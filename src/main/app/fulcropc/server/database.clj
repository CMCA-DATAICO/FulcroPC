(ns app.fulcropc.server.database
  (:require
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [mount.core :refer [defstate]]
    [app.fulcropc.model.attributes :refer [all-attributes]]
    [app.fulcropc.server.config :refer [config]]))

(defstate ^{:on-reload :noop} datomic-connections
  :start
  (datomic/start-databases all-attributes config))
