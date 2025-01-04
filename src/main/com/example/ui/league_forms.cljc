(ns com.example.ui.league-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [clojure.string :as str]
    [com.example.model-rad.team :as r.team]
    [com.example.model-rad.city :as r.city]
    [com.example.model-rad.league :as r.league]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.example.ui.team-forms :refer [TeamForm TeamList]]
    [com.wsscode.pathom.connect :as pc :refer [defmutation]]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as po]
    [com.example.model.league :as le]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [taoensso.timbre :as log]))


(report/defsc-report LeagueList [this {:ui/keys [current-rows current-page page-count] :as props}]
  {ro/title             "PALMARES"
   ro/source-attribute  :league/all-leagues
   ro/row-pk            r.league/id
   ro/columns           [r.league/year r.league/champion r.league/completed?]
   ro/form-links        {r.league/champion TeamForm}
   ro/column-formatters {:league/champion (fn [_ v _ _] (tap> v) (str (:team/title v)))}
   ro/run-on-mount? true
   ro/route "leagues"})
