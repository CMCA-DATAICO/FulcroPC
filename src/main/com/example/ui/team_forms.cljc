(ns com.example.ui.team-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [clojure.string :as str]
    [com.example.model-rad.team :as r.team]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [taoensso.timbre :as log]))

#_(m/defmutation toggle-team-active
    [{:keys [team/id]}]
    (remote [env]
            (assoc env :remote `com.example.server.mutations/set-team-active)))


(form/defsc-form TeamForm [this props]
  {fo/id             r.team/id
   fo/attributes     [r.team/title
                      r.team/city]
   fo/route-prefix   "team"
   fo/title          "Create Teams"
   fo/field-styles   {:team/city :pick-one}
   fo/controls {:team/city
                    (fn [this value _ {:keys [onChange cities]}]
                      (dom/select
                        {:value    (str value)
                         :onChange #(onChange (js/parseInt (.. % -target -value)))}
                        (dom/option {:value ""} "-- Select a City --")
                        (map (fn [{:keys [id title]}]
                               (dom/option {:key id :value id} title))
                             cities)))}
   })

#_:query #_[:team/id :team/title :team/score :team/city :team/enable?
            {:city/all-cities [:id :title]}]



;; NOTE: any form can be used as a subform, but when you do so you must add addl config here
;; so that computed props can be sent to the form to modify its layout. Subforms, for example,
;; don't get top-level controls like "Save" and "Cancel".
#_fo/subforms #_{:team/city {fo/ui                    CityForm
                             fo/title                 "City"
                             fo/can-delete?           (fn [_ _] true)
                             fo/layout-styles         {:ref-container :city}
                             ::form/added-via-upload? true}}

(defsc TeamListItem [this {:team/keys [id title score city enable?] :as props}
                     {:keys [report-instance row-class ::report/idx]}]
  {:query [:team/id :team/title :team/city :team/score :team/enable?]
   :ident :team/id}
  (let [{:keys [edit-form entity-id]} (report/form-link report-instance props :team/id)]
    (dom/div :.item
             (dom/i :.large.github.middle.aligned.icon)
             (div :.content
                  (if edit-form
                    (dom/a :.header {:onClick (fn [] (form/edit! this edit-form entity-id))} title)
                    (dom/div :.header title)))))
  #_(dom/tr
      (dom/td :.right.aligned title)
      (dom/td (str active?)))

  )

(def ui-team-list-item (comp/factory TeamListItem))

(report/defsc-report TeamList [this {:ui/keys [current-rows current-page page-count] :as props}]
  {ro/title               "All Teams"
   ro/source-attribute    :team/all-teams
   ro/row-pk              r.team/id
   ro/columns             [r.team/title r.team/city r.team/score r.team/enable?]
   ro/column-formatters   {:team/city (fn [_ v _ _] (str (:city/title v)))}
   ro/form-links {r.team/title TeamForm}

   ;; NOTE: You can uncomment these 3 lines to see how to switch over to using handwritten row rendering, with a list style
   ;::report/layout-style             :list
   ;::report/row-style                :list
   ;::report/BodyItem                 teamListItem
   ;;ro/form-links       {r.team/id TeamForm}
   ;; ro/column-formatters   {:team/active? (fn [this v] (if v "Yes" "No"))}
   ;;ro/row-visible?
   #_(fn [{::keys [filter-title]} {:team/keys [title]}]
       (let [nm (some-> title (str/lower-case))
             target (some-> filter-title (str/trim) (str/lower-case))]
         (or
           (nil? target)
           (empty? target)
           (and nm (str/includes? nm target)))))
   ro/run-on-mount?       true

   ro/initial-sort-params {:sort-by          :team/title
                           :ascending?       true
                           :sortable-columns #{:team/title}}

   ro/controls            {::new-team     {:type   :button
                                           :local? true
                                           :label  "New Team"
                                           :action (fn [this _] (form/create! this TeamForm))}
                           ::search!      {:type   :button
                                           :local? true
                                           :label  "Filter"
                                           :class  "ui basic compact mini red button"
                                           :action (fn [this _] (report/filter-rows! this))}
                           ::filter-title {:type        :string
                                           :local?      true
                                           :placeholder "Type a partial title and press enter."
                                           :onChange    (fn [this _] (report/filter-rows! this))}
                           #_:show-inactive? #_{:type          :boolean
                                                :local?        true
                                                :style         :toggle
                                                :default-value false
                                                :onChange      (fn [this _] (control/run! this))
                                                :label         "Show Inactive teams?"}}

   ro/control-layout      {:action-buttons [::new-team ::new-city]
                           :inputs         [[::filter-title ::search! :_]]}

   ro/row-actions         [{:label "Primera"
                            :action (fn [report-instance {:team/keys [id]}]
                                          #?(:cljs
                                             (comp/transact! report-instance
                                                             [(com.example.ui.team-forms/toggle-team-active {:team/id id})])))
                            }
                           {:label  "Segunda"
                            ;:action (print "Helado")
                            #_(fn [report-instance {:team/keys [id]}]
                                #_#?(:cljs
                                     (comp/transact! report-instance [(team/set-team-active {:team/id id})])))
                            ;:visible?  (fn [_ row-props] (:team/active? row-props))
                            ;:disabled? (fn [_ row-props] (not (:team/active? row-props)))
                            }]

   ro/route               "teams"})

#_(div
    ;(report/render-controls this)
    (report/render-control this ::new-team)
    (dom/button :.ui.green.button {:onClick (fn [] (form/create! this teamForm))}
                "Boo")
    #_(div :.ui.form
           (div :.field
                (dom/label "Filter")
                (report/render-control this ::filter-title)))
    #_(dom/div :.ui.list
               (mapv (fn [row]
                       (ui-team-list-item row))
                     current-rows)))
