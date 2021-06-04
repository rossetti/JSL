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

import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.simulation.Simulation;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.WeightedStatisticIfc;

/**
 * The purpose of this class is to observe response variables in order to form
 * pairs across replications and to compute statistics across the pairs.
 *
 * If Y(1), Y(2), .., Y(j), .., Y(n) represent observations from the jth
 * replication, then this class averages adjacent pairs
 *
 * X(1) = (Y(1)+Y(2))/2, X(2) = (Y(3)+Y(4))/2, for m = floor(n/2) pairs
 *
 * X(i) = (Y(2j-1) + Y(2j))/2 for j = 1, 2, 3, ... floor(n/2)
 *
 * If the experiment has been set to control the streams using antithetic
 * streams for odd and even replications, then the resulting estimate from this
 * class will implement the variance reduction technique called antithetic
 * variates.
 *
 * The class is designed as an observer that can be attached to individual
 * response variables
 *
 * @author rossetti
 */
public class AntitheticEstimator extends ModelElementObserver {

    private Statistic myStat;

    private double myOdd;

    private ResponseVariable myResponse;

    private ExperimentGetIfc myExp;

    public AntitheticEstimator() {
        this(null);
    }

    public AntitheticEstimator(String name) {
        super(name);
        myStat = new Statistic();
    }

    /**
     * Returns the experiment for the simulation
     *
     * @return the experiment
     */
    protected final ExperimentGetIfc getExperiment() {
        return myExp;
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        myResponse = (ResponseVariable) m;
        myExp = m.getExperiment();
        myStat.setName("Antithetic Estimator for " + myResponse.getName());
        if (myExp.getAntitheticOption() != true) {
            StringBuilder sb = new StringBuilder();
            sb.append("The antithetic option is not on. \n");
            sb.append("And there were AntitheticEstimator instances used.");
            Simulation.LOGGER.warn(sb.toString());
        }
    }

    @Override
    protected void replicationEnded(ModelElement m, Object arg) {
        if ((getExperiment().getCurrentReplicationNumber() % 2) == 0) {
            // get the even replication average
            WeightedStatisticIfc s = myResponse.getWithinReplicationStatistic();
            double myEven = s.getAverage();
            // collect the average of the pair
            myStat.collect((myEven + myOdd)/2.0);
        } else {
            // remember the odd replication
            WeightedStatisticIfc s = myResponse.getWithinReplicationStatistic();
            myOdd = s.getAverage();
        }
    }
    
    public final Statistic getStatistic(){
        return myStat.newInstance();
    }
}
