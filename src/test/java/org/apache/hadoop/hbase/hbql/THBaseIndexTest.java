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

package org.apache.hadoop.hbase.hbql;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HConnection;
import org.apache.hadoop.hbase.hbql.client.HConnectionManager;
import org.apache.hadoop.hbase.hbql.client.HPreparedStatement;
import org.apache.hadoop.hbase.hbql.client.HRecord;
import org.apache.hadoop.hbase.hbql.client.HResultSet;
import org.apache.hadoop.hbase.hbql.client.HStatement;
import org.apache.hadoop.hbase.hbql.client.Util;
import org.apache.hadoop.hbase.hbql.util.TestSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

public class THBaseIndexTest extends TestSupport {

    static HConnection connection = null;

    static Random randomVal = new Random();

    @BeforeClass
    public static void beforeClass() throws HBqlException {

        connection = HConnectionManager.newConnection();

        connection.execute("CREATE TEMP MAPPING tab4 FOR TABLE table21"
                           + "("
                           + "keyval key width 15, "
                           + "f1 ("
                           + "  val1 string alias val1, "
                           + "  val2 int alias val2, "
                           + "  val3 int alias val3 "
                           + "))");

        if (!connection.tableExists("table21"))
            System.out.println(connection.execute("create table table21 (f1())"));
        else {
            System.out.println(connection.execute("delete from tab4"));
        }

        insertRecords(connection, 10);
    }

    private static void insertRecords(final HConnection connection,
                                      final int cnt) throws HBqlException {

        HPreparedStatement stmt = connection.prepareStatement("insert into tab4 " +
                                                              "(keyval, val1, val2, val3) values " +
                                                              "(:key, :val1, :val2, :val3)");

        for (int i = 0; i < cnt; i++) {

            int val = 10 + i;

            final String keyval = Util.getZeroPaddedNonNegativeNumber(i, 15);

            stmt.setParameter("key", keyval);
            stmt.setParameter("val1", Util.getZeroPaddedNonNegativeNumber(val * 100, 15));
            stmt.setParameter("val2", val);
            stmt.setParameter("val3", randomVal.nextInt());
            stmt.execute();
        }
    }

    private int showValues(final String sql, final boolean expectException) {

        Exception ex = null;
        try {
            HStatement stmt = connection.createStatement();
            HResultSet<HRecord> results = stmt.executeQuery(sql);

            int rec_cnt = 0;
            System.out.println("Results:");

            for (HRecord rec : results) {

                String keyval = (String)rec.getCurrentValue("keyval");
                String val1 = (String)rec.getCurrentValue("val1");
                int val2 = (Integer)rec.getCurrentValue("f1:val2");
                int val3 = (Integer)rec.getCurrentValue("f1:val3");

                System.out.println("Current Values: " + keyval + " : " + val1 + " : " + val2 + " : " + val3);
                rec_cnt++;
            }

            return rec_cnt;
        }
        catch (HBqlException e) {
            e.printStackTrace();
            ex = e;
        }

        if (expectException)
            assertTrue(ex != null);

        return 0;
    }

    @Test
    public void nonindexSelect1() {
        // Use a key with a bad width
        final String q1 = "select * from tab4 WITH KEY '00000000000001'";
        final int rec_cnt = showValues(q1, true);
        assertTrue(rec_cnt == 0);
    }

    @Test
    public void simpleSelect1() throws HBqlException {

        HStatement stmt = connection.createStatement();
        // stmt.execute("DROP INDEX foo1 ON MAPPING tab4 if indexexistsfortable('table21', 'foo1')");
        stmt.execute("CREATE INDEX foo1 ON MAPPING tab4 (f1:val1) if not indexexistsformapping('foo1', 'tab4')");

        final String q1 = "select * from tab4";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 10);
    }

    @Test
    public void simpleSelect2() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 10);
    }

    @Test
    public void simpleSelect3() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEYS ALL";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 10);
    }

    @Test
    public void simpleSelect3b() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEYS ALL LIMIT 4";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 4);
    }

    @Test
    public void simpleSelect4() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEYS '000000000001200' TO '000000000001600'";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 5);
    }

    @Test
    public void simpleSelect5() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEYS FIRST TO '000000000001400'";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 5);
    }

    @Test
    public void simpleSelect6() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEYS '000000000001500' TO LAST";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 5);
    }

    @Test
    public void simpleSelect7() throws HBqlException {
        final String q1 = "select * from tab4 WITH INDEX foo1 KEY '000000000001500' ";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 1);
    }

    @Test
    public void simpleSelect8() throws HBqlException {
        final String q1 = "select * from tab4 WITH " +
                          "INDEX foo1 " +
                          "KEYS '000000000001500' TO LAST " +
                          "INDEX FILTER WHERE val1 = '000000000001700' " +
                          "CLIENT FILTER WHERE val1 = '000000000001700'";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 1);
    }

    @Test
    public void simpleSelect9() throws HBqlException {
        final String q1 = "select * from tab4 WITH " +
                          "INDEX foo1 " +
                          "KEYS '000000000001500', '000000000001600', '000000000001700' " +
                          "INDEX FILTER WHERE val1 = '000000000001700' " +
                          "CLIENT FILTER WHERE val1 = '000000000001700'";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 1);
    }

    @Test
    public void simpleSelect10() throws HBqlException {
        final String q1 = "select * from tab4 WITH " +
                          "INDEX foo1 " +
                          "KEYS '000000000001500', '000000000001600', '000000000001700' ";
        final int rec_cnt = showValues(q1, false);
        assertTrue(rec_cnt == 3);
    }
}