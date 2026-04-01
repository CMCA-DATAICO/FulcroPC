(ns app.fulcropc.resolvers.match
  "Custom resolvers and mutations for matches.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    #?(:clj [datomic.client.api :as d])
    [taoensso.timbre :as log]))

#?(:clj
   (defn get-all-matches
     [env query-params]
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (map first
                      (d/q [:find '?uuid
                            :where
                            ['?e :match/id '?uuid]] db))]
         (mapv (fn [id] {:match/id id}) ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver all-matches-resolver [env params]
     {::pc/output [{:match/all-matches [:match/id]}]}
     {:match/all-matches (get-all-matches env params)}))

(def resolvers [all-matches-resolver])





