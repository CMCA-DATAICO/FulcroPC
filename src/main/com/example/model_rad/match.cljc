(ns com.example.model-rad.match
  "RAD definition of an `account`. Attributes only. These will be used all over the app, so try to limit
   requires to model code and library code."
  (:require
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.report-options :as ro]))

(defattr id :match/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr league :match/league :ref
  {ao/target      :league/id
   ao/identities  #{:match/id}
   ao/cardinality :one
   ro/column-EQL  {:match/league [:league/id :league/year]}
   ao/schema      :production})

(defattr date :match/date :instant
  {ao/identities  #{:match/id}
   ao/cardinality :one
   ao/schema      :production})

(defattr match-day :match/match-day :long
  {ao/identities  #{:match/id}
   ao/required?   true
   ao/cardinality :one
   ao/schema      :production})

(defattr local :match/local :ref
  {ao/target      :team/id
   ao/identities  #{:match/id}
   ao/cardinality :one
   ao/required?   true
   ao/schema      :production
   ro/column-EQL  {:match/local [:team/id :team/title :team/score :team/enable?]}
   })

(defattr visitor :match/visitor :ref
  {ao/target      :team/id
   ao/identities  #{:match/id}
   ao/cardinality :one
   ao/required?   true
   ao/schema      :production
   ro/column-EQL  {:match/visitor [:team/id :team/title :team/score :team/enable?]}
   })

(defattr local-goals :match/local-goals :long
  {ao/identities  #{:match/id}
   ao/cardinality :one
   ao/schema      :production
   })

(defattr visitor-goals :match/visitor-goals :long
  {ao/identities  #{:match/id}
   ao/cardinality :one
   ao/schema      :production
   })

(def attributes [id date league match-day local visitor local-goals visitor-goals])