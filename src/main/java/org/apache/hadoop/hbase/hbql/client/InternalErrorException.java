package org.apache.hadoop.hbase.hbql.client;

public class InternalErrorException extends HBqlException {

    public InternalErrorException() {
        super("Internal error");
    }

    public InternalErrorException(final String s) {
        super("Internal error: " + s);
    }
}
