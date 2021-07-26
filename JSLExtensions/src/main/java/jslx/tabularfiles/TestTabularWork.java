package jslx.tabularfiles;


import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.JSL;
import jslx.dbutilities.JSLDatabase;

import java.io.IOException;
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
        LinkedHashMap<String, DataType> columns = TabularFile.columns(3, DataType.NUMERIC);
        columns.put("c4", DataType.TEXT);
        columns.put("c5", DataType.NUMERIC);

        TabularOutputFile tif = new TabularOutputFile(columns, path);
        System.out.println(tif);

        NormalRV n = new NormalRV(10, 1);
        int k = 15;
        // get a row
        RowSetterIfc row = tif.getRow();
        System.out.println("Writing rows...");
        for (int i = 1; i <= k; i++) {
            // reuse the row, many times
            row.setNumeric(n.sample(5));
            row.setText(3, "text data " + i);
            tif.writeRow(row);
        }
        // don't forget to flush
        tif.flushRows();
        System.out.println("Done writing rows!");
        System.out.println();
    }

    private static void readFile(){
        Path path = JSLDatabase.dbDir.resolve("demoFile");
        TabularInputFile tif = new TabularInputFile(path);
        System.out.println(tif);

        for(RowGetterIfc row: tif){
            System.out.println(row);
        }
        System.out.println();
        System.out.println();

        List<RowGetterIfc> rows = tif.fetchRows(1, 5);
        for (RowGetterIfc row : rows) {
            System.out.println(row);
        }
        System.out.println();
        System.out.println();

        TabularInputFile.RowIterator iterator = tif.iterator(9);
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }

        System.out.println();
        System.out.println();
        Double[] numericColumn = tif.getNumericColumn(0, 10, true);
        for (Double v : numericColumn) {
            System.out.println(v);
        }

//        try {
//            tif.writeToExcelWorkbook("demoData.xlsx", JSL.getInstance().getExcelDir());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        tif.printAsText();

    }
}
