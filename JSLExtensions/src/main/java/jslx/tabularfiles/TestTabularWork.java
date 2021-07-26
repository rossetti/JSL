package jslx.tabularfiles;


import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.JSL;
import jslx.dbutilities.JSLDatabase;
import jslx.dbutilities.dbutil.DatabaseIfc;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class TestTabularWork {

    public static void main(String[] args) {

        // demonstrate reading a file
        writeFile();
        // demonstrate reading a file
        readFile();
    }

    private static void writeFile() {
        Path path = JSLDatabase.dbDir.resolve("demoFile");

        // configure the columns
        LinkedHashMap<String, DataType> columns = TabularFile.columns(3, DataType.NUMERIC);
        columns.put("c4", DataType.TEXT);
        columns.put("c5", DataType.NUMERIC);

        // make the file
        TabularOutputFile tif = new TabularOutputFile(columns, path);
        System.out.println(tif);

        // needed for some random data
        NormalRV n = new NormalRV(10, 1);
        int k = 15;
        // get a row
        RowSetterIfc row = tif.getRow();
        // write some data to each row
        System.out.println("Writing rows...");
        for (int i = 1; i <= k; i++) {
            // reuse the same row, many times
            // can fill all numeric columns
            row.setNumeric(n.sample(5));
            // can set specific columns
            row.setText(3, "text data " + i);
            // need to write the row to the buffer
            tif.writeRow(row);
        }
        // don't forget to flush the buffer
        tif.flushRows();
        System.out.println("Done writing rows!");
        System.out.println();
    }

    private static void readFile(){
        Path path = JSLDatabase.dbDir.resolve("demoFile");
        TabularInputFile tif = new TabularInputFile(path);
        System.out.println(tif);

        // TabularInputFile is Iterable and foreach construct works across rows
        for(RowGetterIfc row: tif){
            System.out.println(row);
        }
        System.out.println();
        System.out.println();

        // You can fetch rows as a list
        List<RowGetterIfc> rows = tif.fetchRows(1, 5);
        for (RowGetterIfc row : rows) {
            System.out.println(row);
        }
        System.out.println();
        System.out.println();

        // You can iterate starting at any row
        TabularInputFile.RowIterator iterator = tif.iterator(9);
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }

        System.out.println();
        System.out.println();

        // You can grab various columns as arrays
        Double[] numericColumn = tif.getNumericColumn(0, 10, true);
        for (Double v : numericColumn) {
            System.out.println(v);
        }

        // You can write the data to an Excel workbook
        try {
            tif.writeToExcelWorkbook("demoData.xlsx", JSL.getInstance().getExcelDir());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // You can pretty print the data
        tif.printAsText();

        // You can write the data to CSV
        PrintWriter printWriter = JSL.getInstance().makePrintWriter("data.csv");
        tif.writeAsCSV(printWriter, true);
        printWriter.close();

        // You can copy the file to a SQLite database
        try {
            DatabaseIfc database = tif.asDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
