package com.imap4j.hbase;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 19, 2009
 * Time: 4:39:06 PM
 */
@Table(name = "blogposts")
public class TestObject implements Persistable {

    final String family1 = "post";
    final String family2 = "image";

    final String keyval;

    @Column(family = family1)
    int intValue = -999;

    @Column(family = family1)
    String title = "A title value";

    @Column(family = family1, column = "author")
    String author = "An author value";

    @Column(family = family2, lookup = "getHeaderBytes")
    String header = "A header value";

    @Column(family = family2, column = "bodyimage")
    String bodyimage = "A bodyimage value";

    @Column(family = family2)
    int[] array1 = {1, 2, 3};

    @Column(family = family2)
    String[] array2 = {"val1", "val2", "val3"};

    @Column(family = family2, mapKeysAsColumns = true)
    Map<String, String> mapval1 = Maps.newHashMap();

    public TestObject() {
        this.keyval = "Val-" + System.nanoTime();

        mapval1.put("key1", "val1");
        mapval1.put("key2", "val2");
    }

    @Override
    public byte[] getKeyValue() {
        return keyval.getBytes();
    }

    public byte[] getHeaderBytes() {
        return this.header.getBytes();
    }

    public static void main(String[] args) throws IOException, PersistException {

        HBaseTransaction tx = new HBaseTransaction();

        int cnt = 10;
        for (int i = 0; i < cnt; i++) {
            TestObject obj = new TestObject();
            tx.insert(obj);
        }

        tx.commit();

        HBaseQuery q = new HBaseQuery("select intValue from TestObjects");

    }
}
