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

import org.apache.expreval.client.InternalErrorException;
import org.apache.expreval.client.NullColumnValueException;
import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.ArgumentListTypeSignature;
import org.apache.expreval.expr.MultipleExpressionContext;
import org.apache.expreval.expr.node.BooleanValue;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.mapping.MappingContext;

public class StatementPredicate extends MultipleExpressionContext {

    private static final ArgumentListTypeSignature typesig = new ArgumentListTypeSignature(BooleanValue.class);

    public StatementPredicate(final GenericValue... exprs) {
        super(typesig, exprs);
    }

    public boolean useResultData() {
        return false;
    }

    private void validate() throws HBqlException {
        this.setMappingContext(new MappingContext());
        this.validateTypes(this.allowColumns(), false);
    }

    public boolean evaluate(final HConnectionImpl conn) throws HBqlException {

        this.validate();

        try {
            return (Boolean)this.evaluate(conn, 0, this.allowColumns(), false, conn);
        }
        catch (ResultMissingColumnException e) {
            throw new InternalErrorException("Missing column: " + e.getMessage());
        }
        catch (NullColumnValueException e) {
            throw new InternalErrorException("Null value: " + e.getMessage());
        }
    }

    public String asString() {
        return "[ " + this.getGenericValue(0).asString() + " ]";
    }

    public boolean allowColumns() {
        return false;
    }
}