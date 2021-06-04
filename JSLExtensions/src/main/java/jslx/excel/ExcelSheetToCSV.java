/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jslx.excel;

import java.io.PrintWriter;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.*;
import org.apache.poi.xssf.usermodel.XSSFComment;

/**
 * @author rossetti
 */
class ExcelSheetToCSV implements SheetContentsHandler {

    /**
     * Number of columns to read starting with leftmost
     */
    private final int minColumns;

    /**
     * Destination for data
     */
    private final PrintWriter output;
    private boolean firstCellOfRow;
    private int currentRow = -1;
    private int currentCol = -1;

    /**
     * Creates a sheet content handler to translate and Excel sheet to
     * comma separated file format with no minimum number of columns, written
     * to System.out
     * <p>
     */
    public ExcelSheetToCSV() {
        this(null, -1);
    }

    /**
     * Creates a sheet content handler to translate and Excel sheet to
     * comma separated file format with no minimum number of columns.
     *
     * @param output the output stream to write to
     */
    public ExcelSheetToCSV(PrintWriter output) {
        this(output, -1);
    }

    /**
     * Creates a sheet content handler to translate and Excel sheet to
     * comma separated file format
     *
     * @param output     The PrintStream to output the CSV to
     * @param minColumns The minimum number of columns to output, or -1 for no
     *                   minimum
     */
    public ExcelSheetToCSV(PrintWriter output, int minColumns) {
        if (output == null) {
            output = new PrintWriter(System.out);
        }
        this.minColumns = minColumns;
        this.output = output;
    }

    private void outputMissingRows(int number) {
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < minColumns; j++) {
                output.append(',');
            }
            output.append(System.lineSeparator());
        }
    }

    @Override
    public void startRow(int rowNum) {
        // If there were gaps, output the missing rows
        outputMissingRows(rowNum - currentRow - 1);
        // Prepare for this row
        firstCellOfRow = true;
        currentRow = rowNum;
        currentCol = -1;
    }

    @Override
    public void endRow(int rowNum) {
        // Ensure the minimum number of columns
        for (int i = currentCol; i < minColumns; i++) {
            output.append(',');
        }
        output.append(System.lineSeparator());
    }

    @Override
    public void cell(String cellReference, String formattedValue,
                     XSSFComment comment) {
        if (firstCellOfRow) {
            firstCellOfRow = false;
        } else {
            output.append(',');
        }

        // gracefully handle missing CellRef here in a similar way as XSSFCell does
        if (cellReference == null) {
            cellReference = new CellAddress(currentRow, currentCol).formatAsString();
        }

        // Did we miss any cells?
        int thisCol = (new CellReference(cellReference)).getCol();
        int missedCols = thisCol - currentCol - 1;
        for (int i = 0; i < missedCols; i++) {
            output.append(',');
        }
        currentCol = thisCol;

        // Number or string?
        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(formattedValue);
            output.append(formattedValue);
        } catch (NumberFormatException e) {
            output.append('"');
            output.append(formattedValue);
            output.append('"');
        }
    }

    @Override
    public void headerFooter(String string, boolean bln, String string1) {
        //throw new UnsupportedOperationException("Not supported yet.");
        //To change body of generated methods, choose Tools | Templates.
    }
}
