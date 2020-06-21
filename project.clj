(defproject paintparty/style-plus "0.5.0-SNAPSHOT"
  :description "Co-locate your style"
  :url "https://github.com/paintparty/style-plus"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [stylefy "2.2.0"]]
  :repl-options {:init-ns style-plus.core}
  :deploy-repositories [["releases"  {:sign-releases false :url "https://repo.clojars.org"}]])
