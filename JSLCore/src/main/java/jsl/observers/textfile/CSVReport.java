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
package jsl.observers.textfile;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Objects;

import jsl.simulation.ModelElement;
import jsl.observers.ModelElementObserver;
import jsl.utilities.JSLFileUtil;

/**
 *
 */
public abstract class CSVReport extends ModelElementObserver {

    protected PrintWriter myWriter;

    protected StringBuffer myLine;

    protected int myLineWidth = 300;

    protected char quoteChar = '"';

    protected boolean myHeaderFlag;

    /**
     * @param pathToFile the path to the file, must not be null
     */
    public CSVReport(Path pathToFile)  {
        super(null);
        Objects.requireNonNull(pathToFile, "The path to the CSVReport file was null!");
        if (!JSLFileUtil.hasCSVExtension(pathToFile)){
            // need to add csv extension
            pathToFile = pathToFile.resolve(".csv");
        }
        myWriter = JSLFileUtil.makePrintWriter(pathToFile);
        // name the observer
        String name = JSLFileUtil.removeLastFileExtension(pathToFile.getFileName().toString());
        setName(name);
        myLine = new StringBuffer(myLineWidth);
    }

    public void close() {
        myWriter.close();
    }

    abstract protected void writeHeader(); 

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        // write header
        writeHeader();
    }

}
