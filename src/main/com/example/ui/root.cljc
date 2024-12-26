(ns com.example.ui.root
  "App UI root. Standard Fulcro."
  (:require
    #?@(:cljs [[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]])
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.example.ui.account-forms :refer [AccountForm AccountList]]
    [com.example.ui.team-forms :refer [TeamForm TeamList]]
    [com.example.ui.city-forms :refer [CityForm CityList]]
    [com.example.ui.match-forms :refer [MatchForm MatchList]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.routing.dynamic-routing :refer [defrouter]]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.routing :as rroute]))

(defsc LandingPage [this props]
  {:query         ['*]
   :ident         (fn [] [:component/id ::LandingPage])
   :initial-state {}
   :route-segment ["landing-page"]}
  (dom/div "FUTBOL PROFESIONAL COLOMBIANO - PROBANDO"))

;; This will just be a normal router...but there can be many of them.
(defrouter MainRouter [this {:keys [current-state route-factory route-props]}]
  {:always-render-body? true
   :router-targets      [LandingPage AccountList AccountForm TeamList TeamForm CityList CityForm MatchList MatchForm]}
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
         (dom/div
           (div :.ui.top.menu
                (comp/fragment
                  (dom/div :.ui.item {:onClick (fn [] (rroute/route-to! this LandingPage {}))} "FulcroPC")
                  (ui-dropdown {:className "item" :text "Tournament"}
                               (ui-dropdown-menu {}
                                                 (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this TeamList {}))} "Tournament")
                                                 (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this MatchList {}))} "All Matches")
                                                 (ui-dropdown-item {:onClick (fn [] (form/create! this MatchForm {}))} "New Match")
                                                 ))
                  (ui-dropdown {:className "item" :text "Team"}
                               (ui-dropdown-menu {}
                                                 (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this TeamList {}))} "All Teams")
                                                 ;(ui-dropdown-item {:onClick (fn [] (form/create! this TeamForm {:cities (get-in props [:city/all-cities])}))} "New Team")
                                                 (ui-dropdown-item {:onClick (fn [] (form/create! this TeamForm {}))} "New Team")
                                                 ))
                  (ui-dropdown {:className "item" :text "City"}
                               (ui-dropdown-menu {}
                                                 (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this CityList {}))} "All City")
                                                 (ui-dropdown-item {:onClick (fn [] (form/create! this CityForm))} "New City")
                                                 ))
                  (div :.ui.tiny.loader {:classes [(when busy? "active")]})))
           (div :.ui.segment
                (ui-main-router router))))
       (div :.ui.active.dimmer
            (div :.ui.large.text.loader "Loading")))))

(def ui-root (comp/factory Root))

