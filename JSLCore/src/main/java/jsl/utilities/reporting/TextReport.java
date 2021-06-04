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
package jsl.utilities.reporting;

import jsl.utilities.JSLFileUtil;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

/** Wraps a PrintWriter to make a txt file for making a report. Allows the file
 *  name and date to be added to the file via addFileNameAndDate() method
 * 
 * @author rossetti
 */
public class TextReport {

    private final Path myFilePath;

    protected final PrintWriter myPrintWriter;

    public TextReport(Path pathToFile) {
        myPrintWriter = JSLFileUtil.makePrintWriter(pathToFile);
        myFilePath = pathToFile;
    }         

    public Path getFilePath() {
        return myFilePath;
    }

    public void addFileNameAndDate() {
        myPrintWriter.println(myFilePath.getFileName());
        myPrintWriter.println(Instant.now());
        myPrintWriter.println();
    }

    public void write(String s) {
        myPrintWriter.write(s);
    }

    public void write(String s, int off, int len) {
        myPrintWriter.write(s, off, len);
    }

    public void write(char[] buf) {
        myPrintWriter.write(buf);
    }

    public void write(char[] buf, int off, int len) {
        myPrintWriter.write(buf, off, len);
    }

    public void write(int c) {
        myPrintWriter.write(c);
    }

    public void println(Object x) {
        myPrintWriter.println(x);
    }

    public void println(String x) {
        myPrintWriter.println(x);
    }

    public void println(char[] x) {
        myPrintWriter.println(x);
    }

    public void println(double x) {
        myPrintWriter.println(x);
    }

    public void println(float x) {
        myPrintWriter.println(x);
    }

    public void println(long x) {
        myPrintWriter.println(x);
    }

    public void println(int x) {
        myPrintWriter.println(x);
    }

    public void println(char x) {
        myPrintWriter.println(x);
    }

    public void println(boolean x) {
        myPrintWriter.println(x);
    }

    public void println() {
        myPrintWriter.println();
    }

    public PrintWriter printf(Locale l, String format, Object... args) {
        return myPrintWriter.printf(l, format, args);
    }

    public PrintWriter printf(String format, Object... args) {
        return myPrintWriter.printf(format, args);
    }

    public void print(Object obj) {
        myPrintWriter.print(obj);
    }

    public void print(String s) {
        myPrintWriter.print(s);
    }

    public void print(char[] s) {
        myPrintWriter.print(s);
    }

    public void print(double d) {
        myPrintWriter.print(d);
    }

    public void print(float f) {
        myPrintWriter.print(f);
    }

    public void print(int l) {
        myPrintWriter.print(l);
    }

    public void print(long i) {
        myPrintWriter.print(i);
    }

    public void print(char c) {
        myPrintWriter.print(c);
    }

    public void print(boolean b) {
        myPrintWriter.print(b);
    }

    public PrintWriter format(Locale l, String format, Object... args) {
        return myPrintWriter.format(l, format, args);
    }

    public PrintWriter format(String format, Object... args) {
        return myPrintWriter.format(format, args);
    }

    public void flush() {
        myPrintWriter.flush();
    }

    public void close() {
        myPrintWriter.close();
    }

    public boolean checkError() {
        return myPrintWriter.checkError();
    }

    public PrintWriter append(char c) {
        return myPrintWriter.append(c);
    }

    public PrintWriter append(CharSequence csq, int start, int end) {
        return myPrintWriter.append(csq, start, end);
    }

    public PrintWriter append(CharSequence csq) {
        return myPrintWriter.append(csq);
    }
}
