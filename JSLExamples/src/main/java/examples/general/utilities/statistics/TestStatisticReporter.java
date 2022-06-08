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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.utilities.statistics;

import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.reporting.JSL;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rossetti
 */
public class TestStatisticReporter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RVariableIfc n = new NormalRV();
        Statistic s1 = new Statistic("s1");
        Statistic s2 = new Statistic("s2 blah");
        s1.collect(n.sample(100));
        s2.collect(n.sample(200));
        List<StatisticAccessorIfc> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
        StatisticReporter r = new StatisticReporter(list);
        System.out.println(r.getHalfWidthSummaryReport(0.95));
        System.out.println(r.getSummaryReport());
        //r.setDecimalPlaces(2);
        r.setNameFieldSize(r.findSizeOfLongestName() + 5);
        System.out.println(r.getHalfWidthSummaryReport());
        System.out.println(r.findSizeOfLongestName());
        System.out.println(r.getSummaryReportAsLaTeXTabular(5));
        System.out.println(r.getCSVStatistics());

        PrintWriter out = JSL.getInstance().makePrintWriter("Report.md");
        out.print(r.getHalfWidthSummaryReportAsMarkDown());
        out.flush();

//        StringBuilder sb = r.myRowFormat;
//        for (int i=0; i< sb.length(); i++){
//            System.out.println("i = " + i + " char = " + sb.charAt(i));
//        }
    }
    
}
