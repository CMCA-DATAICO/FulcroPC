(ns app.fulcropc.resolvers.city
  "Custom resolvers and mutations for cities.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    #?(:clj [datomic.client.api :as d])
    [taoensso.timbre :as log]))

#?(:clj
   (defn get-all-cities
     [env _]
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [cities (d/q '[:find ?id ?title
                           :where
                           [?e :city/id ?id]
                           [?e :city/title ?title]]
                         db)]
         (mapv (fn [[id title]] {:city/id id :city/title title}) cities))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver all-cities-resolver [env params]
     {::pc/output [:city/all-cities]}
     {:city/all-cities (get-all-cities env params)}))

(def resolvers [all-cities-resolver])