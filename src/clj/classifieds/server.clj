(ns classifieds.server
  (:require [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]

            [classifieds.data :refer [get-all-sites get-sites-by-country put-site!]]
            ))

(defn json-response [data]
  {:body (json/write-str data)
   :status 200
   :headers {"Content-Type" "application/json"} })


(defmacro r
  "Save precious characters by automatically json-responsing"
  [method route params & body]
  `(~method ~route ~params (json-response ~@body)))


(defn country-codes []
  (for [country (into [] (java.util.Locale/getISOCountries))]
    [country (.getDisplayCountry (java.util.Locale. "en" country))]))


(defroutes app-routes
  (context "/api/v1" []

    (r GET "/countries" []
      (country-codes))

    (r GET "/sites" []
      (get-all-sites))

    (r POST "/sites" {params :params}
      (put-site! params))

    (r GET "/sites/:country-code" [country-code]
      (get-sites-by-country country-code)))

  ; Dev server only, handled by nginx on prod
  (route/files "/" {:root (get env :classified-files "target")}))


(def app (-> app-routes
             (wrap-keyword-params)
             (wrap-params)))
