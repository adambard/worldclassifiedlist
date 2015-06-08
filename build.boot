(set-env!
 :source-paths #{"src/clj" "src/cljs"}
 :dependencies '[;Tasks
                 [mbuczko/boot-ragtime "0.1.2"]
                 [adzerk/boot-cljs "0.0-2814-3"]
                 [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT"]
                 [adzerk/boot-reload "0.2.6"]
                 [pandeiro/boot-http "0.6.3-SNAPSHOT"]
                 [com.joshuadavey/boot-middleman "0.0.3"]

                 ; Clojure
                 [org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [org.postgresql/postgresql "9.3-1103-jdbc41"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.18"]

                 ; Clojurescript
                 [org.clojure/clojurescript "0.0-3123"]
                 [jayq "2.5.4"]
                 [kioo "0.4.1-SNAPSHOT"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.5.1"]
])

(require
  '[mbuczko.boot-ragtime :refer [ragtime]]
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[classifieds.server :refer [-main]]
  '[pandeiro.boot-http :refer [serve]]
  '[com.joshuadavey.boot-middleman :refer [middleman]]
  '[environ.core :refer [env]]
)


(task-options!
 ragtime {:driver-class "org.postgresql.Driver"
          :database (str "jdbc:postgresql://localhost:5432/classifieds?user=classifieds_user&password=" (:database-password env))})


(defn get-port []
  (Integer/parseInt (get env :port "8080")))

(deftask serve-api [])

(deftask clj-dev []
  (comp
    (serve :port (get-port) 
           :handler 'classifieds.server/app
           :httpkit true
           :reload true)
    (repl)))

(deftask cljs-dev []
  (comp
    (serve :port (get-port) :handler 'classifieds.server/app :httpkit true)
    (watch)
    (middleman :dir "html")
    ;(reload)
    ;(cljs-repl)
    (cljs :optimizations :none :source-map true)))

(deftask build []
  (comp
    (middleman :dir "html")
    (cljs :optimizations :whitespace)))

(deftask run-server []
  (comp
    (serve :port (get-port) :handler 'classifieds.server/app :httpkit true)
    (wait)))

