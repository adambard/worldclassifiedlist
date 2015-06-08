(ns classifieds.server
  (:require [clojure.java.jdbc :as j]
            [jdbc.pool.c3p0 :as pool]
            [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [org.httpkit.server :refer [run-server]]
            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            ))

;; DATABASE

(defonce my-db {:subprotocol "postgresql"
            :subname "//127.0.0.1:5432/classifieds"
            :classname "org.postgresql.Driver"
            :user "classifieds_user"
            :password (get env :database-password)})
(defonce my-pool my-db)


(defn get-all-sites []
  (j/query my-pool 
           ["SELECT * FROM classified_site"]))

(defn get-sites-by-country [country-code]
  (j/query my-pool
           ["SELECT * FROM classified_site WHERE country_code=?"
            country-code]))

(defn put-site! [params]
  (j/insert! my-pool :classified_site
             (-> params
                 (select-keys [:country_code :city :url :description :published])
                 (assoc :published false))))

;; Util

(defn country-codes []
  (for [country (into [] (java.util.Locale/getISOCountries))]
    [country (.getDisplayCountry (java.util.Locale. "en" country))]
    )
  )


;; Compojure

(defn json-response [data]
  {:body (json/write-str data)
   :status 200
   :headers {"Content-Type" "application/json"} })

(defroutes app-routes
  (context "/api/v1" []
    (GET "/countries" [] (json-response (country-codes)))
    (GET "/sites" [] (json-response (filter :published (get-all-sites))))
    (POST "/sites" {params :params} (put-site! params))
    (GET "/sites/:country-code" [country-code] (json-response (filter :published (get-sites-by-country country-code)))))
  (route/files "/" {:root (get env :classified-files "target")})
  )

(def app (-> app-routes
             (wrap-keyword-params)
             (wrap-params)))

(defn -main []
  (run-server #'app-routes {:port (Integer/parseInt (or (get env :port "8080")))}))

(comment
  (-main)
  (put-site! {:country_code "US"
                 :city nil
                 :url "http://craigslist.org"
                 :description "You know craigslist"
                 :published true
                 :bonkers-shit "TEASDFASDFASDF"})
  (get-all-sites)

  (j/update! my-pool :classified_site
             {:published true}
             []
             )
  )
