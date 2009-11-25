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

import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

public class ParseStatement extends BasicStatement implements NonConnectionStatement {

    private final HBqlStatement stmt;
    private final GenericValue value;

    public ParseStatement(final HBqlStatement stmt) {
        super(null);
        this.stmt = stmt;
        this.value = null;
    }

    public ParseStatement(final GenericValue value) {
        super(null);
        this.stmt = null;
        this.value = value;
    }

    private HBqlStatement getStmt() {
        return this.stmt;
    }

    private GenericValue getGenericValue() {
        return this.value;
    }

    public ExecutionResults execute() throws HBqlException {

        final ExecutionResults retval = new ExecutionResults("Parsed successfully");

        if (this.getStmt() != null)
            retval.out.println(this.getStmt().getClass().getSimpleName());

        if (this.getGenericValue() != null) {
            Object val = null;
            try {
                this.getGenericValue().validateTypes(null, false);
                val = this.getGenericValue().getValue(null);
            }
            catch (ResultMissingColumnException e) {
                val = "ResultMissingColumnException()";
            }
            retval.out.println(this.getGenericValue().asString() + " = " + val);
        }

        return retval;
    }
}