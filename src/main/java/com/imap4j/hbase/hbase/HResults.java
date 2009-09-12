package com.imap4j.hbase.hbase;

import com.imap4j.hbase.antlr.args.QueryArgs;
import com.imap4j.hbase.antlr.config.HBqlRule;
import com.imap4j.hbase.hbql.expr.ExprTree;
import com.imap4j.hbase.hbql.schema.AnnotationSchema;
import com.imap4j.hbase.hbql.schema.ExprSchema;
import com.imap4j.hbase.hbql.schema.HUtil;
import com.imap4j.hbase.util.Lists;
import com.imap4j.hbase.util.ResultsIterator;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 12, 2009
 * Time: 2:08:38 PM
 */
public class HResults<T extends HPersistable> implements Iterable<T> {

    final List<ResultScanner> scannerList = Lists.newArrayList();
    final HQuery hquery;

    public HResults(final HQuery hquery) {
        this.hquery = hquery;
    }

    private HQuery getHQuery() {
        return hquery;
    }

    private List<ResultScanner> getScannerList() {
        return scannerList;
    }

    public void close() {

        for (final ResultScanner scanner : this.getScannerList())
            closeCurrentScanner(scanner, false);

        this.scannerList.clear();
    }

    private void closeCurrentScanner(final ResultScanner scanner, final boolean removeFromList) {
        if (scanner == null)
            return;

        try {
            scanner.close();
        }
        catch (Exception e) {
            // Do nothing
        }

        if (removeFromList)
            getScannerList().remove(scanner);
    }

    @Override
    public Iterator<T> iterator() {

        try {
            return new ResultsIterator<T>() {

                final QueryArgs args = (QueryArgs)HBqlRule.SELECT.parse(getHQuery().getQuery(), (ExprSchema)null);
                final AnnotationSchema schema = AnnotationSchema.getAnnotationSchema(args.getTableName());
                final List<String> fieldList = (args.getColumns() == null) ? schema.getFieldList() : args.getColumns();

                final ExprTree clientExprTree = getHQuery().getExprTree(args.getWhereExpr().getClientFilter(),
                                                                        schema,
                                                                        fieldList);

                final List<Scan> scanList = HUtil.getScanList(this.schema,
                                                              this.fieldList,
                                                              this.args.getWhereExpr().getKeyRange(),
                                                              this.args.getWhereExpr().getVersion(),
                                                              getHQuery().getExprTree(args.getWhereExpr().getServerFilter(),
                                                                                      this.schema,
                                                                                      this.fieldList));

                final HTable table = getHQuery().getConnection().getHTable(schema.getTableName());
                final Iterator<Scan> scanIter = scanList.iterator();
                int maxVersions = 0;
                ResultScanner currentResultScanner = null;
                Iterator<Result> resultIter = null;

                // Prime the iterator with the first value
                T nextObject = fetchNextObject();

                private Iterator<Result> getNextResultScanner() throws IOException {
                    if (scanIter.hasNext()) {

                        final Scan scan = scanIter.next();
                        maxVersions = scan.getMaxVersions();

                        // First close previous ResultScanner before reassigning
                        closeCurrentScanner(currentResultScanner, true);

                        currentResultScanner = table.getScanner(scan);
                        getScannerList().add(currentResultScanner);

                        return currentResultScanner.iterator();
                    }
                    else {
                        return null;
                    }
                }

                protected T fetchNextObject() throws HPersistException, IOException {

                    T val = doFetch();

                    if (val != null)
                        return val;

                    // Try one more time
                    val = doFetch();
                    if (val == null)
                        closeCurrentScanner(currentResultScanner, true);
                    return val;
                }

                private T doFetch() throws HPersistException, IOException {

                    if (resultIter == null)
                        resultIter = getNextResultScanner();

                    if (resultIter != null) {
                        while (resultIter.hasNext()) {
                            final Result result = resultIter.next();
                            final T val = (T)HUtil.getHPersistable(HUtil.ser, schema, fieldList, maxVersions, result);
                            if (this.clientExprTree == null || this.clientExprTree.evaluate(val))
                                return val;

                        }
                    }

                    // Reset to get next scanner
                    resultIter = null;
                    return null;
                }

                protected T getNextObject() {
                    return this.nextObject;
                }

                protected void setNextObject(final T nextObject) {
                    this.nextObject = nextObject;
                }
            };
        }
        catch (HPersistException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return null;
            }

            @Override
            public void remove() {

            }
        };
    }

}