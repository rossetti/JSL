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

/**
 *
 */
package examples.general.montecarlo;

import jsl.utilities.JSLFileUtil;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.AbstractStatistic;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.StandardizedTimeSeriesStatistic;
import jsl.utilities.statistic.Statistic;

import java.io.PrintWriter;

/**
 * @author rossetti
 *
 */
public class LindleyEquation {

    /**
     * @param args
     */
    public static void main(String[] args) {

//		replicationDeletionExample();

//        oneLongRunExample();
        
        controlVariateExample();

    }

    public static void replicationDeletionExample() {
        // inter-arrival time distribution
        RandomIfc y = new ExponentialRV(1.0);
        // service time distribution
        RandomIfc x = new ExponentialRV(0.7);
        int r = 30; // number of replications
        int n = 100000; // number of customers
        int d = 10000; // warm up
        Statistic avgw = new Statistic("Across rep avg waiting time");
        Statistic avgpw = new Statistic("Across rep prob of wait");
        Statistic wbar = new Statistic("Within rep avg waiting time");
        Statistic pw = new Statistic("Within rep prob of wait");
        for (int i = 1; i <= r; i++) {
            double w = 0; // initial waiting time
            for (int j = 1; j <= n; j++) {
                w = Math.max(0.0, w + x.getValue() - y.getValue());
                wbar.collect(w);// collect waiting time
                pw.collect((w > 0.0)); // collect P(W>0)
                if (j == d) {// clear stats at warmup
                    wbar.reset();
                    pw.reset();
                }
            }
            //collect across replication statistics
            avgw.collect(wbar.getAverage());
            avgpw.collect(pw.getAverage());
            // clear within replication statistics for next rep
            wbar.reset();
            pw.reset();
        }
        System.out.println("Replication/Deletion Lindley Equation Example");
        System.out.println(avgw);
        System.out.println(avgpw);
    }

    public static void controlVariateExample() {
        PrintWriter out = JSLFileUtil.makePrintWriter("controlVariateOut.csv");
        // inter-arrival time distribution
        ExponentialRV y = new ExponentialRV(1.0);
        // service time distribution
        ExponentialRV x = new ExponentialRV(0.7);
        int r = 30; // number of replications
        int n = 10000; // number of customers
        int d = 5000; // warm up
        Statistic avgw = new Statistic("Across rep avg waiting time");
        Statistic avgpw = new Statistic("Across rep prob of wait");
        Statistic wbar = new Statistic("Within rep avg waiting time");
        Statistic pw = new Statistic("Within rep prob of wait");
        Statistic xStat = new Statistic("Service Time");
        Statistic yStat = new Statistic("TBA Time");
        for (int i = 1; i <= r; i++) {
            double w = 0; // initial waiting time
            for (int j = 1; j <= n; j++) {
                double xv = x.getValue();
                double yv = y.getValue();
                w = Math.max(0.0, w + xv - yv);
                wbar.collect(w);// collect waiting time
                xStat.collect(xv);
                yStat.collect(yv);
                pw.collect((w > 0.0)); // collect P(W>0)
                if (j == d) {// clear stats at warmup
                    wbar.reset();
                    pw.reset();
                    xStat.reset();
                    yStat.reset();
                }
            }
            //collect across replication statistics
            avgw.collect(wbar.getAverage());
            avgpw.collect(pw.getAverage());
            writeCSV(out, wbar.getAverage(), xStat.getAverage() - x.getMean(), yStat.getAverage() - y.getMean());
            // clear within replication statistics for next rep
            wbar.reset();
            pw.reset();
            xStat.reset();
            yStat.reset();
        }
        System.out.println("Replication/Deletion Lindley Equation Example");
        System.out.println(avgw);
        System.out.println(avgpw);
    }
    
    private static void writeCSV(PrintWriter out, double y, double x1, double x2){
        out.print(y);
        out.print(",");
        out.print(x1);
        out.print(",");
        out.print(x2);
        out.println();
    }

    public static void oneLongRunExample() {
        // inter-arrival time distribution
        RandomIfc y = new ExponentialRV(1.0);
        // service time distribution
        RandomIfc x = new ExponentialRV(0.7);
        int n = 100000; // number of customers
        int d = 10000; // warm up
        AbstractStatistic wbar = new BatchStatistic("Batch waiting time");
        AbstractStatistic wbarSTS = new StandardizedTimeSeriesStatistic("STS waiting time");
        double w = 0; // initial waiting time
        for (int j = 1; j <= n; j++) {
            w = Math.max(0.0, w + x.getValue() - y.getValue());
            wbar.collect(w);// collect waiting time
            wbarSTS.collect(w);
            if (j == d) {// clear stats at warmup
                wbar.reset();
                wbarSTS.reset();
            }
        }
        System.out.println("One long Run Lindley Equation Example");
        System.out.println(wbar);
        System.out.println(wbarSTS);
    }
}
