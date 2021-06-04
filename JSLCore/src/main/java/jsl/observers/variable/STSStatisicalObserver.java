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

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.utilities.statistic.StandardizedTimeSeriesStatistic;
import jsl.utilities.statistic.Statistic;

/**
 *
 */
public class STSStatisicalObserver extends ModelElementObserver {

    protected StandardizedTimeSeriesStatistic myStatistic;

    protected Statistic myAcrossRepStat;

    protected ResponseVariable myResponseVariable;

    protected Model myModel;

    /**
     *
     */
    public STSStatisicalObserver() {
        this(StandardizedTimeSeriesStatistic.BATCH_SIZE, null);
    }

    /**
     * @param name the name of statistic
     */
    public STSStatisicalObserver(String name) {
        this(StandardizedTimeSeriesStatistic.BATCH_SIZE, name);
    }

    /**
     * @param batchSize the batch size
     * @param name the name
     */
    public STSStatisicalObserver(int batchSize, String name) {
        super(name);
        myStatistic = new StandardizedTimeSeriesStatistic(batchSize);
        myAcrossRepStat = new Statistic();
    }

    public void resetStatistics() {
        myStatistic.reset();
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        if (myStatistic != null) {
            sb.append("-------------------------------------------------\n");
            sb.append("STS Statistic:\n");
            sb.append("-------------------------------------------------\n");
            sb.append(myStatistic);
            sb.append("-------------------------------------------------\n");
            sb.append("\n");
        }

        if (myAcrossRepStat.getCount() >= 2.0) {
            sb.append("-------------------------------------------------\n");
            sb.append("Across Replication Statistic:\n");
            sb.append("-------------------------------------------------\n");
            sb.append(myAcrossRepStat);
            sb.append("-------------------------------------------------\n");
            sb.append("\n");
        }

        return (sb.toString());
    }

    protected void beforeExperiment(ModelElement m, Object arg) {
        myResponseVariable = (ResponseVariable) m;
        myModel = myResponseVariable.getModel();
        myStatistic.setName("STS Stat " + myResponseVariable.getStringLabel());
        myAcrossRepStat.setName("Across Rep Stat " + myResponseVariable.getStringLabel());
        myAcrossRepStat.reset();
        resetStatistics();
    }

    protected void beforeReplication(ModelElement m, Object arg) {
        resetStatistics();
    }

    protected void initialize(ModelElement m, Object arg) {
    }

    protected void warmUp(ModelElement m, Object arg) {
        resetStatistics();
    }

    protected void update(ModelElement m, Object arg) {
        myResponseVariable = (ResponseVariable) m;
        myModel = myResponseVariable.getModel();
        myStatistic.collect(myResponseVariable.getValue());
    }

    protected void afterReplication(ModelElement m, Object arg) {
        myAcrossRepStat.collect(myStatistic.getAverage());
    }

    protected void afterExperiment(ModelElement m, Object arg) {
//		JSL.out.println(toString());
    }

    protected void removedFromModel(ModelElement m, Object arg) {
        myStatistic = null;
        myAcrossRepStat = null;
        myResponseVariable = null;
        myModel = null;
    }
}
