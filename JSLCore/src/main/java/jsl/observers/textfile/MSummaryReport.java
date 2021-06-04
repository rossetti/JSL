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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import jsl.simulation.Model;
import jsl.modeling.elements.variable.*;
import jsl.observers.ObserverIfc;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.reporting.TextReport;

public class MSummaryReport implements ObserverIfc {

    protected Collection<ResponseVariable> myResponseVariables;

    protected Model myModel;

    private final TextReport myTextReport;

    public MSummaryReport(Path pathToFile) {
        myTextReport = new TextReport(pathToFile);
    }

    public void update(Object observable, Object obj) {

        myModel = (Model) observable;

        if (myModel.checkForAfterExperiment()) {

            myTextReport.println();
            myTextReport.println();
            myTextReport.println("----------------------------------------------------------");
            myTextReport.println("Across Replication statistics");
            myTextReport.println("----------------------------------------------------------");
            myTextReport.println();

            List<ResponseVariable> rvs = myModel.getResponseVariables();
            for (ResponseVariable rv : rvs) {
                if (rv.getDefaultReportingOption()) {
                    StatisticAccessorIfc stat = rv.getAcrossReplicationStatistic();
                    myTextReport.println(stat);
                }
            }

            myTextReport.println();
            myTextReport.println();
            myTextReport.println("----------------------------------------------------------");
            myTextReport.println("Counter statistics:");
            myTextReport.println("----------------------------------------------------------");
            myTextReport.println();

            List<Counter> counters = myModel.getCounters();

            for (Counter c : counters) {
                if (c.getDefaultReportingOption()) {
                    StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
                    myTextReport.println(stat);
                }
            }
        }
    }
}
