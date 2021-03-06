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
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

import java.io.IOException;

public class TimestampArgs extends SelectStatementArgs {

    final boolean aSingleValue;

    public TimestampArgs(final GenericValue arg0) {
        super(ArgType.TIMESTAMPRANGE, arg0, arg0);
        this.aSingleValue = true;
    }

    public TimestampArgs(final GenericValue arg0, final GenericValue arg1) {
        super(ArgType.TIMESTAMPRANGE, arg0, arg1);
        this.aSingleValue = false;
    }

    private long getLower() throws HBqlException {
        return (Long)this.evaluateConstant(0, false);
    }

    private long getUpper() throws HBqlException {
        return (Long)this.evaluateConstant(1, false);
    }

    private boolean isASingleValue() {
        return this.aSingleValue;
    }

    public String asString() {
        if (this.isASingleValue())
            return "TIMESTAMP " + this.getGenericValue(0).asString();
        else
            return "TIMESTAMP RANGE " + this.getGenericValue(0).asString() + " TO "
                   + this.getGenericValue(1).asString();
    }

    public void setTimeStamp(final Get get) throws HBqlException {
        try {
            if (this.isASingleValue())
                get.setTimeStamp(this.getLower());
            else
                get.setTimeRange(this.getLower(), this.getUpper());
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }
    }

    public void setTimeStamp(final Scan scan) throws HBqlException {
        try {
            if (this.isASingleValue())
                scan.setTimeStamp(this.getLower());
            else
                scan.setTimeRange(this.getLower(), this.getUpper());
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }
    }
}