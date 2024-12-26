(ns com.example.model.match
  "Functions, resolvers, and mutations supporting `account`.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.example.model-rad.team :as team]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [datomic.client.api :as d]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.components :as comp]
    ))

(defn new-match
  [id league date match-day local visitor local-goals visitor-goals & {:as addl}]
  (merge
    {:db/id               league
     :match/id            id
     :match/league        league
     :match/date          date
     :match/match-day     match-day
     :match/local         local
     :match/visitor       visitor
     :match/local-goals   local-goals
     :match/visitor-goals visitor-goals
     }
    addl))

#?(:clj
   (defn get-all-matches
     [env query-params]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
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
