(ns sample-1-2.core
  (:require
    [reagent.core :as r]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample-1-2.ajax :as ajax]
    [ajax.core :refer [GET POST]]
    [reitit.core :as reitit]
    [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page (:page @session)) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample-1-2"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]]]]))

;;;;;;;;;;;;;
;;  Title ;;
;;;;;;;;;;;;;
(defn title []
  [:div.hero>div.hero-body>div.content.box
   [:h1.title {:style
               {:color "DarkBlue"}}
    "Calculation"]
   [:div
    [:p {:style
         {:color "DarkBlue"}}
     "welcome to sample 1 & 2 demo"]]])

;;;;;;;;;;;;;;;;;
;; data
;;;;;;;;;;;;;;;;;
(def data1 (r/atom {:x 2 :y 4 :op "+" :sum 0}))
(def data2 (r/atom {:x 3 :y 5 :op "+" :sum 0}))
(def data3 (r/atom {:x 4 :y 6 :op "+" :sum 0}))
(def data4 (r/atom {:x 25 :y 27 :op "+" :sum 10}))


;;;;;;;;;;;;;;;;;;
;; set-total
;;;;;;;;;;;;;;;;;;
(defn- set-total [data total]
  (prn "set-total")
  (swap! data assoc :sum (:total total)))

;;;;;;;;;;;;;;;;;;
;; get-answer() ;;
;;;;;;;;;;;;;;;;;;
(defn get-answer [data]

  (let [x (:x @data) y (:y @data) op (:op @data)]
    (prn "received data are:" x y op)

    (cond

      (= op "+" ) (POST "/api/math/plus"
                    {:headers {"Accept" "application/transit+json"}
                     :params {:x x :y y}
                     :handler #(set-total data  %)})

      (= op "-" ) (POST "/api/math/minus"
                        {:headers {"Accept" "application/transit+json"}
                         :params {:x x :y y}
                         :handler #(set-total data  %)})

      (= op "*" ) (POST "/api/math/multi"
                        {:headers {"Accept" "application/transit+json"}
                         :params {:x x :y y}
                         :handler #(set-total data  %)})

      (= op "/" ) (POST "/api/math/div"
                        {:headers {"Accept" "application/transit+json"}
                         :params {:x x :y y}
                         :handler #(set-total data  %)}))))



;;;;;;;;;;;;;;;;;;;
;; input-field
;;;;;;;;;;;;;;;;;;
(defn input-field [tag id data]
  [:div.field
   [tag
    {:type :number
     :value (id @data)
     :on-change #(do
                   (prn "change" id (-> % .-target .-value))
                   (swap! data
                          assoc
                          id (js/parseInt (-> % .-target .-value)))
                   (get-answer data))}]])

;;;;;;;;;;;;;;;;;;;
;; set-operator
;;;;;;;;;;;;;;;;;;
(defn set-operator [pre curr]
  (prn "set-operator")
  (swap! pre assoc :op curr)
  (prn "  after" pre))

;;;;;;;;;;;;;;;;;;;
;; make-row
;;;;;;;;;;;;;;;;;;
(defn make-row [data]
  [:tr
   [:td [input-field :input.input :x data]]
   [:td [:select {:style {:background-color "LightGrey"}
                  :on-change #(do
                                 (set-operator data (-> % .-target .-value))
                                 (get-answer data))}
         [:option "+"]
         [:option "-"]
         [:option "*"]
         [:option "/"]]]

   [:td [input-field :input.input :y data]]
   [:td {:style {:color "Black"}}
    "="]
   [:td {:style
         {:background-color
          (cond
            (< (:sum @data) 0) "yellow"
            (and (<= 0 (:sum @data)) (< (:sum @data) 20)) "lightgreen"
            (and (<= 20 (:sum @data)) (< (:sum @data) 50)) "lightblue"
            (<= 50 (:sum @data)) "lightsalmon")}}
    (str (:sum @data))]])

(defn home-page []
  [:section.section>div.container>div.content
   [title]

   [:table
    [:tbody
     (make-row data1)
     (make-row data2)
     (make-row data3)
     (make-row data4)]]])


(def pages
  {:home #'home-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]]))


(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(swap! session assoc :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (fetch-docs!)

  (get-answer data1)
  (get-answer data2)
  (get-answer data3)
  (get-answer data4)

  (hook-browser-navigation!)
  (mount-components))
