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

package org.apache.expreval.expr.ifthenstmt;

import org.apache.expreval.client.NullColumnValueException;
import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.DelegateStmt;
import org.apache.expreval.expr.ExpressionType;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

public abstract class GenericIfThen extends DelegateStmt<GenericIfThen> {

    protected GenericIfThen(final ExpressionType type,
                            final GenericValue arg0,
                            final GenericValue arg1,
                            final GenericValue arg2) {
        super(type, arg0, arg1, arg2);
    }

    public Object getValue(final HConnectionImpl conn, final Object object) throws HBqlException,
                                                                                   ResultMissingColumnException,
                                                                                   NullColumnValueException {
        if ((Boolean)this.getExprArg(0).getValue(conn, object))
            return this.getExprArg(1).getValue(conn, object);
        else
            return this.getExprArg(2).getValue(conn, object);
    }

    public String asString() {
        return "IF " + this.getExprArg(0).asString() + " THEN " + this.getExprArg(1).asString()
               + " ELSE " + this.getExprArg(2).asString() + " END";
    }
}
