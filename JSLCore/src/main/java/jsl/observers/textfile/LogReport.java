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
import java.text.DecimalFormat;

import jsl.observers.ObserverIfc;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.utilities.reporting.TextReport;

public class LogReport implements ObserverIfc {

    private final DecimalFormat df = new DecimalFormat("0.###");

    private boolean myTimedUpdateLogFlag = false;

    private final TextReport myTextReport;

    public LogReport(Path pathToFile) {
        myTextReport = new TextReport(pathToFile);
        myTextReport.addFileNameAndDate();
    }

    public final void turnOnTimedUpdateLogging() {
        myTimedUpdateLogFlag = true;
    }

    public final void turnOffTimedUpdateLogging() {
        myTimedUpdateLogFlag = false;
    }

    public void update(Object subject, Object arg) {
        ModelElement m = (ModelElement) subject;

        if (m.checkForBeforeExperiment()) {
            myTextReport.println("Before experiment for " + m.getClass().getName() + " " + m.getName());
        }

        if (m.checkForInitialize()) {
            myTextReport.println("Initialize for " + m.getClass().getName() + " " + m.getName());
        }

        if (m.checkForBeforeReplication()) {
            Simulation s = m.getSimulation();
            myTextReport.println("Before Replication " + s.getCurrentReplicationNumber() + " for " + m.getClass().getName() + " " + m.getName());

        }

        if (m.checkForMonteCarlo()) {
            myTextReport.println("Monte Carlo for " + m.getClass().getName() + " " + m.getName());
        }

        if (myTimedUpdateLogFlag == true) {
            if (m.checkForTimedUpdate()) {
                myTextReport.println("Timed update for " + m.getClass().getName() + " " + m.getName() + " at time " + m.getTime());
            }
        }

        if (m.checkForWarmUp()) {
            myTextReport.println("Warm up for " + m.getClass().getName() + " " + m.getName() + " at time " + m.getTime());
        }

        if (m.checkForReplicationEnded()) {
            Simulation s = m.getSimulation();
            myTextReport.println("Replication ended " + s.getCurrentReplicationNumber() + " for " + m.getClass().getName() + " " + m.getName() + " at time " + m.getTime());
        }

        if (m.checkForAfterReplication()) {
            Simulation s = m.getSimulation();
            myTextReport.println("After Replication " + s.getCurrentReplicationNumber() + " for " + m.getClass().getName() + " " + m.getName() + " at time " + m.getTime());
        }

        if (m.checkForAfterExperiment()) {
            myTextReport.println("After experiment for " + m.getClass().getName() + " " + m.getName() + " at time " + m.getTime());
        }

    }
}
