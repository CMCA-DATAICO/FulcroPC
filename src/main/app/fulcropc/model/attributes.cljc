(ns app.fulcropc.model.attributes
  "Central place to gather all RAD attributes to ensure they get required and
   stay required.

   Also defines common helpful things related to the attributes of the model, such
   as a default form validator and attribute lookup."
  (:require
    [app.fulcropc.model.account :as account]
    [app.fulcropc.model.file :as m.file]
    [app.fulcropc.model.team :as team]
    [app.fulcropc.model.city :as m.city]
    [app.fulcropc.model.match :as match]
    [app.fulcropc.model.league :as league]
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