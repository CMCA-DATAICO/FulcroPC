(ns app.fulcropc.ui.team-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as po]
    [clojure.string :as str]
    [app.fulcropc.model.team :as r.team]
    [app.fulcropc.model.city :as r.city]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]))



#_(m/defmutation toggle-team-active
    [{:keys [team/id]}]
    (remote [env]
      (assoc env :remote `app.fulcropc.server.mutations/set-team-active)))

#?(:cljs
   (m/defmutation toggle-enable [{:keys [team/id] :as params}]
     (action [{:keys [app state] :as env}]
       (swap! state update-in [:team/id id :team/enable?] not))
     (remote [env] true)
     (ok-action [{:keys [app state result]}]
       (println "It WORK!"))))

(form/defsc-form TeamForm
  [this props]
  {fo/id           r.team/id
   fo/attributes   [r.team/title
                    r.team/city]
   fo/route-prefix "team"
   fo/title        "Team Form"})



#_(form/defsc-form TeamForm [this props]
    [fo/id r.team/id
     fo/attributes [r.team/title
                    r.team/city]
     fo/route-prefix "team"
     fo/title "Create Teams"
     fo/field-styles {:team/city :pick-one}
     fo/field-options {:team/city {po/query-key       :city/all-cities
                                   po/query-component (rc/nc [:city/title :city/id])
                                   po/options-xform   (fn [normalize-response raw-response]
                                                        (mapv
                                                          (fn [{:city/keys [id title]}]
                                                            {:text title :value [:city/id id]})
                                                          (sort-by :city/title raw-response)
                                                          ))}}])


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
   ro/columns             [r.team/title r.team/city r.team/score r.team/enable? r.team/palmares]
   ro/column-formatters   {:team/city (fn [_ v _ _] (str (:city/title v)))}
   ro/form-links          {r.team/title TeamForm}

   ;; NOTE: You can uncomment these 3 lines to see how to switch over to using handwritten row rendering, with a list style
   ;::report/layout-style             :list
   ;::report/row-style                :list
   ;::report/BodyItem                 teamListItem
   ;;ro/form-links       {r.team/id TeamForm}
   ;; ro/column-formatters   {:team/active? (fn [this v] (if v "Yes" "No"))}
   ;;ro/row-visible?
   #_(fn [{::keys [filter-title]} {:team/keys [title]}]
       (let [nm     (some-> title (str/lower-case))
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

   ro/control-layout      {:action-buttons [::new-team]
                           :inputs         [[::filter-title ::search! :_]]}

   ro/row-actions         [{:label  "Can Play?"
                            :action (fn [this {:team/keys [id]}]
                                      #?(:cljs
                                         (comp/transact! this
                                           [(toggle-enable {:team/id id})])))
                            }
                           ]

   ro/route               "teams"})


(defsc TeamProfile [this {:team/keys [id title score attack mid defense city enable? badge palmares]}]
  {:query         [:team/id :team/title :team/score :team/attack :team/mid :team/defense
                   :team/enable? :team/badge
                   {:team/city [:city/id :city/title]}
                   {:team/palmares [:league/id :league/year]}]
   :ident         :team/id
   :initial-state {:team/id nil}
   :route-segment ["team-profile" :team-id]
   :will-enter    (fn [app {:keys [team-id]}]
                    (let [team-id (uuid team-id)]
                      (df/load! app [:team/id team-id] TeamProfile)
                      (dr/route-immediate [:team/id team-id])))}
  (dom/div :.ui.segment
    (dom/div :.flex.flex-row.items-center.gap-4.mb-6
      (if badge
        (dom/img {:src   (str "/shields/" badge)
                  :alt   title
                  :class "w-24 h-24 object-contain"})
        (dom/div "Imagen"))
      (dom/div
        (dom/h2 :.font-poppins.font-bold.text-3xl title)
        (dom/p :.text-lg (str (:city/title city)))))
    (dom/div :.ui.three.statistics
      (dom/div :.statistic
        (dom/div :.value (str attack))
        (dom/div :.label "Attack"))
      (dom/div :.statistic
        (dom/div :.value (str mid))
        (dom/div :.label "Midfield"))
      (dom/div :.statistic
        (dom/div :.value (str defense))
        (dom/div :.label "Defense")))
    (dom/div :.mt-6
      (dom/h3 :.font-bold.text-xl "Score: " (str score))
      (when (seq palmares)
        (dom/div :.mt-4
          (dom/h4 :.font-bold "Palmares")
          (dom/ul
            (map (fn [{:league/keys [id year]}]
                   (dom/li {:key (str id)} (str "Liga " year)))
              palmares)))))))