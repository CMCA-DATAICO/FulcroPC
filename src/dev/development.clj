(ns development
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [clojure.math.combinatorics :as combo]
    [com.example.components.database :refer [datomic-connections]]
    [com.example.components.ring-middleware]
    [com.example.components.server]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.example.model.team :as team]
    [com.example.model.match :as match]
    [datomic.client.api :as d]
    [mount.core :as mount]
    [taoensso.timbre :as log])
  (:import [java.time LocalDate]))

;; Prevent tools-ns from finding source in other places, such as resources
(set-refresh-dirs "src/main" "src/dev")

(comment
  (let [db (d/db (:main datomic-connections))]
    (d/pull db '[*] [:team/id (new-uuid 100)])))

(def city-initial-arr
  "This vector contains a pre-seed for cities"
  [{:db/id "Medellin" :city/id (new-uuid) :city/title "Medellin"}
   {:db/id "Bogota" :city/id (new-uuid) :city/title "Bogota"}
   {:db/id "Cali" :city/id (new-uuid) :city/title "Cali"}
   {:db/id "Barranquilla" :city/id (new-uuid) :city/title "Barranquilla"}
   {:db/id "Manizales" :city/id (new-uuid) :city/title "Manizales"}
   {:db/id "Ibague" :city/id (new-uuid) :city/title "Ibague"}
   {:db/id "Bucaramanga" :city/id (new-uuid) :city/title "Bucaramanga"}
   {:db/id "Pereira" :city/id (new-uuid) :city/title "Pereira"}
   {:db/id "Tunja" :city/id (new-uuid) :city/title "Tunja"}
   {:db/id "Santa Marta" :city/id (new-uuid) :city/title "Santa Marta"}
   {:db/id "Pasto" :city/id (new-uuid) :city/title "Pasto"}
   {:db/id "Neiva" :city/id (new-uuid) :city/title "Neiva"}
   {:db/id "Envigado" :city/id (new-uuid) :city/title "Envigado"}])

(def team-initial-arr
  "This vector contains a pre-seed for teams"
  [{:team/id (new-uuid) :team/title "Atletico Nacional" :team/city "Medellin" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Independiente Medellin" :team/city "Medellin" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Millonarios" :team/city "Bogota" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Santa Fe" :team/city "Bogota" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "America de Cali" :team/city "Cali" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Deportivo Cali" :team/city "Cali" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Junior" :team/city "Barranquilla" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Once Caldas" :team/city "Manizales" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Deportes Tolima" :team/city "Ibague" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Atletico Bucaramanga" :team/city "Bucaramanga" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Deportivo Pereira" :team/city "Pereira" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Patriotas" :team/city "Tunja" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Union Magdalena" :team/city "Santa Marta" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Deportivo Pasto" :team/city "Pasto" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Envigado" :team/city "Envigado" :team/score (rand-int 100)}
   {:team/id (new-uuid) :team/title "Huila" :team/city "Neiva" :team/score (rand-int 100)}])

(def league-initial-arr
  "Pre-seed league - Now it's working"
  [{:db/id             "2024"
    :league/id         (new-uuid)
    :league/year       2024
    :league/teams      team-initial-arr
    :league/champion   (rand-nth team-initial-arr)
    :league/completed? false}])

(defn new-match
  "Pre-seed match - It is a test"
  [local-team visitor-team match-day]
  {:match/id            (new-uuid)
   :match/date          #inst "2024-01-01T11:30"
   :match/league        "2024"
   :match/match-day     match-day
   :match/local         local-team
   :match/visitor       visitor-team
   :match/local-goals   (rand-int 5)
   :match/visitor-goals (rand-int 5)})


"Old algorithm for match making using permuted-combinations"
(defn- matches-for-team*
  "This is the helper function for matches-for-team and works in the iterations for every team."
  [local visitors match-day matches]
  (if (seq visitors)
    (let [current-match (new-match local (first visitors) match-day)]
      (matches-for-team* local (rest visitors) (inc match-day) (conj matches current-match)))
    matches))

(defn matches-for-team
  "Returns a sequence of matches for a given team who will play against the provided visiting-teams."
  ([local-team visiting-teams]
   (matches-for-team* local-team visiting-teams 1 [])))

(defn all-matches
  "Returns a sequence of home away in permuted combination matches among the given teams."
  [teams]
  (for [[home away] (combo/combinations teams 2)]
    (new-match home away (+ 1 (rand-int 15)))))

(def match-initial-arr (all-matches team-initial-arr))

(defn seed! []
  (dt/set-timezone! "America/Los_Angeles")
  (let [connection (:main datomic-connections)]
    (when connection
      (log/info "Loading data...")
      (try
        (d/transact connection {:tx-data (concat city-initial-arr
                                           team-initial-arr
                                           league-initial-arr
                                           match-initial-arr
                                           )})
        (log/info "Completed.")
        (catch Exception e
          (log/error e "Failed to load data"))))))

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  (seed!)
  :ok)

(defn stop
  "Stop the server."
  []
  (mount/stop))

(defn fast-restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (start))

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))


