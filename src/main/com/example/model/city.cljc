(ns com.example.model.city
  "Functions, resolvers, and mutations supporting `file`.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [datomic.client.api :as d]
    [taoensso.timbre :as log]))

(defn new-city
  "Create a new file object. The sha will be the Datomic tempid. The tempid is the sha."
  [id title & {:as addl}]
  (merge
    {:db/id      title
     :city/id    id
     :city/title title
     }
    addl))

#?(:clj
   (defn get-all-cities
     [env _]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [cities (d/q '[:find ?id ?title
                           :where
                           [?e :city/id ?id]
                           [?e :city/title ?title]]
                         db)]
         ;; Mapping cities to include both :city/id and :city/title
         (mapv (fn [[id title]] {:city/id id :city/title title}) cities))
       (log/error "No database atom for production schema!"))))


#?(:clj
   (defresolver all-cities-resolver [env params]
     {::pc/output [:city/all-cities]}
     {:city/all-cities (get-all-cities env params)}))

(def resolvers [all-cities-resolver])