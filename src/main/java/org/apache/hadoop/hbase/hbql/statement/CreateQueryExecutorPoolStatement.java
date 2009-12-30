/*
 * Copyright (c) 2009.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql.statement;

import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.QueryExecutorPoolManager;
import org.apache.hadoop.hbase.hbql.executor.QueryExecutorPoolDefinition;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

public class CreateQueryExecutorPoolStatement extends BasicStatement implements ConnectionStatement {

    private final QueryExecutorPoolDefinition args;

    public CreateQueryExecutorPoolStatement(final StatementPredicate predicate,
                                            final QueryExecutorPoolDefinition args) {
        super(predicate);
        this.args = args;
    }

    private QueryExecutorPoolDefinition getArgs() {
        return this.args;
    }

    protected ExecutionResults execute(final HConnectionImpl conn) throws HBqlException {

        this.getArgs().validateExecutorPoolPropertyList();

        QueryExecutorPoolManager.newQueryExecutorPool(this.getArgs().getPoolName(),
                                                      this.getArgs().getMaxPoolSize(),
                                                      this.getArgs().getThreadCount(),
                                                      this.getArgs().getThreadsReadResults(),
                                                      this.getArgs().getQueueSize());

        return new ExecutionResults("Executor pool " + this.getArgs().getPoolName() + " created.");
    }


    public static String usage() {
        return "CREATE [QUERY] EXECUTOR POOL pool_name (MAX_POOL_SIZE: int_expr, THREAD_COUNT: int_expr, THREADS_READ_RESULTS: bool_expr, QUEUE_SIZE: int_expr) [IF bool_expr]";
    }
}