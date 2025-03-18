(ns com.example.model-rad.league
  "RAD definition of an `account`. Attributes only. These will be used all over the app, so try to limit
   requires to model code and library code."
  (:require
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.report-options :as ro]))

(defattr id :league/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr year :league/year :long
  {ao/identities  #{:league/id}
   ao/required?   true
   ao/cardinality :one
   ao/schema      :production})

(defattr matches :league :ref
  {ao/target      :team/id
   ao/identities  #{:league/id}
   ao/cardinality :many
   ao/schema      :production
   ro/column-EQL  {:league/matches [:match/id :match/match-day :match/date
                                    :match/local :match/local-goals
                                    :match/visitor :match/visitor-goals]}})

(defattr teams :league/teams :ref
  {ao/target      :team/id
   ao/cardinality :many
   ao/identities  #{:league/id}
   ao/schema      :production
   ro/column-EQL  {:league/teams [:team/id
                                  :team/title
                                  :team/score
                                  :team/attack
                                  :team/mid
                                  :team/defence
                                  :team/city [:city/id :city/title]
                                  :team/enable?
                                  :team/palmares]}})

(defattr ladder :league/ladder :ref
  {ao/target      :team/id
   ao/cardinality :many
   ao/identities  #{:league/id}
   ao/schema      :production
   ro/column-EQL  {:league/ladder [:team/title]}})

(defattr completed? :league/completed? :boolean
  {ao/identities  #{:league/id}
   ao/required?   true
   ao/cardinality :one
   ao/schema      :production})

(defattr champion :league/champion :ref
  {ao/target      :team/id
   ao/cardinality :one
   ao/identities  #{:league/id}
   ao/schema      :production})

(def attributes [id year matches teams ladder completed? champion])