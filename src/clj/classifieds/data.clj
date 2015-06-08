(ns classifieds.data
  (:require [environ.core :refer [env]]
            [clojure.java.jdbc :as j]
            [jdbc.pool.c3p0 :as pool]))


(defonce my-db {:subprotocol "postgresql"
                :subname (str "//127.0.0.1:5432/"
                              (get env :database-name "classifieds"))
                :classname "org.postgresql.Driver"
                :user (get env :database-user "classifieds_user")
                :password (get env :database-password)})

(defonce my-pool (pool/make-datasource-spec
                   (assoc my-db
                      :initial-pool-size 3
                      :min-pool-size 3)))


(defn get-all-sites []
  (j/query my-pool 
           ["SELECT * FROM classified_site WHERE published"]))


(defn get-sites-by-country [country-code]
  (j/query my-pool
           ["SELECT * FROM classified_site WHERE country_code=? AND published"
            country-code]))


(defn put-site! [params]
  (j/insert! my-pool :classified_site
             (-> params
                 (select-keys [:country_code :city :url :description :published])
                 (assoc :published false))))
