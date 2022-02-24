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
package examples.general.utilities.statistics;

import jsl.utilities.random.rvariable.GammaRV;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.statistic.WeightedStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rossetti
 *
 */
public class TestStatistic {

    /**
     * @param args
     */
    public static void main(String[] args) {
        //test1();
        //test2();
        //test3();
        //test4();
        //test5();
        //test6();
        //test7();
        //test8();
    }

    public static void test1() {
        RVariableIfc r = new GammaRV(23.0, 9.0);

        double[] data = r.sample(10);
        for (double d : data) {
            System.out.println(d);
        }
        System.out.println();

        Statistic stat = new Statistic("Gamma data");
        stat.collect(data);
        System.out.println(stat);
        System.out.println(stat.getConfidenceInterval(0.95));

        System.out.println(stat.getCSVStatisticHeader());
        System.out.println(stat.getCSVStatistic());

        RVariableIfc normal = new NormalRV();

        Statistic stat2 = new Statistic("Normal data", normal.sample(10));

        List<StatisticAccessorIfc> list = new ArrayList<>();

        list.add(stat);
        list.add(stat2);

        StatisticReporter reporter = new StatisticReporter(list);

        System.out.println(reporter.getHalfWidthSummaryReport());
    }

    public static void test2() {
        RVariableIfc r = new GammaRV(23.0, 9.0);

        double[] data = r.sample(10);
        for (double d : data) {
            System.out.println(d);
        }

        System.out.println("Median = " + Statistic.getMedian(data));
        for (double d : data) {
            System.out.println(d);
        }
        System.out.println();

        Statistic stat = new Statistic();
        stat.setSaveOption(true);
        stat.collect(data);
        System.out.println(stat);
        System.out.println();
        StringBuilder sb = new StringBuilder();
        sb.append(stat.getCSVHeader());
        sb.append(stat.getCSVValues());
        System.out.println(sb);
        System.out.println();
        double[] x = stat.getSavedData();
        for (double d : x) {
            System.out.println(d);
        }

        //double[] y = {9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6,9,6};
        double[] y = {9, 6, 9};
        //double[] y = {47,64,23,71,38,64,55,41,59,48,71,35,57,40,58,44,80,55,37,74,51,57,50,60,45,57,50,45,25,59,50,71,56,74,50,58,45,54,36,54,48,55,45,57,50,62,44,64,43,52,38,59,55,41,53,49,34,35,54,45,68,38,50,60,39,59,40,57,54,23};
        Statistic yStat = new Statistic(y);
        System.out.println(yStat);

//        
//        double[] x = stat.getStatistics();
//        stat.getStatistics(x);
//        StringBuilder sb = new StringBuilder();
//        for(int i=0; i< x.length; i++){
//        if (Double.isNaN(x[i]) || Double.isInfinite(x[i]))
//        sb.append("");
//        else
//        sb.append(x[i]);
//        if (i < x.length - 1)
//        sb.append(",");
//        }
//
//        System.out.println(sb.toString());
    }

    public static void test3() {
        Statistic stat = new Statistic("test 3");
        WeightedStatistic ws = new WeightedStatistic("ws test 3");
        System.out.println(stat);
        System.out.println(ws);
        NormalRV n = new NormalRV();
        double x;
        for (int i = 1; i <= 10; i++) {
            if (i == 2) {
                x = ws.getMax();
            } else {
                x = n.getValue();
            }
            stat.collect(x);
            System.out.println("x = " + x);
        }
        System.out.println(stat);
    }

    public static void test8(){
        NormalRV n = new NormalRV(10, 2);
          WeightedStatistic ws = new WeightedStatistic("ws test 8");
          for(int i=1; i<=100;i++){
              ws.collect(n.getValue(), 1.0);
          }
          System.out.println(ws);
    }

}
