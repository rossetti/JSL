package jslx.tabularfiles;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DoubleTabularFile {


    private final int myNumCols;

    private int myNumRows;

    private double[][] myData;

    public DoubleTabularFile(Path pathToFile, List<String> colNames){
        Objects.requireNonNull(pathToFile, "The path to the file was null!");
        Objects.requireNonNull(colNames, "The list of column names was null!");
        if (colNames.isEmpty()){
            throw new IllegalArgumentException("The list of column names was empty");
        }
        myNumCols = colNames.size();
        int rowBytes = myNumCols*8;

    }

    //TODO make instance based on number of columns
}
