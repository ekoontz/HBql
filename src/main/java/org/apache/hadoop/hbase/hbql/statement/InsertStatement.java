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

import org.apache.expreval.expr.TypeSupport;
import org.apache.expreval.expr.literal.DefaultKeyword;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.util.Lists;
import org.apache.hadoop.hbase.hbql.client.Batch;
import org.apache.hadoop.hbase.hbql.client.ExecutionOutput;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.PreparedStatement;
import org.apache.hadoop.hbase.hbql.client.Record;
import org.apache.hadoop.hbase.hbql.client.SchemaManager;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.impl.ConnectionImpl;
import org.apache.hadoop.hbase.hbql.schema.ColumnAttrib;
import org.apache.hadoop.hbase.hbql.statement.args.InsertValueSource;
import org.apache.hadoop.hbase.hbql.statement.select.SingleExpressionContext;

import java.io.IOException;
import java.util.List;

public class InsertStatement extends SchemaStatement implements PreparedStatement {

    private final List<SingleExpressionContext> columnList = Lists.newArrayList();
    private final InsertValueSource valueSource;

    private ConnectionImpl connection = null;
    private Record record = null;
    private boolean validated = false;

    public InsertStatement(final String schemaName,
                           final List<GenericValue> columnList,
                           final InsertValueSource valueSource) {
        super(schemaName);

        for (final GenericValue val : columnList)
            this.getColumnList().add(SingleExpressionContext.newSingleExpression(val, null));

        this.valueSource = valueSource;
        this.getValueSource().setInsertStatement(this);
    }

    public void validate(final ConnectionImpl conn) throws HBqlException {

        if (validated)
            return;

        this.validated = true;

        this.connection = conn;
        this.record = SchemaManager.newRecord(this.getSchemaName());

        for (final SingleExpressionContext element : this.getColumnList()) {

            element.validate(this.getSchema(), this.getConnection());

            if (!element.isASimpleColumnReference())
                throw new TypeException(element.asString() + " is not a column reference in " + this.asString());
        }

        if (!this.hasAKeyValue())
            throw new TypeException("Missing a key value in attribute list in " + this.asString());

        this.getValueSource().validate();
    }

    public void validateTypes() throws HBqlException {

        final List<Class<? extends GenericValue>> columnsTypeList = this.getColumnsTypeList();
        final List<Class<? extends GenericValue>> valuesTypeList = this.getValueSource().getValuesTypeList();

        if (columnsTypeList.size() != valuesTypeList.size())
            throw new HBqlException("Number of columns not equal to number of values in " + this.asString());

        for (int i = 0; i < columnsTypeList.size(); i++) {

            final Class<? extends GenericValue> type1 = columnsTypeList.get(i);
            final Class<? extends GenericValue> type2 = valuesTypeList.get(i);

            // Skip Default values
            if (type2 == DefaultKeyword.class) {
                final String name = this.getColumnList().get(i).asString();
                final ColumnAttrib attrib = this.getSchema().getAttribByVariableName(name);
                if (!attrib.hasDefaultArg())
                    throw new HBqlException("No DEFAULT value specified for " + attrib.getNameToUseInExceptions()
                                            + " in " + this.asString());
                continue;
            }

            if (!TypeSupport.isParentClass(type1, type2))
                throw new TypeException("Type mismatch in argument " + i
                                        + " expecting " + type1.getSimpleName()
                                        + " but found " + type2.getSimpleName()
                                        + " in " + this.asString());
        }
    }

    private List<Class<? extends GenericValue>> getColumnsTypeList() throws HBqlException {
        final List<Class<? extends GenericValue>> typeList = Lists.newArrayList();
        for (final SingleExpressionContext element : this.getColumnList()) {
            final Class<? extends GenericValue> type = element.getExpressionType();
            typeList.add(type);
        }
        return typeList;
    }

    private boolean hasAKeyValue() {
        for (final SingleExpressionContext element : this.getColumnList()) {
            if (element.isAKeyValue())
                return true;
        }
        return false;
    }

    public int setParameter(final String name, final Object val) throws HBqlException {
        final int cnt = this.getValueSource().setParameter(name, val);
        if (cnt == 0)
            throw new HBqlException("Parameter name " + name + " does not exist in " + this.asString());
        return cnt;
    }

    private Record getRecord() {
        return this.record;
    }

    public ConnectionImpl getConnection() {
        return this.connection;
    }

    private List<SingleExpressionContext> getColumnList() {
        return columnList;
    }

    private InsertValueSource getValueSource() {
        return this.valueSource;
    }

    public ExecutionOutput execute(final ConnectionImpl conn) throws HBqlException, IOException {

        this.validate(conn);

        this.validateTypes();

        int cnt = 0;

        this.getValueSource().execute();

        while (this.getValueSource().hasValues()) {

            final Batch batch = new Batch();

            for (int i = 0; i < this.getColumnList().size(); i++) {
                final String name = this.getColumnList().get(i).asString();
                final Object val;
                if (this.getValueSource().isDefaultValue(i)) {
                    final ColumnAttrib attrib = this.getSchema().getAttribByVariableName(name);
                    val = attrib.getDefaultValue();
                }
                else {
                    val = this.getValueSource().getValue(i);
                }
                this.getRecord().setCurrentValue(name, val);
            }

            batch.insert(this.getRecord());

            conn.apply(batch);
            cnt++;
        }

        return new ExecutionOutput(cnt + " record" + ((cnt > 1) ? "s" : "") + " inserted");
    }

    public ExecutionOutput execute() throws HBqlException, IOException {
        return this.execute(this.getConnection());
    }

    public void reset() {
        this.getValueSource().reset();
        this.getRecord().reset();
    }

    public String asString() {

        final StringBuilder sbuf = new StringBuilder();

        sbuf.append("INSERT INTO ");
        sbuf.append(this.getSchemaName());
        sbuf.append(" (");

        boolean firstTime = true;
        for (final SingleExpressionContext val : this.getColumnList()) {
            if (!firstTime)
                sbuf.append(", ");
            firstTime = false;

            sbuf.append(val.asString());
        }

        sbuf.append(") ");
        sbuf.append(this.getValueSource().asString());
        return sbuf.toString();
    }
}