(ns app.fulcropc.resolvers.team
  "Custom resolvers and mutations for teams.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    #?(:clj [datomic.client.api :as d])
    [taoensso.timbre :as log]))

#?(:clj
   (defn get-all-teams
     [env query-params]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (map first
                   (d/q [:find '?uuid
                         :where
                         ['?e :team/id '?uuid]] db))]
         (mapv (fn [id] {:team/id id}) ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver all-teams-resolver [env params]
     {::pc/output [{:team/all-teams [:team/id]}]}
     {:team/all-teams (get-all-teams env params)}))

#?(:clj
   (defn get-attributes-teams
     [env query-params]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (d/q '[:find (pull ?e [:team/id :team/title :team/score :team/badge
                                        {:team/city [*]} :team/enable? :team/palmares])
                        :where [?e :team/id _]] db)]
         (mapv first ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver attributes-teams-resolver [env params]
     {::pc/output [{:team/attributes-teams [:team/id]}]}
     {:team/attributes-teams (get-attributes-teams env params)}))

(def resolvers [all-teams-resolver attributes-teams-resolver])