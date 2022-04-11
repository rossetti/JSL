package jslx;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import jsl.utilities.JSLArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.*;

/**
 * A class to facilitate some basic CSV processing without having to worry about underlying csv library.
 * Helps with reading and writing arrays to csv files. Generally, exceptions are squashed.
 */
public class CSVUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Reads all rows from a csv file that may have the first row as a header
     * of column labels and each subsequent row as the data for
     * each column, e.g.
     * "x", "y"
     * 1.1, 2.0
     * 4.3, 6.4
     * etc.
     * This method squelches any IOExceptions
     *
     * @param pathToFile the path to the file
     * @return the filled list
     */
    public static List<String[]> readRows(Path pathToFile) {
        return readRows(0, pathToFile);
    }

    /**
     * Reads all rows from a csv file that may have the first row as a header
     * of column labels and each subsequent row as the data for
     * each column, e.g.
     * "x", "y"
     * 1.1, 2.0
     * 4.3, 6.4
     * etc.
     * This method squelches any IOExceptions. Writes warning to log. If there was a problem
     * an empty list is returned.
     *
     * @param skipLines  the number of lines to skip from the top of the file
     * @param pathToFile the path to the file
     * @return the filled list
     */
    public static List<String[]> readRows(int skipLines, Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile.toFile()))) {
            List<String[]> list = reader.readAll();
            if (skipLines > 0) {
                return list.subList(skipLines, list.size());
            } else {
                return list;
            }
        } catch (IOException | CsvException e) {
            LOGGER.warn("There was a problem reading the rows from file {}", pathToFile);
        }
        return new LinkedList<>();
    }

    /**
     * Returns an iterator to the rows of a csv file that may have the first row as a header
     * of column labels and each subsequent row as the data for
     * each column, e.g.
     * "x", "y"
     * 1.1, 2.0
     * 4.3, 6.4
     * etc.
     * This method squelches any IOExceptions. An iterator with no elements is returned if there is a problem.
     *
     * @param pathToFile the path to the file
     * @return the filled list
     */
    public static Iterator<String[]> getCSVIterator(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile.toFile()))) {
            return reader.iterator();
        } catch (IOException e) {
            LOGGER.warn("There was a problem getting an iterator from file {}", pathToFile);
        }
        return new LinkedList<String[]>().iterator();
    }

    /**
     * Reads data from a csv file that has the first row as a header
     * of column labels and each subsequent row as the data for
     * each row, e.g.
     * "x", "y"
     * 1.1, 2.0
     * 4.3, 6.4
     * etc.
     * The List names will hold ("x", "y"). If names has strings it will be cleared.
     * The returned array will hold
     * data[0] = {1.1, 2.0}
     * data[1] = {4.3, 6.4}
     * etc.
     *
     * @param names      the list to fill with header names
     * @param pathToFile the path to the file
     * @return the filled array of arrays
     */
    public static double[][] readToRows(List<String> names, Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        if (names == null) {
            names = new ArrayList<>();
        }
        names.clear();
        List<String[]> entries = readRows(pathToFile);
        if (entries.isEmpty()) {
            // no header and no data
            return new double[0][0];
        }
        // size at least 1
        names.addAll(Arrays.asList(entries.get(0)));
        if (entries.size() == 1) {
            // only header, no data
            return new double[0][0];
        }
        // was header and a least 1 other row
        return JSLArrayUtil.parseTo2DArray(entries.subList(1, entries.size()));
    }

    /**
     * Reads data from a csv file that has the first row as a header
     * of column labels and each subsequent row as the data for
     * each column, e.g.
     * "x", "y"
     * 1.1, 2.0
     * 4.3, 6.4
     * etc.
     * The List names will hold ("x", "y"). If names has strings it will be cleared.
     * The returned array will hold
     * data[0] = {1.1, 4.3}
     * data[1] = {2.0, 6.4}
     * etc.
     *
     * @param names      the list to fill with header names
     * @param pathToFile the path to the file
     * @return the filled array of arrays
     */
    public static double[][] readToColumns(List<String> names, Path pathToFile) {
        double[][] data = readToRows(names, pathToFile);
        return JSLArrayUtil.transpose2DArray(data);
    }

    /**
     * IOException is squelched with a warning to the logger if there was a problem writing to the file.
     *
     * @param array      the array to write
     * @param pathToFile the path to the file
     */
    public static void writeArrayToCSVFile(double[][] array, Path pathToFile) {
        writeArrayToCSVFile(null, array, false, pathToFile);
    }

    /**
     * IOException is squelched with a warning to the logger if there was a problem writing to the file.
     *
     * @param header     the names of the columns as strings
     * @param array      the array to write
     * @param pathToFile the path to the file
     */
    public static void writeArrayToCSVFile(List<String> header, double[][] array, Path pathToFile) {
        writeArrayToCSVFile(header, array, false, pathToFile);
    }

    /**
     * IOException is squelched with a warning to the logger if there was a problem writing to the file.
     *
     * @param header            the names of the columns as strings
     * @param array             the array to write
     * @param applyQuotesToData if true the numeric data will be surrounded by quotes
     * @param pathToFile        the path to the file
     */
    public static void writeArrayToCSVFile(List<String> header, double[][] array, boolean applyQuotesToData, Path pathToFile) {
        // if header is empty or null get size from array and make col names
        Objects.requireNonNull(array, "The array was null");
        Objects.requireNonNull(pathToFile, "The path to the file was null");
        if (!JSLArrayUtil.isRectangular(array)) {
            throw new IllegalArgumentException("The supplied array was not rectangular");
        }
        if (header == null) {
            header = new ArrayList<>();
        }
        if (header.isEmpty()) {
            int nc = JSLArrayUtil.getMaxNumColumns(array);
            for (int i = 0; i < nc; i++) {
                String name = "col" + i;
                header.add(name);
            }
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(pathToFile.toFile()))) {
            writer.writeNext(header.toArray(new String[0]));
            for (int i = 0; i < array.length; i++) {
                writer.writeNext(JSLArrayUtil.toString(array[i]), applyQuotesToData);
            }
        } catch (IOException e) {
            LOGGER.warn("There was a problem writing an array to csv file {}", pathToFile);
        }
    }

}
