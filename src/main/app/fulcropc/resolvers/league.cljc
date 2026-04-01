(ns app.fulcropc.resolvers.league
  "Custom resolvers and mutations for leagues.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [taoensso.timbre :as log]
    #?(:clj [datomic.client.api :as d])))

#?(:clj
   (defn get-all-leagues
     [env query-params]
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (d/q '[:find (pull ?e [:league/id :league/year {:league/matches [*]}
                                        {:league/teams [*]} {:league/ladder [*]}
                                        :league/completed? {:league/champion [*]}])
                        :where
                        [?e :league/id _]] db)]
         (mapv first ids))
       (do
         (log/error "No database atom for production schema!")
         []))))

#?(:clj
   (defresolver all-leagues-resolver
     [env params]
     {::pc/output [{:league/all-leagues [:league/id]}]}
     {:league/all-leagues (get-all-leagues env params)}))

#?(:clj
   (def resolvers [all-leagues-resolver]))
