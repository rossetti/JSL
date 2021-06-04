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
import java.nio.file.Path;
import java.util.*;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.utilities.statistic.*;
import jsl.modeling.elements.variable.*;

/** Represents a comma separated value file for replication data
 * 
 *  SimName, ModelName, ExpName, RepNum, ResponseType, ResponseID, ResponseName, ..
 *  then the header from WeightedStatistic.getCSVStatisticHeader()
 * 
 *  Captures all ResponseVariables, TimeWeighted variables, and Counters
 *
 */
public class CSVReplicationReport extends CSVReport {

    protected int myRepCount = 0;

    /** Makes a report as a file within the supplied path
     *
     * @param pathToFile the path to the file, must not be null
     */
    public CSVReplicationReport(Path pathToFile) {
        super(pathToFile);
    }

    /**
     * @return The number of times afterReplication was called
     */
    public final int getReplicationCount() {
        return myRepCount;
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        super.beforeExperiment(m, arg);
        myRepCount = 0;
    }

    @Override
    protected void writeHeader() {
        if (myHeaderFlag == true) {
            return;
        }
        myHeaderFlag = true;
        myWriter.print("SimName,");
        myWriter.print("ModelName,");
        myWriter.print("ExpName,");
        myWriter.print("RepNum,");
        myWriter.print("ResponseType,");
        myWriter.print("ResponseID,");
        myWriter.print("ResponseName,");
        WeightedStatistic w = new WeightedStatistic();
        myWriter.print(w.getCSVStatisticHeader());
        myWriter.println();

    }

    private void writeLine(Simulation sim, ResponseVariable rv) {
        myWriter.print(sim.getName());
        myWriter.print(",");
        myWriter.print(sim.getModel().getName());
        myWriter.print(",");
        myWriter.print(sim.getExperiment().getExperimentName());
        myWriter.print(",");
        //myWriter.print(sim.getExperiment().getCurrentReplicationNumber());
        myWriter.print(myRepCount);
        myWriter.print(",");
        myWriter.print(rv.getClass().getSimpleName());
        myWriter.print(",");
        myWriter.print(rv.getId());
        myWriter.print(",");
        myWriter.print(rv.getName());
        myWriter.print(",");
        myWriter.print(rv.getWithinReplicationStatistic().getCSVStatistic());
        myWriter.println();
    }

    private void writeLine(Simulation sim, Counter c) {
        myWriter.print(sim.getName());
        myWriter.print(",");
        myWriter.print(sim.getModel().getName());
        myWriter.print(",");
        myWriter.print(sim.getExperiment().getExperimentName());
        myWriter.print(",");
        //myWriter.print(sim.getExperiment().getCurrentReplicationNumber());
        myWriter.print(myRepCount);
        myWriter.print(",");
        myWriter.print(c.getClass().getSimpleName());
        myWriter.print(",");
        myWriter.print(c.getId());
        myWriter.print(",");
        myWriter.print(c.getName());
        myWriter.print(",");
        myWriter.print(c.getName());
        myWriter.print(",");
        myWriter.print(c.getValue());
        myWriter.println();
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        Model model = m.getModel();
        Simulation sim = m.getSimulation();
        myRepCount++;
        List<ResponseVariable> rvs = model.getResponseVariables();

        for (ResponseVariable rv : rvs) {
            if (rv.getDefaultReportingOption()) {
                writeLine(sim, rv);
            }
        }

        List<Counter> counters = model.getCounters();

        for (Counter c : counters) {
            if (c.getDefaultReportingOption()) {
                writeLine(sim, c);
            }
        }

    }
    
    
}
