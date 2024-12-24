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
  {ao/identities #{:team/id}
   ao/required?  true
   ao/schema     :production})

(defattr league :league :long
  {ao/identities    #{:team/id}
   ao/schema        :production})

(defattr teams :league/teams :ref
  {ao/target     :city/id
   ao/identities #{:team/id}
   ao/schema     :production
   ro/column-EQL {:team/city [:city/id :city/title]}
   })

(defattr enable? :team/enable? :boolean
  {ao/identities    #{:team/id}
   fo/default-value true
   ao/schema        :production})

(def attributes [id title score city enable?])