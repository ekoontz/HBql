/*
 * Copyright (c) 2010.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql.statement.args;

import org.apache.expreval.expr.var.NamedParameter;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.mapping.MappingContext;
import org.apache.hadoop.hbase.hbql.util.Lists;

import java.util.List;

public class KeyRangeArgs {

    private final List<KeyRange> keyRangeList;
    private final List<NamedParameter> namedParamList = Lists.newArrayList();

    public KeyRangeArgs() {
        this(null);
    }

    public KeyRangeArgs(final List<KeyRange> keyRangeList) {
        this.keyRangeList = (keyRangeList == null) ? Lists.newArrayList(KeyRange.newAllRange()) : keyRangeList;
    }

    public List<KeyRange> getKeyRangeList() {
        return this.keyRangeList;
    }

    public void setMappingContext(final MappingContext mappingContext) throws HBqlException {
        for (final KeyRange keyRange : this.getKeyRangeList())
            keyRange.setMappingContext(mappingContext);

        for (final KeyRange keyRange : this.getKeyRangeList())
            this.getParameterList().addAll(keyRange.getParameterList());
    }

    public void validate() throws HBqlException {
        for (final KeyRange keyRange : this.getKeyRangeList())
            keyRange.validate();
    }

    public String asString() {
        final StringBuilder sbuf = new StringBuilder("KEYS ");
        boolean first = true;
        for (final KeyRange keyRange : this.getKeyRangeList()) {
            if (!first)
                sbuf.append(", ");
            sbuf.append(keyRange.asString());
            first = false;
        }
        return sbuf.toString();
    }

    public List<NamedParameter> getParameterList() {
        return this.namedParamList;
    }

    public void reset() {
        for (final KeyRange keyRange : this.getKeyRangeList())
            keyRange.reset();
    }

    public int setParameter(final String name, final Object val) throws HBqlException {
        int cnt = 0;
        for (final KeyRange keyRange : this.getKeyRangeList())
            cnt += keyRange.setParameter(name, val);
        return cnt;
    }
}
