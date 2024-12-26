(ns com.example.ui.match-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [clojure.string :as str]
    [com.example.model-rad.team :as r.team]
    [com.example.model-rad.match :as r.match]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as po]))


(form/defsc-form MatchForm [this props]
  {fo/id            r.match/id
   fo/attributes    [r.match/league r.match/date r.match/match-day
                     r.match/local r.match/local-goals
                     r.match/visitor r.match/visitor-goals]
   fo/route-prefix  "match"
   fo/title         "Create Match"
   fo/field-styles  {:match/local   :pick-one
                     :match/visitor :pick-one}
   fo/field-options {:match/local   {po/query-key       :team/all-teams
                                     po/query-component (rc/nc [:team/title :team/id])
                                     po/options-xform   (fn [normalize-response raw-response]
                                                          (mapv
                                                            (fn [{:team/keys [id title]}]
                                                              {:text title :value [:team/id id]})
                                                            (sort-by :team/title raw-response)
                                                            ))}
                     :match/visitor {po/query-key       :team/all-teams
                                     po/query-component (rc/nc [:team/title :team/id])
                                     po/options-xform   (fn [normalize-response raw-response]
                                                          (mapv
                                                            (fn [{:team/keys [id title]}]
                                                              {:text title :value [:team/id id]})
                                                            (sort-by :team/title raw-response)
                                                            ))}}})


(report/defsc-report MatchList [this {:ui/keys [current-rows current-page page-count] :as props}]
  {ro/title            "All Matches"
   ro/source-attribute :match/all-matches
   ro/row-pk           r.match/id
   ro/columns          [r.match/league r.match/match-day r.match/date
                        r.match/local r.match/local-goals
                        r.match/visitor-goals r.match/visitor]
   ro/column-formatters {:match/local (fn [_ v _ _] (str (:team/title v)))
                         :match/visitor (fn [_ v _ _] (str (:team/title v)))
                         }
   ;ro/form-links          {r.team/title TeamForm}
   ro/run-on-mount? true
   ro/controls            {::new-match     {:type   :button
                                           :local? true
                                           :label  "New Match"
                                           :action (fn [this _] (form/create! this MatchForm))}}
   ro/control-layout      {:action-buttons [::new-match]}

   #_ro/initial-sort-params #_{:sort-by          :match/match-day
                               :ascending?       true
                               :sortable-columns #{:match/match-day}}

   ro/route "matches"}
  #_:defaults              #_{:current-rows [] :current-page 0 :page-count 1})