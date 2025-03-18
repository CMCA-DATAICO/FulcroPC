(ns com.example.model-rad.team
  "RAD definition of an `account`. Attributes only. These will be used all over the app, so try to limit
   requires to model code and library code."
  (:require
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.report-options :as ro]))

(defattr id :team/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr title :team/title :string
  {ao/identities  #{:team/id}
   ao/required?   true
   ao/cardinality :one
   ao/schema      :production})

(defattr score :team/score :long
  {ao/identities    #{:team/id}
   ao/cardinality   :one
   fo/default-value 0
   ao/schema        :production})

(defattr attack :team/attack :long
  {ao/identities    #{:team/id}
   ao/cardinality   :one
   fo/default-value (rand-int 100)
   ao/schema        :production})

(defattr mid :team/mid :long
  {ao/identities    #{:team/id}
   ao/cardinality   :one
   fo/default-value (rand-int 100)
   ao/schema        :production})

(defattr defence :team/defence :long
  {ao/identities    #{:team/id}
   ao/cardinality   :one
   fo/default-value (rand-int 100)
   ao/schema        :production})

(defattr city :team/city :ref
  {ao/target     :city/id
   ao/identities #{:team/id}
   ao/schema     :production
   ro/column-EQL {:team/city [:city/id :city/title]}
   })

(defattr enable? :team/enable? :boolean
  {ao/identities    #{:team/id}
   fo/default-value true
   ao/schema        :production})

(defattr palmares :team/palmares :ref
  {ao/target      :league/id
   ao/identities  #{:team/id}
   ao/schema      :production
   ao/cardinality :many
   ro/column-EQL  {:team/palmares [:league/id :league/year :league/champion :league/completed?]}
   })

(def attributes [id title score attack mid defence city enable? palmares])