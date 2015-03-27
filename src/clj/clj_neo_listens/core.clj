(ns clj-neo-listens.core
  (:require  [clojure.set :as cset])
  (:import (java.util.concurrent ExecutorService)
           (org.neo4j.graphdb Direction DynamicLabel DynamicRelationshipType GraphDatabaseService Node Relationship RelationshipType Transaction)
           (org.neo4j.graphdb.event LabelEntry TransactionData TransactionEventHandler)))


(defn get-label
  ^DynamicLabel
  [^String x]
  (DynamicLabel/label x))

(defn ^RelationshipType get-rel
  [^String r]
  (DynamicRelationshipType/withName r))

(defn get-rel-names
  [rel-name]
  (into-array ^RelationshipType [(get-rel rel-name)]))


(def Suspect (get-label "Suspect"))
(def KNOWS   (get-rel "KNOWS"))
(def KNOWS-LIST   (get-rel-names "KNOWS"))

(defn run-logic
  [^TransactionData td ^GraphDatabaseService db]
  (with-open [^Transaction tx  (.beginTx db)]
    (let  [suspects           (set (filter (fn [^Node x]
                                             (.hasLabel x Suspect))
                                           (.createdNodes td)))
           more-suspects      (set (filter (fn [^LabelEntry x]
                                             (and (= (.label x) Suspect)
                                                  (not (contains? suspects (.node x)))))
                                           (.assignedLabels td)))]
      (when (seq suspects)
        (println "A new Suspect has been created!"))
      (when (seq more-suspects)
        (println "A new Suspect has been identified!"))
      (doseq [^Relationship r (.createdRelationships td)]
             (when (.isType r KNOWS)
               (doseq [^Node user  (.getNodes r)]
                      (when (.hasLabel user Suspect)
                        (println "A new direct relationship to a Suspect has been created!"))
                      (doseq [^Relationship knows (.getRelationships
                                                   user
                                                   Direction/BOTH
                                                   #^"[Lorg.neo4j.graphdb.DynamicRelationshipType;" KNOWS-LIST)
                              :let  [^Node otherUser  (.getOtherNode knows user)]]
                        (when (and (.hasLabel otherUser Suspect)
                                   (not= otherUser (.getOtherNode r user)))
                          (println "A new indirect relationship to a Suspect has been created!")))))))))


(deftype SuspectRunnable [^TransactionData td ^GraphDatabaseService db]
  Runnable
  (run [this]
    (require 'clj-neo-listens.core)
    (try
      (run-logic td db)
      (catch Exception e
        (println e)))))


(deftype MyTransactionEventHandler [^GraphDatabaseService db ^ExecutorService ex]
  TransactionEventHandler
  (beforeCommit
    [this ^TransactionData data]
    nil)
  (afterCommit
    [this data state]
    (.submit ex (SuspectRunnable. data db))
    nil)
  (afterRollback
    [this data state]
    nil))
