(ns com.example.model-rad.city
  "RAD definition of a `file`. Attributes only. These will be used all over the app, so try to limit
   requires to model code and library code."
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]))

(defattr id :city/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr title :city/title :string
  {ao/identities #{:city/id}
   ao/schema    :production})

(def attributes [id title])
