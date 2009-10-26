package org.apache.hadoop.hbase.hbql.stmt.expr.betweenstmt;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.stmt.expr.GenericExpr;
import org.apache.hadoop.hbase.hbql.stmt.expr.node.GenericValue;

public class DateBetweenStmt extends GenericBetweenStmt {

    public DateBetweenStmt(final GenericValue expr, final boolean not, final GenericValue lower, final GenericValue upper) {
        super(GenericExpr.Type.DATEBETWEEN, not, expr, lower, upper);
    }

    public Boolean getValue(final Object object) throws HBqlException, ResultMissingColumnException {

        final long dateval = (Long)this.getArg(0).getValue(object);
        final boolean retval = dateval >= (Long)this.getArg(1).getValue(object)
                               && dateval <= (Long)this.getArg(2).getValue(object);

        return (this.isNot()) ? !retval : retval;
    }
}