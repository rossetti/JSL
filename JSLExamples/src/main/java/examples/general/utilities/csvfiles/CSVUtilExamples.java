package examples.general.utilities.csvfiles;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.reporting.JSL;
import jsl.utilities.reporting.TableFormatter;
import jslx.CSVUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVUtilExamples {


    public static void main(String[] args) {
        Path csvDir = JSL.getInstance().makeSubDirectory("CSVFiles");
        Path filePath = csvDir.resolve("MyFile.csv");
        makeExampleCSVFile(filePath);
        readExampleCSVFile(filePath);
        Path dataPath = csvDir.resolve("data");
        readArray(dataPath);
    }

    public static void makeExampleCSVFile(Path filePath) {
        String[] header = {"x", "y"};
        double[][] data = {{1.1, 2.0},
                {4.3, 6.4}};
        CSVUtil.writeArrayToCSVFile(Arrays.asList(header), data, filePath);
    }

    public static void readExampleCSVFile(Path filePath){
        List<String[]> strings = CSVUtil.readRows(filePath);
        for(String[] sArray: strings){
            System.out.println(Arrays.toString(sArray));
        }
        List<String> header = new ArrayList<>();
        double[][] rows = CSVUtil.readToRows(header, filePath);

        System.out.println(header);
        System.out.println(TableFormatter.asString(rows));

        double[][] cols = CSVUtil.readToColumns(header, filePath);

        System.out.println(header);
        System.out.println(TableFormatter.asString(cols));

    }

    public static void readArray(Path filePath){
        double[] array = JSLArrayUtil.scanToArray(filePath);
        System.out.println(Arrays.toString(array));
    }
}
