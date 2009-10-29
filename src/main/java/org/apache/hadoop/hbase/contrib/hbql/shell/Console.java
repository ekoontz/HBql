package org.apache.hadoop.hbase.contrib.hbql.shell;

import jline.ArgumentCompletor;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import org.apache.expreval.client.HBqlException;
import org.apache.expreval.util.Lists;
import org.apache.hadoop.hbase.contrib.hbql.client.HConnection;
import org.apache.hadoop.hbase.contrib.hbql.client.HConnectionManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Console {

    public static void main(String[] args) throws IOException {

        final List<SimpleCompletor> completors = Lists.newArrayList();
        completors.add(new SimpleCompletor(new String[]{"select", "insert", "create", "table", "schema", "describe"}));

        final ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);
        reader.setUseHistory(true);
        //reader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));
        reader.addCompletor(new ArgumentCompletor(completors));

        String line;
        final PrintWriter out = new PrintWriter(System.out);

        final HConnection conn = HConnectionManager.newHConnection();

        while ((line = reader.readLine("HBql> ")) != null) {

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
                break;

            try {
                out.println(conn.execute(line));
            }
            catch (HBqlException e) {
                out.println("Error in input: " + line);
            }

            out.flush();
        }
    }
}
