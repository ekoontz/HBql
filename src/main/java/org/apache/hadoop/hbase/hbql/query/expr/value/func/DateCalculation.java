package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.node.DateValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.DateConstant;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 7, 2009
 * Time: 9:51:01 PM
 */
public class DateCalculation extends GenericCalculation implements DateValue {

    public DateCalculation(final GenericValue expr1, final Operator operator, final GenericValue expr2) {
        super(expr1, operator, expr2);
    }

    @Override
    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {
        return this.validateType(DateValue.class);
    }

    @Override
    public GenericValue getOptimizedValue() throws HBqlException {

        this.setExpr1(this.getExpr1().getOptimizedValue());
        this.setExpr2(this.getExpr2().getOptimizedValue());

        return this.isAConstant() ? new DateConstant(this.getValue(null)) : this;
    }

    @Override
    public Long getValue(final Object object) throws HBqlException {

        final long val1 = (Long)this.getExpr1().getValue(object);
        final long val2 = (Long)this.getExpr2().getValue(object);

        switch (this.getOperator()) {
            case PLUS:
                return val1 + val2;
            case MINUS:
                return val1 - val2;
        }

        throw new HBqlException("Invalid operator:" + this.getOperator());
    }

}