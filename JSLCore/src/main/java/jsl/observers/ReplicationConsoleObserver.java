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

import jsl.simulation.Executive;
import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Simulation;

/**
 *  Can be attached to a Simulation to have updates to System.out
 *  during replications
 * 
 * @author rossetti
 */
public class ReplicationConsoleObserver implements ObserverIfc {
    private Simulation mySim;
    
    public ReplicationConsoleObserver(Simulation sim) {
        mySim = sim;
    }

    @Override
    public void update(Object observable, Object obj) {
        Simulation sim = mySim;

        if (sim.isStepCompleted()) {
            System.out.println();
            System.out.println("Simulation name: " + sim.getName());
            System.out.println("Completed Replication: " + sim.getCurrentReplicationNumber() + " of " + sim.getNumberOfReplications());
            Executive exec = sim.getExecutive();
            ExperimentGetIfc ep = sim.getExperiment();

            if (exec.isEnded()) {

                System.out.println("Planned Run length for replication: " + ep.getLengthOfReplication());
                System.out.println("Warm up for replication: " + ep.getLengthOfWarmUp());
                System.out.println("The actual run length for replication: " + exec.getTime());
                long et = exec.getMaximumAllowedExecutionTime();
                if (et == 0) {
                    System.out.println("Maximum allowed execution time not specified.");
                } else {
                    System.out.println("Maximum allowed execution time: " + et + " milliseconds.");
                }
                System.out.println();
                long t = exec.getEndExecutionTime() - exec.getBeginExecutionTime();
                System.out.println("The total execution time was approximately " + t / 1000.0 + " seconds");

                if (exec.isCompleted()) {
                    System.out.println("The replication ran all its events.");
                }

                if (exec.isTimedOut()) {
                    System.out.println("The replication timed out.");
                }

                if (exec.isEndConditionMet()) {
                    System.out.println("The replication ended because its end condition was met.");
                }

                if (exec.isUnfinished()) {
                    System.out.println("The replication was ended early.");
                }

                System.out.println();
            }
        }
    }
}
