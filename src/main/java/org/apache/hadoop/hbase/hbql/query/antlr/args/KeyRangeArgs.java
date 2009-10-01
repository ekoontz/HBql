package org.apache.hadoop.hbase.hbql.query.antlr.args;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;
import org.apache.hadoop.hbase.hbql.query.util.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 4, 2009
 * Time: 9:53:48 AM
 */
public class KeyRangeArgs {

    private final List<Range> rangeList;

    public enum Type {
        LAST, RANGE
    }

    public static class Range {
        private final String lower;
        private final String upper;
        private final Type type;

        public Range(final String lower, final Type type) {
            this(lower, null, type);
        }

        public Range(final String lower, final String upper) {
            this(lower, upper, Type.RANGE);
        }

        private Range(final String lower, final String upper, final Type type) {
            this.lower = lower;
            this.upper = upper;
            this.type = type;
        }

        public String getLower() {
            return this.lower;
        }

        public String getUpper() {
            return this.upper;
        }

        public Type getType() {
            return this.type;
        }

        public byte[] getLowerAsBytes() throws HBqlException {
            return HUtil.ser.getStringAsBytes(this.getLower());
        }

        public byte[] getUpperAsBytes() throws HBqlException {
            return HUtil.ser.getStringAsBytes(this.getUpper());
        }

        public boolean isStartLastRange() {
            return this.getType() == Type.LAST;
        }

        public String asString() {
            final StringBuilder sbuf = new StringBuilder();
            sbuf.append("'" + this.lower + "' TO ");
            if (this.isStartLastRange())
                sbuf.append("LAST");
            else
                sbuf.append("'" + this.upper + "'");
            return sbuf.toString();
        }

    }

    public KeyRangeArgs() {
        this.rangeList = Lists.newArrayList();
    }

    public KeyRangeArgs(final List<Range> rangeList) {
        if (rangeList == null)
            this.rangeList = Lists.newArrayList();
        else
            this.rangeList = rangeList;
    }

    public boolean isValid() {
        return this.getRangeList().size() > 0;
    }

    public List<Range> getRangeList() {
        return this.rangeList;
    }

    public String asString() {
        final StringBuilder sbuf = new StringBuilder("KEYS ");
        boolean first = true;
        for (final Range range : this.getRangeList()) {
            if (!first)
                sbuf.append(", ");
            sbuf.append(range.asString());
            first = false;
        }
        return sbuf.toString();
    }
}
