(ns app.fulcropc.ui.league-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [clojure.string :as str]
    [app.fulcropc.model.team :as r.team]
    [app.fulcropc.model.city :as r.city]
    [app.fulcropc.model.league :as r.league]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [app.fulcropc.ui.team-forms :refer [TeamForm TeamList]]
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as po]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.rad.routing :as rroute]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [taoensso.timbre :as log]))

;; Mutation to toggle a team between available and selected
#?(:cljs
   (m/defmutation toggle-team-selection [{:keys [team-id]}]
     (action [{:keys [state]}]
       (js/console.log "Toggle team:" (str team-id))
       (swap! state update-in [:component/id ::Tournament :ui/selected-teams]
         (fn [selected]
           (let [selected     (or selected #{})
                 new-selected (if (contains? selected team-id)
                                (disj selected team-id)
                                (conj selected team-id))]
             (js/console.log "Selected teams now:" (str new-selected))
             new-selected))))))

(defsc TeamPickerItem [this {:team/keys [id title badge] :as props} {:keys [selected? on-click]}]
  {:query [:team/id :team/title :team/badge]
   :ident :team/id}
  (div :.flex.items-center.gap-3.p-3.bg-white.rounded-lg.cursor-pointer.hover:bg-gray-100.border
    {:onClick on-click}
    (when badge
      (dom/img {:src   (str "shields/" badge)
                :alt   title
                :class "w-10 h-10 object-contain"}))
    (div :.font-semibold title)))

(def ui-team-picker-item (comp/factory TeamPickerItem {:keyfn :team/id}))

(defsc Tournament [this {:ui/keys [all-teams selected-teams] :as props}]
  {:query         [{:ui/all-teams (comp/get-query TeamPickerItem)}
                   :ui/selected-teams]
   :ident         (fn [] [:component/id ::Tournament])
   :route-segment ["tournament"]
   :initial-state {:ui/all-teams      []
                   :ui/selected-teams #{}}
   :will-enter    (fn [app _params]
                    (let [ident [:component/id ::Tournament]]
                      (df/load! app :team/all-teams TeamPickerItem
                        {:target [:component/id ::Tournament :ui/all-teams]})
                      (merge/merge-component! app Tournament {:ui/selected-teams #{}})
                      (dr/route-immediate ident)))}
  (let [selected-teams  (or selected-teams #{})
        available-teams (remove #(contains? selected-teams (:team/id %)) all-teams)
        selected-items  (filter #(contains? selected-teams (:team/id %)) all-teams)]
    (div :.flex.flex-col.gap-6.p-8
      ;; Header
      (div :.text-2xl.font-bold.text-center.border.rounded-lg.bg-white.m-10.p-10 "CAMPEONATO FPC")

      ;; Two column picker
      (div :.grid.grid-cols-2.gap-8

        ;; Available teams (left)
        (div :.flex.flex-col.gap-2
          (div :.font-semibold.text-lg.mb-2 "FPC Primera A " (count selected-items))
          (div :.bg-gray-100.rounded-lg.p-4.min-h-96.flex.flex-col.gap-2
            (if (seq available-teams)
              (map (fn [team]
                     (ui-team-picker-item team
                       {:selected? false
                        :on-click  #(comp/transact! this [(toggle-team-selection {:team-id (:team/id team)})])}))
                available-teams)
              (div :.text-gray-500.text-center.py-8 "No hay equipos disponibles"))))

        ;; Selected teams (right)
        (div :.flex.flex-col.gap-2
          (div :.font-semibold.text-lg.mb-2
            (str "Primera B " (count selected-items)))
          (div :.bg-blue-50.rounded-lg.p-4.min-h-96.flex.flex-col.gap-2.border-2.border-blue-200
            (if (seq selected-items)
              (map (fn [team]
                     (ui-team-picker-item team
                       {:selected? true
                        :on-click  #(comp/transact! this [(toggle-team-selection {:team-id (:team/id team)})])}))
                selected-items)
              (div :.text-gray-500.text-center.py-8 "Haz clic en un equipo para seleccionarlo")))))

      ;; Actions
      (div :.flex.justify-end.gap-4
        (dom/button :.ui.button
          {:onClick #(dr/change-route! this ["leagues"])}
          "Cancelar")
        (dom/button :.ui.primary.button
          {:disabled (empty? selected-items)
           :onClick  #(js/console.log "Next step - create league with teams:" (clj->js selected-teams))}
          "Siguiente")))))

(report/defsc-report LeagueList [this {:ui/keys [current-rows current-page page-count] :as props}]
  {ro/title             "PALMARES"
   ro/source-attribute  :league/all-leagues
   ro/row-pk            r.league/id
   ro/columns           [r.league/year r.league/champion r.league/completed? r.league/teams]
   ro/form-links        {r.league/champion TeamForm}
   ro/column-formatters {:league/champion (fn [_ v _ _] (str (:team/title v)))
                         :league/teams    (fn [_ v _ _] (str (mapv :team/title v)))}
   ro/run-on-mount?     true
   ro/controls          {::new-tournament {:type   :button
                                           :label  "Nuevo torneo"
                                           :action (fn [this] (rroute/route-to! this Tournament {}))}}
   ro/control-layout    {:action-buttons [::new-tournament]}
   ro/route             "leagues"}
  )