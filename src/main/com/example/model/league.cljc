(ns com.example.model.league
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
    [com.wsscode.pathom.connect :as pc :refer [defmutation]]
    [com.fulcrologic.fulcro.components :as comp]
    ))

;; [your-app.db :as db]


(defn new-league
  [id year teams matches & {:as addl}]
  (merge
    {:db/id        year
     :league/id      id
     :league/year year
     :league/teams teams
     :league/matches matches}
    addl))

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




#_#?(:clj
     (defresolver all-teams-attributes-resolver [env params]
                  {::pc/output [{:team/all-teams [:team/id]}]}
                  {:team/all-teams (get-all-teams env params)}))

#_#?(:clj
     (defmutation set-team-active [{:keys [team/id]}]
                  (action [{:keys [db-conn]}]
                          (let [current-value (-> (d/pull @db-conn [:team/enable?] [:team/id id])
                                                  :team/enable?)
                                new-value (not current-value)]
                            (d/transact db-conn [{:db/id        [:team/id id]
                                                  :team/enable? new-value}])
                            {:status       :success
                             :team/id      id
                             :team/enable? new-value}))))

(def resolvers [all-teams-resolver])
