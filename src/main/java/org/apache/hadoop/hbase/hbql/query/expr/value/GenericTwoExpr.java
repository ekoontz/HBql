package org.apache.hadoop.hbase.hbql.query.expr.value;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 10, 2009
 * Time: 11:33:09 AM
 */
public class GenericTwoExpr {

    private GenericValue expr1 = null, expr2 = null;

    public GenericTwoExpr(final GenericValue expr1, final GenericValue expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    protected GenericValue getExpr1() {
        return this.expr1;
    }

    protected void setExpr1(final GenericValue expr1) {
        this.expr1 = expr1;
    }

    protected GenericValue getExpr2() {
        return this.expr2;
    }

    protected void setExpr2(final GenericValue expr2) {
        this.expr2 = expr2;
    }

    public boolean isAConstant() throws HBqlException {
        return (this.getExpr1().isAConstant() && this.getExpr2().isAConstant());
    }

    public void setContext(final ExprTree context) {
        this.getExpr1().setContext(context);
        this.getExpr2().setContext(context);
    }
}