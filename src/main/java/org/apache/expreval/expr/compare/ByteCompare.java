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

package org.apache.expreval.expr.compare;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.expreval.client.NullColumnValueException;
import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.Operator;
import org.apache.expreval.expr.node.BooleanValue;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.var.DelegateColumn;
import org.apache.expreval.expr.var.GenericColumn;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.impl.Utils;
import org.apache.hadoop.hbase.hbql.io.IO;
import org.apache.hadoop.hbase.hbql.mapping.FieldType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteCompare extends GenericCompare implements BooleanValue {

    private static final Log LOG = LogFactory.getLog(ByteCompare.class);

    public ByteCompare(final GenericValue arg0, final Operator operator, final GenericValue arg1) {
        super(arg0, operator, arg1);
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowCollections) throws HBqlException {
        return this.validateType(BooleanValue.class);
    }

    public Boolean getValue(final HConnectionImpl conn, final Object object) throws HBqlException,
                                                                                    ResultMissingColumnException,
                                                                                    NullColumnValueException {
        final byte val0 = (Byte)this.getValue(0, conn, object);
        final byte val1 = (Byte)this.getValue(1, conn, object);

        switch (this.getOperator()) {
            case EQ:
                return val0 == val1;
            case GT:
                return val0 > val1;
            case GTEQ:
                return val0 >= val1;
            case LT:
                return val0 < val1;
            case LTEQ:
                return val0 <= val1;
            case NOTEQ:
                return val0 != val1;
            default:
                throw new HBqlException("Invalid operator: " + this.getOperator());
        }
    }

    public Filter getFilter() throws HBqlException {

        this.validateArgsForCompareFilter();

        final GenericColumn<? extends GenericValue> column;
        final Object constant;
        final CompareFilter.CompareOp compareOp;

        if (this.getExprArg(0).isAColumnReference()) {
            column = ((DelegateColumn)this.getExprArg(0)).getTypedColumn();
            constant = this.getConstantValue(1);
            compareOp = this.getOperator().getCompareOpLeft();
        }
        else {
            column = ((DelegateColumn)this.getExprArg(1)).getTypedColumn();
            constant = this.getConstantValue(0);
            compareOp = this.getOperator().getCompareOpRight();
        }

        return this.newSingleColumnValueFilter(column.getColumnAttrib(),
                                               compareOp,
                                               new ByteComparable((Byte)constant));
    }

    private static class ByteComparable extends GenericComparable<Byte> {

        public ByteComparable() {
        }

        public ByteComparable(final Byte value) {
            this.setValue(value);
        }

        public int compareTo(final byte[] bytes) {

            if (this.equalValues(bytes))
                return 0;

            try {
                final Byte columnValue = IO.getSerialization().getByteFromBytes(bytes);
                return (this.getValue().compareTo(columnValue));
            }
            catch (HBqlException e) {
                e.printStackTrace();
                Utils.logException(LOG, e);
                return 1;
            }
        }

        public void write(final DataOutput dataOutput) throws IOException {
            dataOutput.writeByte(this.getValue());
        }

        public void readFields(final DataInput dataInput) throws IOException {
            this.setValue(dataInput.readByte());

            this.setValueInBytes(FieldType.ByteType, this.getValue());
        }
    }
}