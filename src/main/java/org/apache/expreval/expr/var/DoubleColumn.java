package org.apache.expreval.expr.var;

import org.apache.expreval.expr.node.NumberValue;
import org.apache.expreval.schema.ColumnAttrib;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.contrib.hbql.client.HBqlException;
import org.apache.hadoop.hbase.contrib.hbql.client.ResultMissingColumnException;

public class DoubleColumn extends GenericColumn<NumberValue> implements NumberValue {

    public DoubleColumn(ColumnAttrib attrib) {
        super(attrib);
    }

    public Double getValue(final Object object) throws HBqlException, ResultMissingColumnException {
        if (this.getExprContext().useHBaseResult())
            return (Double)this.getColumnAttrib().getValueFromBytes((Result)object);
        else
            return (Double)this.getColumnAttrib().getCurrentValue(object);
    }
}