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
package jsl.observers.variable;

import java.io.File;
import java.nio.file.Path;

import jsl.modeling.elements.variable.Variable;
import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Model;
import jsl.utilities.reporting.TextReport;
import jsl.observers.ObserverIfc;
import org.w3c.dom.Text;

/**
 * This class creates a comma separated file that traces the value of a variable
 *
 * observation number, time of change, value, time of previous change, previous
 * value, weight, replication number, within replication count, experiment name
 *
 */
public class VariableTraceTextReport implements ObserverIfc {

    protected long myCount = 0;

    protected long myRepCount = 0;

    protected double myRepNum = 0;

    private final TextReport myTextReport;

    /**
     *
     * @param pathToFile the path to the file
     * @param header the header
     */
    public VariableTraceTextReport(Path pathToFile, boolean header) {
        myTextReport = new TextReport(pathToFile);
        if (header) {
            writeHeader();
        }
    }

    private void writeHeader() {
        myTextReport.print("n");
        myTextReport.print(",");
        myTextReport.print("t");
        myTextReport.print(",");
        myTextReport.print("x(t)");
        myTextReport.print(",");
        myTextReport.print("t(n-1)");
        myTextReport.print(",");
        myTextReport.print("x(t(n-1))");
        myTextReport.print(",");
        myTextReport.print("w");
        myTextReport.print(",");
        myTextReport.print("r");
        myTextReport.print(",");
        myTextReport.print("nr");
        myTextReport.print(",");
        myTextReport.print("sim");
        myTextReport.print(",");
        myTextReport.print("model");
        myTextReport.print(",");
        myTextReport.print("exp");
        myTextReport.println();
    }

    @Override
    public void update(Object observable, Object obj) {
        Variable v = (Variable) observable;
        Model m = v.getModel();

        if (v.checkForUpdate()) {
            myCount++;
            myTextReport.print(myCount);
            myTextReport.print(",");
            myTextReport.print(v.getTimeOfChange());
            myTextReport.print(",");
            myTextReport.print(v.getValue());
            myTextReport.print(",");
            myTextReport.print(v.getPreviousTimeOfChange());
            myTextReport.print(",");
            myTextReport.print(v.getPreviousValue());
            myTextReport.print(",");
            myTextReport.print(v.getWeight());
            myTextReport.print(",");
            ExperimentGetIfc e = v.getExperiment();
            if (e != null) {
                if (myRepNum != e.getCurrentReplicationNumber()) {
                    myRepCount = 0;
                }
                myRepCount++;
                myRepNum = e.getCurrentReplicationNumber();
                myTextReport.print(myRepNum);
                myTextReport.print(",");
                myTextReport.print(myRepCount);
                myTextReport.print(",");
                myTextReport.print(m.getSimulation().getName());
                myTextReport.print(",");
                myTextReport.print(m.getName());
                myTextReport.print(",");
                myTextReport.print(e.getExperimentName());
            }
            myTextReport.println();

        }
    }
}
