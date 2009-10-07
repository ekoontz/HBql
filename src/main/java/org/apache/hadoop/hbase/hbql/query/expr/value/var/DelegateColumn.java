package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprContext;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.schema.ColumnAttrib;

public class DelegateColumn extends GenericColumn<GenericValue> {

    private GenericColumn typedColumn = null;
    private String variableName;

    public DelegateColumn(final String variableName) {
        super(null);
        this.variableName = variableName;
    }

    private GenericColumn getTypedColumn() {
        return this.typedColumn;
    }

    private void setTypedColumn(final GenericColumn typedColumn) {
        this.typedColumn = typedColumn;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public Object getValue(final Object object) throws HBqlException {
        return this.getTypedColumn().getValue(object);
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {
        return this.getTypedColumn().validateTypes(parentExpr, allowsCollections);
    }

    public void setExprContext(final ExprContext context) throws HBqlException {

        final ColumnAttrib attrib = context.getSchema().getAttribByVariableName(this.getVariableName());

        if (attrib == null)
            throw new HBqlException("Invalid variable: " + this.getVariableName());

        switch (attrib.getFieldType()) {

            case KeyType:
            case StringType:
                this.setTypedColumn(new StringColumn(attrib));
                break;

            case LongType:
                this.setTypedColumn(new LongColumn(attrib));
                break;

            case IntegerType:
                this.setTypedColumn(new IntegerColumn(attrib));
                break;

            case DateType:
                this.setTypedColumn(new DateColumn(attrib));
                break;

            case BooleanType:
                this.setTypedColumn(new BooleanColumn(attrib));
                break;

            default:
                throw new HBqlException("Invalid type: " + attrib.getFieldType().name());
        }

        this.getTypedColumn().setExprContext(context);
    }
}
