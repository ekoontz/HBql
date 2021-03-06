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

package org.apache.hadoop.hbase.hbql.impl;

import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

public class ResultScannerExecutor extends CompletionQueueExecutor<ResultScanner> {

    public ResultScannerExecutor(final QueryExecutorPoolImpl executorPool,
                                 final int minThreadCount,
                                 final int maxThreadCount,
                                 final long keepAliveSecs,
                                 final int completionQueueSize) throws HBqlException {
        super(executorPool, minThreadCount, maxThreadCount, keepAliveSecs, completionQueueSize);
    }

    public static ResultScannerExecutor newPooledResultScannerExecutor(final QueryExecutorPoolImpl executorPool,
                                                                       final int minThreadCount,
                                                                       final int maxThreadCount,
                                                                       final long keepAliveSecs,
                                                                       final int completionQueueSize) throws HBqlException {
        return new ResultScannerExecutor(executorPool, minThreadCount, maxThreadCount, keepAliveSecs, completionQueueSize);
    }

    public static ResultScannerExecutor newResultScannerExecutor(final int minThreadCount,
                                                                 final int maxThreadCount,
                                                                 final long keepAliveSecs,
                                                                 final int completionQueueSize) throws HBqlException {
        return new ResultScannerExecutor(null, minThreadCount, maxThreadCount, keepAliveSecs, completionQueueSize);
    }

    public boolean threadsReadResults() {
        return false;
    }
}
