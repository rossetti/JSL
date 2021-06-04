package examples.utilities;

import jsl.utilities.reporting.JSL;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class TextFileWriteSample {
    public static void main(String[] args) {
        try {
            writeExample1();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeExample2();
        System.out.println("Done!");
        JSL.getInstance().out.println("Hello world!");
    }

    public static void writeExample1() throws IOException {
        FileOutputStream fileStream = null;
        PrintWriter outFS = null;
        // Try to open file
        // specify the path to the file

        Path path = JSL.getInstance().getOutDir().resolve("myoutfile.txt");
        fileStream = new FileOutputStream(path.toFile());

        outFS = new PrintWriter(fileStream);

        // Arriving here implies that the file can now be written
        // to, otherwise an exception would have been thrown.
        outFS.println("Hello");
        outFS.println("1 2 3");
        outFS.flush();

        // Done with file, so try to close
        // Note that close() may throw an IOException on failure
        fileStream.close();
    }

    public static void writeExample2(){
        // use the JSL to make your PrintWriter
        // automatically saves file in jslOutputDir
        PrintWriter outFS = JSL.getInstance().makePrintWriter("myoutfile2.txt");
        outFS.println("Hello");
        outFS.println("1 2 3");
        outFS.flush();
        outFS.close();
    }
}
