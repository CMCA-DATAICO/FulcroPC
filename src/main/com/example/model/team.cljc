(ns com.example.model.team
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

(defn new-team
  [id title city score attack mid defence enable? palmares & {:as addl}]
  (merge
    {:db/id         title
     :team/id       id
     :team/title    title
     :team/city     city
     :team/score    score
     :team/attack   attack
     :team/mid      mid
     :team/defence  defence
     :team/enable?  enable?
     :team/palmares palmares}
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

#?(:clj
   (defn get-attributes-teams
     [env query-params]
     (log/debug (some-> (get-in env [do/databases :production]) deref))
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (d/q '[:find (pull ?e [:team/id :team/title :team/score
                                        {:team/city [*]} :team/enable? :team/palmares])
                        :where [?e :team/id _]] db)]
         (mapv first ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver attributes-teams-resolver [env params]
                {::pc/output [{:team/attributes-teams [:team/id]}]}
                {:team/attributes-teams (get-attributes-teams env params)}))

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

(def resolvers [all-teams-resolver attributes-teams-resolver])