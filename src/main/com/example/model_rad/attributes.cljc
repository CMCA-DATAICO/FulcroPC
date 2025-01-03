(ns com.example.model-rad.attributes
  "Central place to gather all RAD attributes to ensure they get required and
   stay required.

   Also defines common helpful things related to the attributes of the model, such
   as a default form validator and attribute lookup."
  (:require
    [com.example.model-rad.account :as account]
    [com.example.model-rad.file :as m.file]
    [com.example.model-rad.team :as team]
    [com.example.model-rad.city :as m.city]
    [com.example.model-rad.match :as match]
    [com.example.model-rad.league :as league]
    [com.fulcrologic.rad.attributes :as attr]))

(def all-attributes (into []
                      (concat
                        account/attributes
                        m.file/attributes
                        team/attributes
                        m.city/attributes
                        match/attributes
                        league/attributes
                        )))

(def key->attribute (attr/attribute-map all-attributes))

(def all-attribute-validator (attr/make-attribute-validator all-attributes))