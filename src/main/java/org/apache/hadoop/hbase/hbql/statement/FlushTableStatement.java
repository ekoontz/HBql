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

import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

import java.io.IOException;

public class FlushTableStatement extends TableStatement {

    public FlushTableStatement(final StatementPredicate predicate, final String tableName) {
        super(predicate, tableName);
    }

    protected ExecutionResults execute(final HConnectionImpl conn) throws HBqlException {

        try {
            conn.getHBaseAdmin().flush(this.getTableName());
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }

        return new ExecutionResults("Table " + this.getTableName() + " flushed.");
    }

    public static String usage() {
        return "FLUSH TABLE table_name [IF bool_expr]";
    }
}