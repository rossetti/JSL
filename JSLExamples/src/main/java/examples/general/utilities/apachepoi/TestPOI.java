package examples.general.utilities.apachepoi;

import jsl.utilities.reporting.JSL;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class TestPOI {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        System.out.println("Executing Excel write example");
        writeExample();
        System.out.println("Excel write example done!");
        System.out.println();
        System.out.println("Executing Excel read example");
        readExample();
        System.out.println("Excel read example done!");
    }

    public static void writeExample() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        Row row = sheet.createRow(2);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(new Date());
        row.createCell(2).setCellValue(Calendar.getInstance());
        row.createCell(3).setCellValue("a string");
        row.createCell(4).setCellValue(true);

        // Write the output to a file
        Path wbPath = JSL.getInstance().getExcelDir().resolve("writeExampleWorkbook.xlsx");
        try (OutputStream fileOut = new FileOutputStream(wbPath.toString())) {
            wb.write(fileOut);
        }
    }

    public static void readExample() throws IOException, InvalidFormatException {
        //TODO this example requires this file to be there, call a method to make it, instead of storing it.
        Path wbPath = JSL.getInstance().getExcelDir().resolve("readDataExample.xlsx");
        OPCPackage pkg = OPCPackage.open(wbPath.toFile());
        XSSFWorkbook wb = new XSSFWorkbook(pkg);

        XSSFSheet sheet = wb.getSheetAt(0);
        // read stuff here
        Iterator<Row> rowIterator = sheet.rowIterator();

        // handle the header
        if (rowIterator.hasNext()){
            Row first = rowIterator.next();
            Cell cell1 = first.getCell(0);
            Cell cell2 = first.getCell(1);
            String v1 = cell1.getStringCellValue();
            String v2 = cell2.getStringCellValue();
            System.out.printf("%s, %s %n", v1, v2);
        }

        // process each row
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            Cell cell1 = row.getCell(0);
            Cell cell2 = row.getCell(1);
            double v1 = cell1.getNumericCellValue();
            double v2 = cell2.getNumericCellValue();
            System.out.printf("%f, %f %n", v1, v2);
        }

        pkg.close();
    }
}
