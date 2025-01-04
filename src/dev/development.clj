(ns development
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.example.components.database :refer [datomic-connections]]
    [com.example.components.ring-middleware]
    [com.example.components.server]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.example.model.team :as team]
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
  [{:db/id             "2024"
    :league/id         (new-uuid)
    :league/year       2024
    :league/champion   (rand-nth team-initial-arr)
    :league/completed? false}])

(defn create-match [local visitor]
  (let [local-name (:team/title local)
        visitor-name (:team/title visitor)]
    (str local-name " vs " visitor-name)))

#_(doseq [{:keys [team/id team/title]} team-initial-arr]
    (println title))

#_(defn random-date-in-year [year]
    (let [start-of-year (LocalDate/of year 1 1)
          days-in-year (.lengthOfYear start-of-year)
          random-day (rand-int days-in-year)]
      (.toInstant (.plusDays start-of-year random-day))))

(defn generate-match
  [local-team visitor-team match-day]
  {:match/id            (new-uuid)
   :match/league        "2024"
   :match/match-day     match-day
   :match/local         local-team
   :match/visitor       visitor-team
   :match/local-goals   (rand-int 5)
   :match/visitor-goals (rand-int 5)})

#_((def nacional (rand-nth team-initial-arr))
   (def dim (rand-nth team-initial-arr))
   (str team-initial-arr)

   (def arr-match (vector (generate-match nacional dim (rand-int 19))))
   arr-match

   (create-match nacional dim)

   (select-keys nacional [:team/id])

   (let [{:keys [team/title team/id]} nacional]
     (str "Title: " title " ID:" id)))

#_(def matches
    (mapv
      (fn [idx [local visitor]]
        (generate-match local visitor (inc idx)))
      (partition 2 team-initial-arr)))

(defn seed! []
  (dt/set-timezone! "America/Los_Angeles")
  (let [connection (:main datomic-connections)]
    (when connection
      (log/info "Loading data...")
      (try
        (d/transact connection {:tx-data (concat city-initial-arr
                                                 team-initial-arr
                                                 league-initial-arr
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
