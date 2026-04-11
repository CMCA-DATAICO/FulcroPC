(ns app.fulcropc.ui.landing-page
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.rad.routing :as rroute]
    [app.fulcropc.ui.team-forms :refer [TeamProfile]]))

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
                    (let [ident [:component/id ::LandingPage]]
                      (dr/route-deferred ident
                        (fn []
                          (df/load! app :league/all-leagues nil {:target (conj ident :league/all-leagues)})
                          (load-data app :team/attributes-teams ident)))))}
  (dom/div :.ui.grid.bg-gray-400
    (dom/div :.flex.flex-col.flex-1
      (dom/div :.font-inter.text-blue.font-bold.text-center.text-4xl.my-6 "FULCRO PROFESIONAL COLOMBIANO")
      (dom/div :.border.border-black.text-center.bg-white.py-4.px-10.mb-8.mx-10.rounded-lg.cursor-pointer
        (if (seq all-leagues)
          (map (fn [{:league/keys [id year]}]
                 (dom/div :.font-poppins.font-semibold.text-2xl {:key id} (str "Liga Mustang " year " - Play HERE!")))
            all-leagues)
          "Loading leagues...")))
    (if attributes-teams
      (let [chunks (partition 4 attributes-teams)]
        (map (fn [row]
               (dom/div :.row.mx-10 {:key (str "row-" (first row))}
                 (map (fn [{:team/keys [id title city badge]}]
                        (dom/div :.four.wide.column {:key id}
                          (dom/div :.flex.flex-row.border.p-8.items-center.justify-center.text-center.gap-4.bg-white.cursor-pointer
                            {:onClick (fn [] (rroute/route-to! this TeamProfile {:team-id (str id)}))}
                            (when badge
                              (dom/img {:src   (str "/shields/" badge)
                                        :alt   title
                                        :class "w-18 h-18 object-contain"}))
                            (dom/div :.flex.flex-col.p-2
                              (dom/div :.font-bold title)
                              (dom/div :.text-sm (str (:city/title city)))))))
                   row)))
          chunks)))))
