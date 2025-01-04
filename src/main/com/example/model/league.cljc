(ns com.example.model.league
  "Functions, resolvers, and mutations supporting `account`.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [taoensso.timbre :as log]
    #?(:clj [datomic.client.api :as d])
    ))

(defn new-league
  [id year teams matches ladder champion completed? & {:as addl}]
  (merge
    {:db/id             year
     :league/id         id
     :league/year       year
     :league/teams      teams
     :league/matches    matches
     :league/ladder     ladder
     :league/champion   champion
     :league/completed? completed?}
    addl))

#?(:clj
   (defn get-all-leagues
     [env query-params]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (d/q '[:find (pull ?e [:league/id {:league/champion [*]} :league/completed?])
                       :where
                       [?e :league/id _]] db)]
         (map first ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver all-leagues-resolver [env params]
     {::pc/output [{:league/all-leagues [:league/id]}]}
     {:league/all-leagues (get-all-leagues env params)})
   )

(def resolvers [all-leagues-resolver])

