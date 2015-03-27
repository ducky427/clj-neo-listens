(def neo4j-version "2.2.0")

(defproject clj-neo-listens "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :profiles {:provided {:dependencies [[org.neo4j/neo4j ~neo4j-version]
                                     [org.neo4j/neo4j-kernel ~neo4j-version :classifier "tests" :scope "test"]

                                     ;; for testing
                                     [org.neo4j.test/neo4j-harness ~neo4j-version :scope "test"]
                                     [org.neo4j/neo4j-io ~neo4j-version :classifier "tests" :scope "test"]
                                     [clj-http "1.0.1"]]}
           :uberjar {:aot :all}}
  :prep-tasks [["compile" "clj-neo-listens.core"]
                "javac" "compile"]
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.6.0"]])
