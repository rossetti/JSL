package jsl.utilities;

import jsl.utilities.reporting.LogPrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides some basic file utilities. Addtional utilities can be found in Google Guava and
 * Apache Commons IO.  However, this basic IO provides basic needs without external libraries and
 * is integrated withe JSL functionality.
 */
public class JSLFileUtil {

    private static int myFileCounter_;

    /**
     * for logging
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Returns the directory that the program was launched from on the OS
     * as a string. This call may throw a SecurityException if the system information
     * is not accessible. Uses System property "user.dir"
     *
     * @return the path as a string
     */
    public static String getProgramLaunchDirectoryAsString() {
        return System.getProperty("user.dir");
    }

    /**
     * Returns the path to the directory that the program was launched from on the OS.
     * This call may throw a SecurityException if the system information
     * is not accessible. Uses System property "user.dir"
     *
     * @return the path to the directory
     */
    public static Path getProgramLaunchDirectory() {
        return Paths.get("").toAbsolutePath();
    }

    /**
     * @param pathToFile the path to the file
     * @return true if the path exists and the extension is csv
     */
    public static boolean hasCSVExtension(Path pathToFile) {
        Optional<String> fromPath = getExtensionFromPath(pathToFile);
        if (fromPath.isEmpty()) {
            return false;
        }
        return fromPath.get().equalsIgnoreCase("csv");
    }

    /**
     * Makes a PrintWriter from the given path, any IOExceptions are caught and logged.
     * The path must be to a file, not a directory.  If the directories that are on the
     * path do not exist, they are created.  If the referenced file exists it is written over.
     *
     * @param pathToFile the path to the file that will be underneath the PrintWriter, must not be null
     * @return the returned PrintWriter, or a PrintWriter wrapping System.out if some problem occurs
     */
    public static PrintWriter makePrintWriter(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The supplied path was null");
        // make the intermediate directories
        Path dir = pathToFile.getParent();
        createDirectories(dir);
        return makePrintWriter(pathToFile.toFile());
    }

    /**
     * Makes a PrintWriter from the given File. IOExceptions are caught and logged.
     * If the file exists it is written over.
     *
     * @param file the file support the returned PrintWriter, must not be null
     * @return the PrintWriter, may be System.out if an IOException occurred
     */
    public static PrintWriter makePrintWriter(File file) {
        Objects.requireNonNull(file, "The supplied file was null");
        try {
            return new PrintWriter(new FileWriter(file), true);
        } catch (IOException ex) {
            String str = "Problem creating PrintWriter for " + file.getAbsolutePath();
            LOGGER.error(str, ex);
            return new PrintWriter(System.out);
        }
    }

    /**
     * Makes the file in the directory that the program launched within
     *
     * @param fileName the name of the file to make
     * @return the created PrintWriter, may be System.out if an IOException occurred
     */
    public static PrintWriter makePrintWriter(String fileName) {
        Objects.requireNonNull(fileName, "The supplied file name was null");
        return makePrintWriter(getProgramLaunchDirectory().resolve(fileName));
    }

    /**
     * Makes a PrintWriter from the given path, any IOExceptions are caught and logged.
     *
     * @param pathToFile the path to the file that will be underneath the PrintWriter, must not be null
     * @return the returned PrintWriter, or System.out if some IOException occurred
     */
    public static LogPrintWriter makeLogPrintWriter(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The supplied path was null");
        // make the intermediate directories
        Path dir = pathToFile.getParent();
        createDirectories(dir);
        return makeLogPrintWriter(pathToFile.toFile());
    }

    /**
     * Makes a LogPrintWriter from the given File. IOExceptions are caught and logged.
     * If the file exists it will be written over.
     *
     * @param file the file support the returned PrintWriter, must not be null
     * @return the LogPrintWriter, may be a PrintWriter wrapping System.out if an IOException occurred
     */
    public static LogPrintWriter makeLogPrintWriter(File file) {
        Objects.requireNonNull(file, "The supplied file was null");
        try {
            return new LogPrintWriter(new FileWriter(file), true);
        } catch (IOException ex) {
            String str = "Problem creating LogPrintWriter for " + file.getAbsolutePath();
            LOGGER.error(str, ex);
            return new LogPrintWriter(System.out);
        }
    }

    /**
     * Will throw an IOException if something goes wrong in the creation.
     *
     * @param path the path to the directory to create
     * @return the same path after creating the intermediate directories
     */
    public static Path createDirectories(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            LOGGER.error("There was a problem creating the directories for {}", path);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a sub-directory within the supplied main directory.
     *
     * @param mainDir a path to the directory to hold the sub-directory, must not be null
     * @param dirName the name of the sub-directory, must not be null
     * @return the path to the sub-directory, or mainDir, if something went wrong
     */
    public static Path makeSubDirectory(Path mainDir, String dirName) {
        Objects.requireNonNull(mainDir, "The supplied base directory name was null");
        Objects.requireNonNull(dirName, "The supplied output directory name was null");
        Path newDirPath = mainDir.resolve(dirName);
        try {
            return Files.createDirectories(newDirPath);
        } catch (IOException e) {
            LOGGER.error("There was a problem creating the sub-directory for {}", newDirPath);
            return mainDir;
        }
    }

    /**
     * @param pathToFile the path to the file, must not be null and must not be a directory
     * @return the reference to the File
     */
    public static File makeFile(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file was null!");
        if (Files.isDirectory(pathToFile)) {
            throw new IllegalArgumentException("The path was a directory not a file!");
        }
        createDirectories(pathToFile.getParent());
        return pathToFile.toFile();
    }

    /**
     * Uses Desktop.getDesktop() to open the file
     *
     * @param file the file
     * @throws IOException if file cannot be opened
     */
    public static void openFile(File file) throws IOException {
        if (file == null) {
            JOptionPane.showMessageDialog(null,
                    "Cannot open the supplied file because it was null",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null,
                    "Cannot open the supplied file because it does not exist.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Cannot open the supplied file because it \n AWT Desktop is not supported!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    /**
     * Creates a PDF representation of a LaTeX file within the
     * provided directory with the given name.
     *
     * @param pdfcmd   the command for making the pdf within the OS
     * @param dirname  must not be null
     * @param filename must not be null, must have .tex extension
     * @return the process exit value
     * @throws IOException          if file does not exist or end with .tex
     * @throws InterruptedException if it was interrupted
     */
    public static int makePDFFromLaTeX(String pdfcmd, String dirname, String filename)
            throws IOException, InterruptedException {
        if (dirname == null) {
            throw new IllegalArgumentException("The directory name was null");
        }
        if (filename == null) {
            throw new IllegalArgumentException("The file name was null");
        }
        File d = new File(dirname);
        d.mkdir();
        File f = new File(d, filename);
        if (!f.exists()) {
            d.delete();
            throw new IOException("The file did not exist");
        }
        if (f.length() == 0) {
            d.delete();
            throw new IOException("The file was empty");
        }
        String fn = f.getName();
        String[] g = fn.split("\\.");
        if (!g[1].equals("tex")) {
            throw new IllegalArgumentException("The file was not a tex file");
        }

        ProcessBuilder b = new ProcessBuilder();
        b.command(pdfcmd, f.getName());
        b.directory(d);
        Process process = b.start();
        process.waitFor();
        return process.exitValue();
    }

    /**
     * Creates a PDF representation of a LaTeX file within the
     * with the given name. Uses pdflatex if it
     * exists
     *
     * @param pdfCmdString must not be null, the appropriate OS system command to convert tex file
     * @param file         must not be null, must have .tex extension
     * @return the process exit value
     * @throws IOException          if file does not exist or end with .tex
     * @throws InterruptedException if it was interrupted
     */
    public static int makePDFFromLaTeX(String pdfCmdString, File file) throws IOException, InterruptedException {
        Objects.requireNonNull(pdfCmdString, "The latex to pdf command string was null");
        if (file == null) {
            throw new IllegalArgumentException("The file was null");
        }
        if (!file.exists()) {
            throw new IOException("The file did not exist");
        }
        if (file.length() == 0) {
            throw new IOException("The file was empty");
        }
        if (!isTexFileName(file.getName())) {
            throw new IllegalArgumentException("The file was not a tex file");
        }
        ProcessBuilder b = new ProcessBuilder();
        b.command(pdfCmdString, file.getName());
        Process process = b.start();
        process.waitFor();
        return process.exitValue();
    }

    /**
     * @param fileName the string path representation of the file
     * @return the string without the extensions
     */
    public static String removeLastFileExtension(String fileName) {
        return removeFileExtension(fileName, false);
    }

    /**
     * @param filename            the string path representation of the file
     * @param removeAllExtensions if true all extensions including the last are removed
     * @return the string without the extensions
     */
    public static String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    /**
     * This method will check for the dot ‘.' occurrence in the given filename.
     * If it exists, then it will find the last position of the dot ‘.'
     * and return the characters after that, the characters after the last dot ‘.' known as the file extension.
     * Special Cases:
     * <p>
     * No extension – this method will return an empty String
     * Only extension – this method will return the String after the dot, e.g. gitignore
     * See www.baeldung.com/java-file-extension
     * Used here to avoid having to use external library
     *
     * @param pathToFile the name of the file that has the extension
     * @return an optional holding the string of the extension or null
     */
    public static Optional<String> getExtensionFromPath(Path pathToFile) {
        return getExtensionByStringFileName(pathToFile.toAbsolutePath().toString());
    }

    /**
     * This method will check for the dot ‘.' occurrence in the given filename.
     * If it exists, then it will find the last position of the dot ‘.'
     * and return the characters after that, the characters after the last dot ‘.' known as the file extension.
     * Special Cases:
     * <p>
     * No extension – this method will return an empty String
     * Only extension – this method will return the String after the dot, e.g. “gitignore”
     * See www.baeldung.com/java-file-extension
     * Used here to avoid having to use external library
     *
     * @param filename the name of the file that has the extension
     * @return an optional holding the string of the extension or null
     */
    public static Optional<String> getExtensionByStringFileName(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * @param fileName the name of the file as a string
     * @return true if the extension for the file is txt or TXT
     */
    public static boolean isTextFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionByStringFileName(fileName);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("txt");
    }

    /**
     * @param pathToFile
     * @return true if extension on path is txt
     */
    public static boolean isTextFile(Path pathToFile) {
        if (pathToFile == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionFromPath(pathToFile);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("txt");
    }

    /**
     * @param fileName the name of the file as a string
     * @return true if the extension for the file is txt or TXT
     */
    public static boolean isCSVFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionByStringFileName(fileName);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("csv");
    }

    /**
     * @param pathToFile
     * @return true if extension on path is csv
     */
    public static boolean isCSVFile(Path pathToFile) {
        if (pathToFile == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionFromPath(pathToFile);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("csv");
    }

    /**
     * @param fileName the name of the file as a string
     * @return true if the extension for the file is tex
     */
    public static boolean isTexFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionByStringFileName(fileName);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("tex");
    }

    /**
     * @param pathToFile
     * @return true if extension on path is tex
     */
    public static boolean isTeXFile(Path pathToFile) {
        if (pathToFile == null) {
            return false;
        }
        Optional<String> optionalS = getExtensionFromPath(pathToFile);
        if (optionalS.isEmpty()) {
            return false;
        }
        return optionalS.get().equalsIgnoreCase("tex");
    }

    /**
     * Makes a String that has the form name.csv
     *
     * @param name the name
     * @return the formed String
     */
    public static String makeCSVFileName(String name) {
        return makeFileName(name, "csv");
    }

    /**
     * Makes a String that has the form name.txt
     *
     * @param name the name
     * @return the formed String
     */
    public static String makeTxtFileName(String name) {
        return makeFileName(name, "txt");
    }

    /**
     * Makes a String that has the form name.ext
     * If an extension already exists it is replaced.
     *
     * @param name the name
     * @param ext  the extension
     * @return the String
     */
    public static String makeFileName(String name, String ext) {
        if (name == null) {
            myFileCounter_ = myFileCounter_ + 1;
            name = "Temp" + myFileCounter_;
        }
        if (ext == null) {
            ext = "txt";
        }
        String s;
        int dot = name.lastIndexOf(".");
        if (dot == -1) {
            // no period found
            s = name + "." + ext;
        } else {
            // period found
            s = name.substring(dot) + ext;
        }
        return (s);
    }

    public static boolean deleteDirectory(Path pathToDir) {
        Objects.requireNonNull(pathToDir, "The supplied path was null");
        return deleteDirectory(pathToDir.toFile());
    }

    /**
     * Recursively deletes
     *
     * @param directoryToBeDeleted the file reference to the directory to delete
     * @return true if deleted
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        Objects.requireNonNull(directoryToBeDeleted, "The supplied file directory was null");
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static void copyDirectoryInternal(File sourceDirectory, File destinationDirectory) throws IOException {
        Objects.requireNonNull(sourceDirectory, "The source directory was null");
        Objects.requireNonNull(destinationDirectory, "The destination directory was null");
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        for (String f : sourceDirectory.list()) {
            copyDirectory(new File(sourceDirectory, f), new File(destinationDirectory, f));
        }
    }

    /**
     *
     * @param source the source directory as a file, must not be null
     * @param destination the destination directory as a file, must not be null
     * @throws IOException if a problem occurs
     */
    public static void copyDirectory(Path source, Path destination) throws IOException{
        Objects.requireNonNull(source, "The source directory was null");
        Objects.requireNonNull(destination, "The destination directory was null");
        copyDirectory(source.toFile(), destination.toFile());
    }

    /**
     *
     * @param source the source directory as a file, must not be null
     * @param destination the destination directory as a file, must not be null
     * @throws IOException if a problem occurs
     */
    public static void copyDirectory(File source, File destination) throws IOException {
        Objects.requireNonNull(source, "The source directory was null");
        Objects.requireNonNull(destination, "The destination directory was null");
        if (source.isDirectory()) {
            copyDirectoryInternal(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyFile(File sourceFile, File destinationFile) throws IOException {
        Objects.requireNonNull(sourceFile, "The source file was null");
        Objects.requireNonNull(destinationFile, "The destination file was null");
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
