package com.imap4j.hbase.hbql.io;

import com.google.common.collect.Maps;
import com.imap4j.hbase.hbql.HPersistException;
import com.imap4j.hbase.hbql.HPersistable;
import com.imap4j.hbase.hbql.schema.ClassSchema;
import com.imap4j.hbase.hbql.schema.ColumnAttrib;
import com.imap4j.hbase.hbql.schema.FieldType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 31, 2009
 * Time: 3:55:02 PM
 */
public abstract class Serialization {

    public enum TYPE {
        JAVA, HADOOP
    }

    private final static Serialization java = new JavaSerialization();
    private final static Serialization hadoop = new HadoopSerialization();

    public static Serialization getSerializationStrategy(final TYPE type) {

        switch (type) {
            case JAVA:
                return java;
            case HADOOP:
                return hadoop;
        }

        return null;
    }

    abstract public Object getScalarFromBytes(FieldType fieldType, byte[] b) throws IOException, HPersistException;

    abstract public byte[] getScalarAsBytes(FieldType fieldType, Object obj) throws IOException, HPersistException;

    abstract public Object getArrayFromBytes(FieldType fieldType, Class clazz, byte[] b) throws IOException, HPersistException;

    abstract public byte[] getArrayasBytes(FieldType fieldType, Object obj) throws IOException, HPersistException;

    public byte[] getStringAsBytes(final String obj) throws IOException, HPersistException {
        return this.getScalarAsBytes(FieldType.StringType, obj);
    }

    public byte[] getObjectAsBytes(final Object obj) throws IOException, HPersistException {
        return this.getScalarAsBytes(FieldType.getFieldType(obj), obj);
    }

    public String getStringFromBytes(final byte[] b) throws IOException, HPersistException {
        return (String)this.getScalarFromBytes(FieldType.StringType, b);
    }

    public Object getObjectFromBytes(final FieldType type, final byte[] b) throws IOException, HPersistException {
        return this.getScalarFromBytes(type, b);
    }

    public HPersistable getHPersistable(final ClassSchema classSchema,
                                        final Scan scan,
                                        final Result result) throws HPersistException {

        try {
            // Create object and assign key value
            final HPersistable newobj = this.createNewObject(classSchema, result);

            // Assign most recent values
            this.assignCurrentValues(classSchema, result, newobj);

            // Assign the versioned values
            if (scan.getMaxVersions() > 1)
                this.assignVersionedValues(classSchema, result, newobj);

            return newobj;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new HPersistException("Error in getHPersistable()");
        }
    }

    private HPersistable createNewObject(final ClassSchema classSchema, final Result result) throws IOException, HPersistException {

        // Create new instance and set key value
        final ColumnAttrib keyattrib = classSchema.getKeyColumnAttrib();
        final HPersistable newobj;
        try {
            newobj = (HPersistable)classSchema.getClazz().newInstance();
            final byte[] keybytes = result.getRow();
            keyattrib.setValue(this, newobj, keybytes);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Cannot create new instance of " + classSchema.getClazz());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set value for key  " + keyattrib.getVariableName()
                                       + " for " + classSchema.getClazz());
        }
        return newobj;
    }

    private void assignCurrentValues(final ClassSchema classSchema,
                                     final Result result,
                                     final HPersistable newobj) throws IOException, HPersistException {

        for (final KeyValue keyValue : result.list()) {

            final byte[] cbytes = keyValue.getColumn();
            final byte[] vbytes = result.getValue(cbytes);
            final String colname = this.getStringFromBytes(cbytes);

            if (colname.endsWith("]")) {
                final int lbrace = colname.indexOf("[");
                final String mapcolumn = colname.substring(0, lbrace);
                final String mapKey = colname.substring(lbrace + 1, colname.length() - 1);
                final ColumnAttrib attrib = classSchema.getColumnAttribByFamilyQualifiedColumnName(mapcolumn);
                final Object val = attrib.getValueFromBytes(this, newobj, vbytes);

                Map mapval = (Map)attrib.getValue(newobj);

                // TODO it is probably not kosher to create a map here
                if (mapval == null) {
                    mapval = Maps.newHashMap();
                    attrib.setValue(newobj, mapval);
                }

                mapval.put(mapKey, val);
            }
            else {
                final ColumnAttrib attrib = classSchema.getColumnAttribByFamilyQualifiedColumnName(colname);
                attrib.setValue(this, newobj, vbytes);
            }
        }
    }

    private void assignVersionedValues(final ClassSchema classSchema,
                                       final Result result,
                                       final HPersistable newobj) throws IOException, HPersistException {

        final NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> familyMap = result.getMap();

        for (final byte[] fbytes : familyMap.keySet()) {

            final String famname = this.getStringFromBytes(fbytes) + ":";
            final NavigableMap<byte[], NavigableMap<Long, byte[]>> columnMap = familyMap.get(fbytes);

            for (final byte[] cbytes : columnMap.keySet()) {
                final String colname = this.getStringFromBytes(cbytes);
                final String qualifiedName = famname + colname;
                final NavigableMap<Long, byte[]> tsMap = columnMap.get(cbytes);

                for (final Long ts : tsMap.keySet()) {
                    final byte[] vbytes = tsMap.get(ts);

                    //  final VersionAttrib attrib = classSchema.getVersionAttribByFamilyQualifiedColumnName(qualifiedName);
                    //  attrib.setValue(this, newobj, vbytes);

                }
            }
        }
    }

    public boolean isSerializable(final Object obj) {

        try {
            final byte[] b = getObjectAsBytes(obj);
            final Object newobj = getObjectFromBytes(FieldType.getFieldType(obj), b);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        catch (HPersistException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}