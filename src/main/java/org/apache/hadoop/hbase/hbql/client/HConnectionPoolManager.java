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

package org.apache.hadoop.hbase.hbql.client;

import org.apache.expreval.util.Maps;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.hbql.impl.HConnectionPoolImpl;

import java.util.Map;

public class HConnectionPoolManager {

    private static Map<String, HConnectionPool> connectionPoolMap = Maps.newConcurrentHashMap();

    private static int maxPoolReferencesPerTablePerConnection = Integer.MAX_VALUE;

    public static HConnectionPool newConnectionPool(final int initConnectionPoolSize,
                                                    final int maxConnectionPoolSize) throws HBqlException {
        return HConnectionPoolManager.newConnectionPool(initConnectionPoolSize,
                                                        maxConnectionPoolSize,
                                                        null,
                                                        null);
    }

    public static HConnectionPool newConnectionPool(final int initConnectionPoolSize,
                                                    final int maxConnectionPoolSize,
                                                    final HBaseConfiguration config) throws HBqlException {
        return HConnectionPoolManager.newConnectionPool(initConnectionPoolSize,
                                                        maxConnectionPoolSize,
                                                        null,
                                                        config);
    }

    public static HConnectionPool newConnectionPool(final int initConnectionPoolSize,
                                                    final int maxConnectionPoolSize,
                                                    final String connectionPoolName) throws HBqlException {
        return HConnectionPoolManager.newConnectionPool(initConnectionPoolSize,
                                                        maxConnectionPoolSize,
                                                        connectionPoolName,
                                                        null);
    }

    public static HConnectionPool newConnectionPool(final int initConnectionPoolSize,
                                                    final int maxConnectionPoolSize,
                                                    final String connectiionPoolName,
                                                    final HBaseConfiguration config) throws HBqlException {
        final HConnectionPoolImpl connectionPool = new HConnectionPoolImpl(initConnectionPoolSize,
                                                                           maxConnectionPoolSize,
                                                                           connectiionPoolName,
                                                                           config,
                                                                           getMaxPoolReferencesPerTablePerConnection());

        if (connectionPool.getName() != null)
            HConnectionPoolManager.getConnectionPoolMap().put(connectionPool.getName(), connectionPool);

        return connectionPool;
    }


    public static int getMaxPoolReferencesPerTablePerConnection() {
        return maxPoolReferencesPerTablePerConnection;
    }

    public static void setMaxPoolReferencesPerTablePerConnection(final int maxPoolReferencesPerTablePerConnection) {
        HConnectionPoolManager.maxPoolReferencesPerTablePerConnection = maxPoolReferencesPerTablePerConnection;
    }

    public static HConnectionPool getConnectionPool(final String name) {
        return HConnectionPoolManager.getConnectionPoolMap().get(name);
    }

    private static Map<String, HConnectionPool> getConnectionPoolMap() {
        return HConnectionPoolManager.connectionPoolMap;
    }
}