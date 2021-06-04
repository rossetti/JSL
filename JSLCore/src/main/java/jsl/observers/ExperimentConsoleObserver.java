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
package jsl.observers;

import java.util.*;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Simulation;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 */
public class ExperimentConsoleObserver implements ObserverIfc {

    private Simulation mySim;
    protected boolean myRepUpdateFlag = false;

    public ExperimentConsoleObserver(Simulation sim) {
        this(false);
        mySim = sim;
    }

    /**
     * @param repUpdateFlag 
     */
    public ExperimentConsoleObserver(boolean repUpdateFlag) {
        myRepUpdateFlag = repUpdateFlag;
    }

    public final void setReplicationOutputFlag(boolean flag) {
        myRepUpdateFlag = flag;
    }

    public final boolean getReplicationOutputFlag() {
        return (myRepUpdateFlag);
    }

    @Override
    public void update(Object simulation, Object arg1) {
        Simulation sim = mySim;

        if (sim.isInitialized()) {
            System.out.println("Simulation: " + sim.getName() + " initialized.");
        }

        if (myRepUpdateFlag == true) {
            if (sim.isStepCompleted()) {
                System.out.println("Experiment: " + sim.getName() + " Replication: " + sim.getCurrentReplicationNumber() + " Completed");
            }
        }

        if (sim.isEnded()) {
            ExperimentGetIfc e = sim.getExperiment();
            System.out.println(e);
            System.out.println("Number of replications: " + e.getNumberOfReplications());
            System.out.println("Run length for each replication: " + e.getLengthOfReplication());
            System.out.println("Warm up for each replication: " + e.getLengthOfWarmUp());
            long et = sim.getMaximumAllowedExecutionTime();
            if (et == 0) {
                System.out.println("Maximum allowed experiment execution time not specified.");
            } else {
                System.out.println("Maximum allowed experiment execution time: " + et + " milliseconds.");
            }

            System.out.println();
            long t = sim.getEndExecutionTime() - sim.getBeginExecutionTime();
            System.out.println("The total time was approximately " + (t / 1000.0) + " seconds");

            if (sim.allStepsCompleted()) {
                System.out.println("The experiment ran all replications.");
            }

            if (sim.executionTimeExceeded()) {
                System.out.println("The experiment timed out.");
            }

            if (sim.stoppedByCondition()) {
                System.out.println("The experiment because it was stopped by the user.");
                System.out.println();
            }

            if (sim.isUnfinished()) {
                System.out.println("The experiment was ended early.");
            }

            
            List<ResponseVariable> rvs = sim.getModel().getResponseVariables();

            if (!rvs.isEmpty()) {
                System.out.println();
                System.out.println();
                System.out.println("----------------------------------------------------------");
                System.out.println("Across Replication statistics");
                System.out.println("----------------------------------------------------------");
                System.out.println();
                for (ResponseVariable rv : rvs) {
                    if (rv.getDefaultReportingOption()) {
                        StatisticAccessorIfc stat = rv.getAcrossReplicationStatistic();
                        System.out.println(stat);
                    }
                }
            }

            List<Counter> counters = sim.getModel().getCounters();

            if (!counters.isEmpty()) {
                System.out.println();
                System.out.println("----------------------------------------------------------");
                System.out.println("Counter statistics:");
                System.out.println("----------------------------------------------------------");
                System.out.println();

                for (Counter c : counters) {
                    if (c.getDefaultReportingOption()) {
                        StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
                        System.out.println(stat);
                    }
                }
            }

        }

    }
}
