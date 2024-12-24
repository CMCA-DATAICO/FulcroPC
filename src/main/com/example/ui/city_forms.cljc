(ns com.example.ui.city-forms
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.example.model-rad.city :as r.city]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]))

(form/defsc-form CityForm [this props]
  {fo/id           r.city/id
   fo/attributes   [r.city/id
                    r.city/title]
   fo/default-value {}
   fo/route-prefix "city"
   fo/title        "Edit City"})

(report/defsc-report CityList [this {:ui/keys [current-rows current-page page-count] :as props}]
  {ro/title               "All Cities"
   ro/source-attribute    :city/all-cities
   ro/row-pk              r.city/id
   ro/columns             [r.city/title]
   ro/form-links          {r.city/title CityForm}
   ro/run-on-mount?       true
   ro/route               "city-list"
   ro/initial-sort-params {:sort-by          :city/title
                           :ascending?       true
                           :sortable-columns #{:city/title}}
   ro/controls            {::new-city {:type   :button
                                       :local? true?
                                       :label  "New City"
                                       :action (fn [this _] (form/create! this CityForm))}
                           }
   ro/control-layout      {:action-buttons [::new-city]}})


