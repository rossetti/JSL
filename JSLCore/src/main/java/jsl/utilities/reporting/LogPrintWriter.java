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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;

/** A wrapper for a PrintWriter.  This class has all the functionality of
 *  PrintWriter but has a public field OUTPUT_ON that can be set to false
 *  to turn off any printing or set to true to turn printing on.
 * @author rossetti
 *
 */
public class LogPrintWriter extends PrintWriter {

    /**
     *  Controls whether or not any the PrintWriter functionality happens
     */
    public boolean OUTPUT_ON = true;

    /**
     * @param out the Writer
     */
    public LogPrintWriter(Writer out) {
        super(out);
    }

    /**
     * @param out the output stream
     */
    public LogPrintWriter(OutputStream out) {
        super(out);
    }

    /**
     * @param fileName the file name
     * @throws FileNotFoundException the exception
     */
    public LogPrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * @param file the file
     * @throws FileNotFoundException the exception
     */
    public LogPrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * @param out the Writer
     * @param autoFlush true means auto flush
     */
    public LogPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * @param out the Writer
     * @param autoFlush true means auto flush
     */
    public LogPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * @param fileName the file name to use
     * @param csn name of the character set
     * @throws FileNotFoundException an exception
     * @throws UnsupportedEncodingException an exception
     */
    public LogPrintWriter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    /**
     * @param file the file
     * @param csn name of the character set
     * @throws FileNotFoundException an exception
     * @throws UnsupportedEncodingException an exception
     */
    public LogPrintWriter(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void println(String x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public PrintWriter append(char c) {
        if (OUTPUT_ON) {
            return (super.append(c));
        } else {
            return (this);
        }
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        if (OUTPUT_ON) {
            return (super.append(csq, start, end));
        } else {
            return (this);
        }
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        if (OUTPUT_ON) {
            return (super.append(csq));
        } else {
            return (this);
        }
    }

    @Override
    public void print(boolean b) {
        if (OUTPUT_ON) {
            super.print(b);
        }
    }

    @Override
    public void print(char c) {
        if (OUTPUT_ON) {
            super.print(c);
        }
    }

    @Override
    public void print(char[] s) {
        if (OUTPUT_ON) {
            super.print(s);
        }
    }

    @Override
    public void print(double d) {
        if (OUTPUT_ON) {
            super.print(d);
        }
    }

    @Override
    public void print(float f) {
        if (OUTPUT_ON) {
            super.print(f);
        }
    }

    @Override
    public void print(int i) {
        if (OUTPUT_ON) {
            super.print(i);
        }
    }

    @Override
    public void print(long l) {
        if (OUTPUT_ON) {
            super.print(l);
        }
    }

    @Override
    public void print(Object obj) {
        if (OUTPUT_ON) {
            super.print(obj);
        }
    }

    @Override
    public void print(String s) {
        if (OUTPUT_ON) {
            super.print(s);
        }
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        if (OUTPUT_ON) {
            return super.printf(l, format, args);
        } else {
            return (this);
        }
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        if (OUTPUT_ON) {
            return super.printf(format, args);
        } else {
            return this;
        }
    }

    @Override
    public void println() {
        if (OUTPUT_ON) {
            super.println();
        }
    }

    @Override
    public void println(boolean x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(char x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(char[] x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(double x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(float x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(int x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(long x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void println(Object x) {
        if (OUTPUT_ON) {
            super.println(x);
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {
        if (OUTPUT_ON) {
            super.write(buf, off, len);
        }
    }

    @Override
    public void write(char[] buf) {
        if (OUTPUT_ON) {
            super.write(buf);
        }
    }

    @Override
    public void write(int c) {
        if (OUTPUT_ON) {
            super.write(c);
        }
    }

    @Override
    public void write(String s, int off, int len) {
        if (OUTPUT_ON) {
            super.write(s, off, len);
        }
    }

    @Override
    public void write(String s) {
        if (OUTPUT_ON) {
            super.write(s);
        }
    }
}
