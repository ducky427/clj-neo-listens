(ns clj-neo-listens.core
  (:import (java.util.concurrent ExecutorService)
           (org.neo4j.graphdb GraphDatabaseService Transaction)
           (org.neo4j.graphdb.event TransactionData TransactionEventHandler)))


(deftype MyTransactionEventHandler [^GraphDatabaseService db ^ExecutorService ex]
  TransactionEventHandler
  (beforeCommit
    [this ^TransactionData data]
    (println "Committing Transaction!"))
  (afterCommit
    [this data state]
    (println "Committed Transaction!")
    nil)
  (afterRollback
    [this data state]
    (println "Transaction rolled back!")
    nil))
