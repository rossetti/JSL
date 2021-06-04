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
import java.util.*;

import jsl.observers.ObserverIfc;
import jsl.simulation.IterativeProcess;
import jsl.utilities.reporting.TextReport;
import jsl.utilities.IdentityIfc;

public class IPLogReport implements ObserverIfc {

    private final TextReport myTextReport;

    public IPLogReport(Path pathToFile) {
        myTextReport = new TextReport(pathToFile);
    }

    public void update(Object observable, Object obj) {
        IterativeProcess ip = (IterativeProcess) observable;
        IdentityIfc id = (IdentityIfc) obj;

        if (ip.isInitialized()) {
            myTextReport.println(ip.getName() + " initialized at " + new Date());
        }

        if (ip.isStepCompleted()) {
            myTextReport.println(ip.getName() + "  completed " + id.getName());
        }

        if (ip.isEnded()) {
            myTextReport.println(ip.getName() + " ended at " + new Date());
            myTextReport.print("\t");
            if (ip.allStepsCompleted()) {
                myTextReport.println(ip.getName() + " completed all steps.");
            }

            if (ip.executionTimeExceeded()) {
                myTextReport.println(ip.getName() + " timed out.");
            }

            if (ip.stoppedByCondition()) {
                myTextReport.println(ip.getName() + " ended due to end condition being met.");
            }

            if (ip.isUnfinished()) {
                myTextReport.println(ip.getName() + " ended due to user.");
            }
        }
        myTextReport.print("\tCurrent state ");
        myTextReport.print(ip.getCurrentStateAsString());
        myTextReport.print("\t\tEnding State Indicator: ");
        myTextReport.println(ip.getEndingStateIndicatorAsString());;
        if (ip.getStoppingMessage()!= null) {
            myTextReport.print("\t\tStopping Message: ");
            myTextReport.println(ip.getStoppingMessage());
        }
    }
}
