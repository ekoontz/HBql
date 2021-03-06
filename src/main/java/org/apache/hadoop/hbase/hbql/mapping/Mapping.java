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

package org.apache.hadoop.hbase.hbql.mapping;

import org.apache.expreval.expr.ExpressionTree;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.util.AtomicReferences;
import org.apache.hadoop.hbase.hbql.util.Lists;
import org.apache.hadoop.hbase.hbql.util.Maps;
import org.apache.hadoop.hbase.hbql.util.Sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Mapping implements Serializable {

    private static final long serialVersionUID = 3L;

    private final transient AtomicReference<Map<String, ExpressionTree>> atomicEvalMap = AtomicReferences.newAtomicReference();
    private final Map<String, ColumnAttrib> columnAttribByVariableNameMap = Maps.newHashMap();
    private final Set<ColumnAttrib> columnAttribSet = Sets.newHashSet();

    private ColumnAttrib keyAttrib = null;
    private List<String> evalList = null;
    private int expressionTreeCacheSize = 25;
    private String mappingName = null;
    private String tableName = null;


    // For serialization
    public Mapping() {
    }

    protected Mapping(final String mappingName, final String tableName) {
        this.mappingName = mappingName;
        this.tableName = tableName;
    }

    public abstract Collection<String> getMappingFamilyNames() throws HBqlException;

    public Set<ColumnAttrib> getColumnAttribSet() {
        return this.columnAttribSet;
    }

    // *** columnAttribByVariableNameMap calls
    private Map<String, ColumnAttrib> getColumnAttribByVariableNameMap() {
        return this.columnAttribByVariableNameMap;
    }

    public boolean containsVariableName(final String varname) {
        return this.getColumnAttribByVariableNameMap().containsKey(varname);
    }

    public ColumnAttrib getAttribByVariableName(final String name) {
        return this.getColumnAttribByVariableNameMap().get(name);
    }

    public void resetDefaultValues() throws HBqlException {
        for (final ColumnAttrib attrib : this.getColumnAttribSet())
            attrib.resetDefaultValue();
    }

    protected void addAttribToVariableNameMap(final ColumnAttrib attrib,
                                              final String... attribNames) throws HBqlException {

        this.getColumnAttribSet().add(attrib);

        for (final String attribName : attribNames) {
            if (this.getColumnAttribByVariableNameMap().containsKey(attribName))
                throw new HBqlException(attribName + " already declared");

            this.getColumnAttribByVariableNameMap().put(attribName, attrib);
        }
    }

    private AtomicReference<Map<String, ExpressionTree>> getAtomicEvalMap() {
        return this.atomicEvalMap;
    }

    public Map<String, ExpressionTree> getEvalMap() {

        if (this.getAtomicEvalMap().get() == null) {
            synchronized (this) {
                if (this.getAtomicEvalMap().get() == null) {
                    final Map<String, ExpressionTree> val = Maps.newHashMap();
                    this.getAtomicEvalMap().set(val);
                    this.evalList = Lists.newArrayList();
                }
            }
        }
        return this.getAtomicEvalMap().get();
    }

    public String getMappingName() {
        return this.mappingName;
    }

    private List<String> getEvalList() {
        return this.evalList;
    }

    public String toString() {
        return this.getMappingName();
    }

    public int getEvalCacheSize() {
        return this.expressionTreeCacheSize;
    }

    public ColumnAttrib getKeyAttrib() {
        return this.keyAttrib;
    }

    protected void setKeyAttrib(final ColumnAttrib keyAttrib) {
        this.keyAttrib = keyAttrib;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setEvalCacheSize(final int size) {

        if (size > 0) {
            this.expressionTreeCacheSize = size;

            // Reset existing cache
            final Map<String, ExpressionTree> map = this.getEvalMap();
            final List<String> list = this.getEvalList();
            map.clear();
            list.clear();
        }
    }

    public synchronized void addToExpressionTreeCache(final String exprStr, final ExpressionTree expressionTree) {

        final Map<String, ExpressionTree> map = this.getEvalMap();

        if (!map.containsKey(exprStr)) {

            final List<String> list = this.getEvalList();

            list.add(exprStr);
            map.put(exprStr, expressionTree);

            if (list.size() > this.getEvalCacheSize()) {
                final String firstOne = list.get(0);
                map.remove(firstOne);
                list.remove(0);
            }
        }
    }
}
