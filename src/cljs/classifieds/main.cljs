(ns classifieds.main
  (:require [kioo.reagent :as kioo :refer [content set-attr do-> substitute listen]]
            [reagent.core :as reagent]
            [jayq.core :as jq :refer [$]])
  (:require-macros [kioo.reagent :refer [defsnippet deftemplate]]))


;; Data

(def countries (reagent/atom {:countries [["" "All Countries"]]
                              :country-map {"" "All Countries"}}))


(def data (reagent/atom {:sites []
                         :flash ""}))


;; Ajax stuff

(defn update-country-list! [result]
 (swap! countries assoc :countries (sort-by second result)
                   :country-map (apply hash-map (apply concat (js->clj result)))))

(defn update-countries! []
  (jq/ajax {:url "/api/v1/countries"
             :dataType "json"
             :success update-country-list!}))


(defn update-site-list! [result]
  (swap! data assoc :sites (sort-by :country_code result)))

(defn update-sites! []
  (jq/ajax {:url "/api/v1/sites"
            :dataType "json"
            :success update-site-list!}))

(defn update-sites-by-country! [code]
  (if (empty? code)
    (update-sites!)
    (jq/ajax {:url (str "/api/v1/sites/" code)
              :dataType "json"
              :success update-site-list!})))


(defn submit-site! [e]
  (.preventDefault e)
  (let [form-data (jq/serialize ($ "#submission-form form"))]
    (jq/ajax {:url "/api/v1/sites"
              :type "post"
              :data form-data
              :success (fn [& args]
                         (swap! data assoc :flash "Thanks for your submission. It will be published following a review."))})))


;; Utils

(defn strip-url-to-domain [url]
  (second (re-find #"^https?://([^/]*)/?.*$" url)))

(defn country-option [[code name]]
  [:option {:key code :value code} name])

(defn country-select-options [countries]
  (doall
    (concat [[:option {:key "" :value ""} "All Countries"]]
            (map country-option countries))))

(defn country-by-code [code country-map]
  (get country-map code "Unknown"))

(defn annotate-country-name [countries]
  (fn [site]
    (assoc site "country" (country-by-code (get site "country_code") countries))))


;; Kioo Templates/snippets

(defsnippet table-row-tpl "index.html" [:tbody :tr]
  [site]
  {
  [:.url] (kioo/content [:a {:href (get site "url")}
                            (strip-url-to-domain (get site "url"))] )
  [:.country] (kioo/content (get site "country"))
  [:.city] (kioo/content (get site "city"))
  [:.description] (kioo/content (get site "description"))})

(defsnippet submission-form-tpl "index.html" [:#submission-form]
  []
  {[:form] (if (:flash @data) (kioo/before [:p.flash-message (:flash @data)]))
   [:input.submit] (kioo/listen :on-click submit-site!)
   [:.country-select] (kioo/content (map country-option (:countries @countries))) })

(defsnippet classifieds-list-tpl "index.html" [:#classifieds-list]
  []
  {[:.country-select] (kioo/do->
                        (kioo/content (country-select-options (:countries @countries)))
                        (kioo/listen :on-change #(->> (.-target %)
                                                      ($)
                                                      (jq/val)
                                                      (update-sites-by-country!))))

   [:table :tbody] (kioo/content (->> (:sites @data)
                                      (map (comp (annotate-country-name (:country-map @countries))
                                                 js->clj))
                                      (sort-by #(get % "country"))
                                      (map table-row-tpl)
                                      (doall)))})

(deftemplate index-tpl "index.html" []
  {[:#submission-form]  (kioo/substitute (submission-form-tpl))
   [:#classifieds-list] (kioo/substitute (classifieds-list-tpl))})


;; Start it up

($ (fn []
     (update-countries!)
     (update-sites!)
     (reagent/render-component [index-tpl] (.-body js/document))))
