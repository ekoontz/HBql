#!/usr/bin/env bash

#HBQL_HOME=/Users/pambrose/git/hbql
#HBASE_HOME=/Users/pambrose/src/hbase-0.20.1

HBASE_CLASSPATH=$HBASE_HOME/conf:$HBASE_HOME/hbase-0.20.1.jar:$HBASE_HOME/lib/hadoop-0.20.1-hdfs127-core.jar:$HBASE_HOME/lib/zookeeper-3.2.1.jar:$HBASE_HOME/lib/log4j-1.2.15.jar:$HBASE_HOME/lib/commons-logging-1.0.4.jar

HBQL_CLASSPATH=$HBQL_HOME/lib/jline-0.9.94.jar:$HBQL_HOME/lib/antlr-runtime-3.1.3.jar:$HBQL_HOME/target/classes

java -classpath $HBASE_CLASSPATH:$HBQL_CLASSPATH org.apache.hadoop.hbase.contrib.hbql.Console
