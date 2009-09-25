package org.apache.hadoop.hbase.hbql.query.expr.predicate;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.GenericOneExprExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 8:28:06 PM
 */
public class BooleanNot extends GenericOneExprExpr implements BooleanValue {

    private final boolean not;

    public BooleanNot(final boolean not, final BooleanValue expr) {
        super(expr);
        this.not = not;
    }

    public Class<? extends ValueExpr> validateType() throws HBqlException {

        final Class<? extends ValueExpr> type = this.getExpr().validateType();

        if (!HUtil.isParentClass(BooleanValue.class, type))
            throw new HBqlException("Invalid type " + type.getName() + " in CondFactor.validateType()");

        return BooleanValue.class;
    }

    @Override
    public ValueExpr getOptimizedValue() throws HBqlException {
        this.setExpr(this.getExpr().getOptimizedValue());
        return this.isAConstant() ? new BooleanLiteral(this.getValue(null)) : this;
    }

    @Override
    public Boolean getValue(final Object object) throws HBqlException {
        final boolean retval = (Boolean)this.getExpr().getValue(object);
        return (this.not) ? !retval : retval;

    }
}