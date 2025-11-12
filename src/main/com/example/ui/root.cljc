(ns com.example.ui.root
  "App UI root. Standard Fulcro."
  (:require
    #?(:clj [com.example.model-rad.team :as team])
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]])
    #?(:cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    #?@(:cljs [[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]])
    [com.example.ui.account-forms :refer [AccountForm AccountList]]
    [com.example.ui.team-forms :refer [TeamForm TeamList]]
    [com.example.ui.city-forms :refer [CityForm CityList]]
    [com.example.ui.match-forms :refer [MatchForm MatchList]]
    [com.example.ui.league-forms :refer [LeagueList]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.routing.dynamic-routing :refer [defrouter]]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.routing :as rroute]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))


(defn load-data [app key target]
  (df/load! app key nil {:target               (conj target key)
                         :post-mutation        `dr/target-ready
                         :post-mutation-params {:target target}}))

(defsc LandingPage [this {:keys [league/all-leagues team/attributes-teams]}]
  {:query         [{:league/all-leagues [:league/id :league/year]}
                   {:team/attributes-teams [:team/id :team/title :team/city :team/badge]}]
   :ident         (fn [] [:component/id ::LandingPage])
   :initial-state {}
   :route-segment ["landing-page"]
   :will-enter    (fn [app _]
                    (dr/route-deferred
                      [:component/id ::LandingPage]
                      (fn []
                        (load-data app :league/all-leagues [:component/id ::LandingPage])
                        (load-data app :team/attributes-teams [:component/id ::LandingPage]))))}
  (dom/div :.ui.grid.bg-gray-400
    (dom/div :.row
      (dom/div :.column
        (dom/div :.flex.flex-col
          (dom/div :.ui.container.mt-4
            (dom/div :.font-inter.text-blue.font-bold.text-center.text-4xl "FULCRO PROFESIONAL COLOMBIANO"))
          (dom/div :.border.border-black.text-center.bg-white.m-10.py-4.rounded-lg.cursor-pointer
            (if all-leagues
              (map (fn [{:league/keys [id year]}]
                     (dom/div :.font-poppins.font-semibold.text-2xl {:key id} (str "Liga Mustang " year)))
                all-leagues)
              "Loading leagues...")))))
    (if attributes-teams
      (let [chunks (partition 4 attributes-teams)]          ;; Partition the data into groups of 4
        (map (fn [row]
               (dom/div :.row.w-full.mx-10 {:key (str "row-" (first row))}
                 (map (fn [{:team/keys [id title city badge]}]
                        (dom/div :.four.wide.column {:key id}
                          (dom/div :.flex.flex-row.border.p-8.items-center.justify-center.text-center.gap-4.bg-white.cursor-pointer
                            (when badge
                              (dom/img {:src   (str "shields/" badge)
                                        :alt   title
                                        :class "w-18 h-18 object-contain"}))
                            (dom/div :.flex.flex-col.p-2
                              (dom/div :.font-bold title)
                              (dom/div :.text-sm (str (:city/title city)))))))
                   row)))
          chunks)))))

;; This will just be a normal router...but there can be many of them.
(defrouter MainRouter [this {:keys [current-state route-factory route-props]}]
  {:always-render-body? true
   :router-targets      [LandingPage
                         AccountList AccountForm
                         TeamList TeamForm
                         CityList CityForm
                         MatchList MatchForm
                         LeagueList]}
  ;; Normal Fulcro code to show a loader on slow route change (assuming Semantic UI here, should
  ;; be generalized for RAD so UI-specific code isn't necessary)
  (dom/div
    (dom/div :.ui.loader {:classes [(when-not (= :routed current-state) "active")]})
    (when route-factory
      (route-factory route-props))))

(def ui-main-router (comp/factory MainRouter))

(defsc Root [this {::app/keys [active-remotes]
                   :ui/keys   [ready? router]}]
  {:query         [:ui/ready?
                   {:ui/router (comp/get-query MainRouter)}
                   ::app/active-remotes]
   :initial-state {:ui/ready? false
                   :ui/router {}}}
  #?(:cljs
     (if ready?
       (let [busy? (seq active-remotes)]
         (dom/div {:style {:background "linear-gradient(to right, #FBBF24 0%, #FBBF24 50%, #2563EB 50%, #2563EB 75%, #DC2626 75%, #DC2626 100%)"}}
           (div :.ui.top.menu
             (comp/fragment
               (dom/div :.ui.item.cursor-pointer {:onClick (fn [] (rroute/route-to! this LandingPage {}))} "FulcroPC")
               (ui-dropdown {:className "item" :text "Tournament"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this LeagueList {}))} "Tournament")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this MatchList {}))} "All Matches")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this MatchForm {}))} "New Match")
                   ))
               (ui-dropdown {:className "item" :text "Team"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this TeamList {}))} "All Teams")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this TeamForm {}))} "New Team")
                   ))
               (ui-dropdown {:className "item" :text "City"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this CityList {}))} "All City")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this CityForm))} "New City")
                   ))
               (div :.ui.tiny.loader {:classes [(when busy? "active")]})))
           (div :.ui.basic.segment
             (ui-main-router router))))
       (div :.ui.active.dimmer
         (div :.ui.large.text.loader "Loading")))))

(def ui-root (comp/factory Root))