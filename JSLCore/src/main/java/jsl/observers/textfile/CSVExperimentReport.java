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
import java.util.List;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.utilities.statistic.Statistic;

/** Represents a comma separated value file for experiment data (across
 *  replication data)
 * 
 *  SimName, ModelName, ExpName, RepNum, ResponseType, ResponseID, ResponseName, ..
 *  then the header from StatisticAccessorIfc.getCSVStatisticHeader()
 * 
 *  Captures all ResponseVariables, TimeWeighted variables, and Counters
 *
 */
public class CSVExperimentReport extends CSVReport {

    /** Makes a report as a file within the supplied path
     *
     * @param pathToFile the path to the file, must not be null
     */
    public CSVExperimentReport(Path pathToFile) {
        super(pathToFile);
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
        myWriter.print("ResponseType,");
        myWriter.print("ResponseID,");
        myWriter.print("ResponseName,");
        Statistic s = new Statistic();
        myWriter.print(s.getCSVStatisticHeader());
        myWriter.println();
    }

    @Override
    protected void afterExperiment(ModelElement m, Object arg) {

        Model model = m.getModel();
        Simulation sim = m.getSimulation();

        List<ResponseVariable> rvs = model.getResponseVariables();

        for (ResponseVariable rv : rvs) {
            if (rv.getDefaultReportingOption()) {
                myWriter.print(sim.getName());
                myWriter.print(",");
                myWriter.print(sim.getModel().getName());
                myWriter.print(",");
                myWriter.print(sim.getExperiment().getExperimentName());
                myWriter.print(",");
                myWriter.print(rv.getClass().getSimpleName() + ",");
                myWriter.print(rv.getId() + ",");
                myWriter.print(rv.getName() + ",");
                myWriter.print(rv.getAcrossReplicationStatistic().getCSVStatistic());
                myWriter.println();
            }
        }

        List<Counter> counters = model.getCounters();

        for (Counter c : counters) {
            if (c.getDefaultReportingOption()) {
                myWriter.print(sim.getName());
                myWriter.print(",");
                myWriter.print(sim.getModel().getName());
                myWriter.print(",");
                myWriter.print(sim.getExperiment().getExperimentName());
                myWriter.print(",");
                myWriter.print(c.getClass().getSimpleName() + ",");
                myWriter.print(c.getId() + ",");
                myWriter.print(c.getName() + ",");
                myWriter.print(c.getAcrossReplicationStatistic().getCSVStatistic());
                myWriter.println();
            }
        }
    }
}
