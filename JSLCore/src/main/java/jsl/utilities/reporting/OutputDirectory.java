package jsl.utilities.reporting;

import jsl.utilities.JSLFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class provides basic context for creating and writing output files.
 * Files and directories created by instances of this class will be relative to
 * the Path supplied at creation.
 *
 */
public class OutputDirectory {

    /**
     * for logging
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     *  The path to the default Excel directory
     */
    private Path excelDir;

    /**
     *  The path to the default output directory
     */
    private Path outDir;

    /**
     * Can be used like System.out, but instead writes to a file
     * found in the base output directory
     */
    public final LogPrintWriter out;

    /** Creates a OutputDirectory with the current program launch directory with the base directory, "OutputDir"
     *  and "Output_Out.txt" as the file name related to property out
     */
    public OutputDirectory(){
        this("OutputDir", "out.txt");
    }

    /** Creates a OutputDirectory with the current program launch directory as the base directory
     *
     * @param outDirName the name of the directory within the current launch directory
     * @param outFileName the name of the created text file related to property out
     */
    public OutputDirectory(String outDirName, String outFileName){
        this(JSLFileUtil.getProgramLaunchDirectory().resolve(outDirName), outFileName);
    }

    /** Creates an OutputDirectory based on the supplied path
     *
     * @param outputDirectory the base output directory to use for writing text files relative to this OutputDirectory instance
     * @param outFileName the name of the created text file related to property out
     */
    public OutputDirectory(Path outputDirectory, String outFileName){
        Objects.requireNonNull(outputDirectory, "The supplied output directory was null");
        Objects.requireNonNull(outFileName, "The supplied output file name was null");
        // need to create the directories if they do not exist
        try {
            outDir = Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            LOGGER.error("There was a problem creating the directories for {}", outputDirectory);
            e.printStackTrace();
        }
        Path newFilePath = getOutDir().resolve(outFileName);
        out = JSLFileUtil.makeLogPrintWriter(newFilePath);
    }

    /**
     *
     * @return the path to the base directory for this OutputDirectory
     */
    public final Path getOutDir(){
        return outDir;
    }

    /**
     *
     * @return the path to the default excel directory, relative to this output directory
     */
    public final Path getExcelDir(){
        if (excelDir == null){
            try {
                excelDir = Files.createDirectories(outDir.resolve("excel"));
            } catch (IOException e) {
                LOGGER.error("There was a problem creating the directories for {}", outDir.resolve("excel"));
                e.printStackTrace();
            }
        }
        return excelDir;
    }

    /** Makes a new PrintWriter within the base directory with the given file name
     *
     * @param fileName the name of the file for the PrintWriter
     * @return the PrintWriter, or System.out if there was some problem with its creation
     */
    public final PrintWriter makePrintWriter(String fileName){
        Objects.requireNonNull(fileName, "The supplied output file name was null");
        Path newFilePath = getOutDir().resolve(fileName);
        return JSLFileUtil.makePrintWriter(newFilePath);
    }

    /** Makes a new PrintWriter within the base directory with the given file name
     *
     * @param fileName the name of the file for the PrintWriter
     * @return the File in the base directory
     */
    public final File makeFile(String fileName){
        Objects.requireNonNull(fileName, "The supplied output file name was null");
        Path newFilePath = getOutDir().resolve(fileName);
        return newFilePath.toFile();
    }

    /** Makes a Path to the named sub-directory within the base directory
     *
     * @param dirName the name of the sub-directory to create. It must not be null
     * @return a path to the created sub-directory, or the base directory if something went wrong in the creation.
     * Any problems are logged.
     */
    public final Path makeSubDirectory(String dirName){
         return JSLFileUtil.makeSubDirectory(getOutDir(), dirName);
    }

}
