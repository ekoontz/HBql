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

package org.apache.hadoop.hbase.hbql.statement.args;

import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.var.NamedParameter;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.statement.InsertStatement;

import java.util.List;

public abstract class InsertValueSource {

    private InsertStatement insertStatement = null;

    public void setInsertStatement(final InsertStatement insertStatement) {
        this.insertStatement = insertStatement;
    }

    protected InsertStatement getInsertStatement() {
        return this.insertStatement;
    }

    public abstract List<NamedParameter> getParameterList();

    public abstract int setInsertSourceParameter(String name, Object val) throws HBqlException;

    public abstract void validate() throws HBqlException;

    public abstract void reset();

    public abstract String asString();

    public abstract Object getValue(final HConnectionImpl conn, int i) throws HBqlException;

    public abstract boolean isDefaultValue(int i) throws HBqlException;

    public abstract boolean hasValues();

    public abstract void execute() throws HBqlException;

    public abstract List<Class<? extends GenericValue>> getValuesTypeList() throws HBqlException;
}
