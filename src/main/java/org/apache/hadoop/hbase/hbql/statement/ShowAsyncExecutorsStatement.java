/*
 * Copyright (c) 2010.  The Apache Software Foundation
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

import org.apache.hadoop.hbase.hbql.client.AsyncExecutor;
import org.apache.hadoop.hbase.hbql.client.AsyncExecutorManager;
import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

public class ShowAsyncExecutorsStatement extends GenericStatement implements ConnectionStatement {

    public ShowAsyncExecutorsStatement() {
        super(null);
    }

    protected ExecutionResults execute(final HConnectionImpl conn) throws HBqlException {

        final ExecutionResults retval = new ExecutionResults();
        retval.out.println("Async Executors: ");
        for (final String name : AsyncExecutorManager.getAsyncExecutorNames()) {
            final AsyncExecutor asyncExecutor = AsyncExecutorManager.getAsyncExecutor(name);
            retval.out.println("\t" + asyncExecutor.getName() + "("
                               + "MIN_THREAD_COUNT: " + asyncExecutor.getMinThreadCount()
                               + ", MAX_THREAD_COUNT: " + asyncExecutor.getMaxThreadCount()
                               + ", KEEP_ALIVE_SECS: " + asyncExecutor.getKeepAliveSecs()
                               + ")");
        }
        retval.out.flush();
        return retval;
    }

    public static String usage() {
        return "SHOW ASYNC EXECUTORS";
    }
}