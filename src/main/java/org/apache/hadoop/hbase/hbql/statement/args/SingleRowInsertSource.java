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
import org.apache.hadoop.hbase.hbql.statement.select.SelectExpressionContext;
import org.apache.hadoop.hbase.hbql.util.Lists;

import java.util.List;

public class SingleRowInsertSource extends InsertValueSource {

    private final List<SelectExpressionContext> valueList = Lists.newArrayList();
    private boolean calledForValues = false;

    public SingleRowInsertSource(final List<GenericValue> valueList) {
        for (final GenericValue val : valueList)
            this.getValueList().add(SelectExpressionContext.newExpression(val, null));
    }

    private List<SelectExpressionContext> getValueList() {
        return this.valueList;
    }

    public List<NamedParameter> getParameterList() {

        final List<NamedParameter> parameterList = Lists.newArrayList();

        for (final SelectExpressionContext expr : this.getValueList())
            parameterList.addAll(expr.getParameterList());

        return parameterList;
    }

    public int setInsertSourceParameter(final String name, final Object val) throws HBqlException {

        int cnt = 0;

        for (final SelectExpressionContext expr : this.getValueList())
            cnt += expr.setParameter(name, val);

        return cnt;
    }

    public void validate() throws HBqlException {

        final HConnectionImpl conn = this.getInsertStatement().getConnection();

        for (final SelectExpressionContext element : this.getValueList()) {
            element.validate(this.getInsertStatement().getMappingContext(), conn);

            // Make sure values do not have column references
            if (element.hasAColumnReference())
                throw new HBqlException("Column reference " + element.asString() + " not valid in " + this.asString());
        }
    }

    public void execute() {
        // No op
    }

    public void reset() {
        this.calledForValues = false;
        for (final SelectExpressionContext expr : this.getValueList())
            expr.reset();
    }

    public String asString() {

        final StringBuilder sbuf = new StringBuilder();

        sbuf.append("VALUES (");

        boolean firstTime = true;
        for (final SelectExpressionContext val : this.getValueList()) {
            if (!firstTime)
                sbuf.append(", ");
            firstTime = false;

            sbuf.append(val.asString());
        }

        sbuf.append(")");

        return sbuf.toString();
    }

    public boolean isDefaultValue(final int i) throws HBqlException {
        return this.getValueList().get(i).isDefaultKeyword();
    }

    public Object getValue(final HConnectionImpl conn, final int i) throws HBqlException {
        return this.getValueList().get(i).evaluateConstant(0, false);
    }

    public List<Class<? extends GenericValue>> getValuesTypeList() throws HBqlException {
        final List<Class<? extends GenericValue>> typeList = Lists.newArrayList();
        for (final SelectExpressionContext element : this.getValueList()) {
            final Class<? extends GenericValue> type = element.getExpressionType();
            typeList.add(type);
        }
        return typeList;
    }

    public boolean hasValues() {
        this.calledForValues = !this.calledForValues;
        return this.calledForValues;
    }
}
