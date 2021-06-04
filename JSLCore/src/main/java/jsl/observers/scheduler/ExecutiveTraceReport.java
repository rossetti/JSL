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
package jsl.observers.scheduler;

import jsl.simulation.Executive;
import jsl.simulation.JSLEvent;

import jsl.utilities.reporting.TextReport;

import java.nio.file.Path;
import java.text.DecimalFormat;
import jsl.observers.ObserverIfc;

public class ExecutiveTraceReport implements ObserverIfc {

    DecimalFormat df = new DecimalFormat("0.###");

    private double myOffTime = Double.POSITIVE_INFINITY;

    private final TextReport myTextReport;

    public ExecutiveTraceReport(Path pathToFile) {
        myTextReport = new TextReport(pathToFile);
        myTextReport.addFileNameAndDate();
    }

    @Override
    public void update(Object subject, Object arg) {
        Executive executive = (Executive) subject;

        if (executive.getObserverState() == Executive.INITIALIZED) {
            myTextReport.println("Executive: Initialized. Time = " + df.format(executive.getTime()));
        }

        if (executive.getObserverState() == Executive.AFTER_EXECUTION) {
            myTextReport.println("Executive: After executing all events. Time = " + df.format(executive.getTime()));
        }

        if (executive.getObserverState() == Executive.AFTER_EVENT) {
            if (executive.getTime() > myOffTime) {
                return;
            }
            JSLEvent event = (JSLEvent) arg;

            myTextReport.print("\t");
            myTextReport.print(df.format(event.getTime()));
            myTextReport.print("\t");
            myTextReport.print(event.getId());
            myTextReport.print("\t");
            myTextReport.print(event.getName());
            myTextReport.print("\t");
            myTextReport.print(event.getPriority());
            myTextReport.print("\t");
            myTextReport.print(event.getType());
            //        print("\t");
            //        print(event.getListener());
            //        print("\t");
            myTextReport.println();
        }

    }

    /** Don't trace after the supplied time
     *
     * @param time
     */
    public final void setOffTime(double time){
        myOffTime = time;
    }

    /** The time that the trace will stop tracing events
     * @return The time that the trace will stop tracing events
     */
    public final double getOffTime(){
        return myOffTime;
    }
}
