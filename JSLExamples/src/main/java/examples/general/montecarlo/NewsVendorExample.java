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
package examples.general.montecarlo;

import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class NewsVendorExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double q = 30; // order qty
        double s = 0.25; //sales price
        double c = 0.15; // unit cost
        double u = 0.02; //salvage value
        double[] values = {5, 10, 40, 45, 50, 55, 60};
        double[] cdf = {0.1, 0.3, 0.6, 0.8, 0.9, 0.95, 1.0};
        DEmpiricalRV dCDF = new DEmpiricalRV(values, cdf);
        Statistic stat = new Statistic("Profit");
        double n = 1994; // sampel size
        for (int i = 1; i <= n; i++) {
            double d = dCDF.getValue();
            double amtSold = Math.min(d, q);
            double amtLeft = Math.max(0, q - d);
            double g = s * amtSold + u * amtLeft - c * q;
            stat.collect(g);
        }
        System.out.printf("%s \t %f %n", "Count = ", stat.getCount());
        System.out.printf("%s \t %f %n", "Average = ", stat.getAverage());
        System.out.printf("%s \t %f %n", "Std. Dev. = ", stat.getStandardDeviation());
        System.out.printf("%s \t %f %n", "Half-width = ", stat.getHalfWidth());
        System.out.println(stat.getConfidenceLevel() * 100 + "% CI = " + stat.getConfidenceInterval());
    }

}
