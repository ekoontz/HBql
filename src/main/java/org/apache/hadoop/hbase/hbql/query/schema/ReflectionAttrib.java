package org.apache.hadoop.hbase.hbql.query.schema;

import org.apache.hadoop.hbase.hbql.client.HBqlException;

import java.lang.reflect.Field;

public class ReflectionAttrib extends FieldAttrib {

    public ReflectionAttrib(final Field field) throws HBqlException {
        super(null, null, field, FieldType.getFieldType(field.getType()), false, false, null, null);

        this.defineAccessors();
    }

    final String getVariableName() {
        return this.getColumnName();
    }
}