package jsl.utilities.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

public class JSL {

    /**
     * for logging
     */
    public final Logger LOGGER;

    /**
     * Used to assign unique enum constants
     */
    private int myEnumCounter;
    private final OutputDirectory myOutputDir;
    public final LogPrintWriter out;

    private JSL(){
        myOutputDir = new OutputDirectory("jslOutput", "jslOutput.txt");
        out = myOutputDir.out;
        LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }

    private static class JSLSingleton {
        private static final JSL INSTANCE = new JSL();
    }

    public static JSL getInstance(){
        return JSLSingleton.INSTANCE;
    }

    /**
     *
     * @return the path to the base directory for this OutputDirectory
     */
    public final Path getOutDir() {
        return myOutputDir.getOutDir();
    }

    /**
     *
     * @return the path to the default excel directory, relative to this output directory
     */
    public final Path getExcelDir() {
        return myOutputDir.getExcelDir();
    }

    /**
     * Should be used by classes to get the next constant
     * so that unique constants can be used
     *
     * @return the constant
     */
    public int getNextEnumConstant() {
        return (++myEnumCounter);
    }

    /** Makes a new PrintWriter within the base directory with the given file name
     *
     * @param fileName the name of the file for the PrintWriter
     * @return the PrintWriter, or System.out if there was some problem with its creation
     */
    public final PrintWriter makePrintWriter(String fileName) {
        return myOutputDir.makePrintWriter(fileName);
    }

    /** Makes a new PrintWriter within the base directory with the given file name
     *
     * @param fileName the name of the file for the PrintWriter
     * @return the File in the base directory
     */
    public final File makeFile(String fileName) {
        return myOutputDir.makeFile(fileName);
    }

    /** Makes a Path to the named sub-directory within the base directory
     *
     * @param dirName the name of the sub-directory to create. It must not be null
     * @return a path to the created sub-directory, or the base directory if something went wrong in the creation.
     * Any problems are logged.
     */
    public final Path makeSubDirectory(String dirName) {
        return myOutputDir.makeSubDirectory(dirName);
    }

    public static void main(String[] args) {
        JSL.getInstance().out.println("The stuff to write");
    }
}
