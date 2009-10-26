package org.apache.hadoop.hbase.hbql.stmt.expr.var;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.stmt.expr.node.NumberValue;
import org.apache.hadoop.hbase.hbql.stmt.schema.ColumnAttrib;

public class ByteColumn extends GenericColumn<NumberValue> implements NumberValue {

    public ByteColumn(ColumnAttrib attrib) {
        super(attrib);
    }

    public Byte getValue(final Object object) throws HBqlException, ResultMissingColumnException {
        if (this.getExprContext().useHBaseResult())
            return (Byte)this.getColumnAttrib().getValueFromBytes((Result)object);
        else
            return (Byte)this.getColumnAttrib().getCurrentValue(object);
    }
}