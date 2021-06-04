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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.misc;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import jsl.utilities.JSLFileUtil;
import jsl.utilities.reporting.JSL;

/**
 *
 * @author rossetti
 */
public class TestPDFLATEX {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String s = "Jobshop_SummaryReport.tex";
//        String[] g = s.split("\\.");
//        for (String k : g) {
//            System.out.println(k);
//        }

       // test2();

        test3();

    }

    public static void test3() {
        // JOptionPane.showMessageDialog(null, "test");
        JOptionPane.showMessageDialog(null,
                "This is a warning message",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
    
    public static void test4(){
        
    }

    public static void test2() {
        File f = new File("Jobshop_SummaryReport.tex");
        try {
            makePDFFromLaTeX(f);
            System.out.println("made pdf file for " + "Jobshop_SummaryReport.tex");
        } catch (Exception ex) {
            System.out.println("failed to make pdf file for " + "Jobshop_SummaryReport.tex");
            Logger.getLogger(TestPDFLATEX.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            makePDFFromLaTeX("jslOutput", "Jobshop_SummaryReport.tex");
            System.out.println("made pdf file for " + "Jobshop_SummaryReport.tex");
        } catch (Exception ex) {
            System.out.println("failed to make pdf file for " + "Jobshop_SummaryReport.tex");
            Logger.getLogger(TestPDFLATEX.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            File d = new File("jslOutput");
            d.mkdir();
            File file = new File(d, "Jobshop_SummaryReport.pdf");
            openFile(file);
        } catch (IOException ex) {
            Logger.getLogger(TestPDFLATEX.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int makePDFFromLaTeX(String dirname, String filename) throws IOException, InterruptedException {
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
        b.command("/usr/texbin/pdflatex", f.getName());
        b.directory(d);
        Process process = b.start();
        process.waitFor();
        return process.exitValue();
    }

    public static int makePDFFromLaTeX(File file) throws IOException, InterruptedException {
        if (file == null) {
            throw new IllegalArgumentException("The file was null");
        }
        if (!file.exists()) {
            throw new IOException("The file did not exist");
        }
        if (file.length() == 0) {
            throw new IOException("The file was empty");
        }

        String fn = file.getName();
        String[] g = fn.split("\\.");
        if (!g[1].equals("tex")) {
            throw new IllegalArgumentException("The file was not a tex file");
        }
        ProcessBuilder b = new ProcessBuilder();
        b.command("/usr/texbin/pdflatex", file.getName());
        Process process = b.start();
        process.waitFor();
        return process.exitValue();
    }

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

    public static void test() throws IOException {
        File f = new File("Jobshop_SummaryReport.tex");
//        File g = f.getParentFile();
//        System.out.println("Parent path = " + g.getPath());
//        System.out.println("Parent name = " + g.getName());
//        System.out.println("Parent abs path = " + g.getAbsolutePath());
        ProcessBuilder b = new ProcessBuilder("/usr/texbin/pdflatex", f.getName());
        File makeOutputSubDirectory = new File("jslOutput");
        makeOutputSubDirectory.mkdir();
        System.out.println("makeOutputSubDirectory = " + makeOutputSubDirectory);
        b.directory(makeOutputSubDirectory);
        //        ProcessBuilder b = new ProcessBuilder("/usr/texbin/pdflatex","-version");
        //        ProcessBuilder command = b.command("/usr/texbin/pdflatex",f.getAbsolutePath());

        //ProcessBuilder command = b.command("/usr/texbin/pdflatex","-version");

//        String s = "/usr/texbin/pdflatex " + f.getName();
        String s = "/usr/texbin/pdflatex " + f.getAbsolutePath();
        System.out.println("absolute path = " + f.getAbsolutePath());
        String canonicalPath = f.getCanonicalPath();
        System.out.println("canonical path = " + canonicalPath);
        System.out.println("name = " + f.getName());
        String cmd = "pwd";
        System.out.println(s);
        String workingDirectory = JSLFileUtil.getProgramLaunchDirectoryAsString();
        System.out.println(workingDirectory);
        //   b.command(s);
//        ProcessBuilder directory = b.directory(g);
        //ProcessBuilder command = directory.command(s);
//        System.out.println("directory = " + directory.directory());
        try {
            Process process = b.start();
//            command.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            System.out.printf("Output of running %s is:", cmd);

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.out.println("IO exception");
            System.out.println(ex);
        }
    }
}
