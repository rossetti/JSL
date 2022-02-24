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

import jsl.utilities.random.rvariable.BetaRV;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class CashFlowExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UniformRV S = new UniformRV(90.0,100.0);
        NormalRV C = new NormalRV(100.0, 10.0*10.0);
        NormalRV Y = new NormalRV(300.0, 50.0*50.0);
        double[] values = {4.0, 5.0, 6.0, 7.0};
        double[] cdf = {0.3, 0.7, 0.8, 1.0};
        DEmpiricalRV N = new DEmpiricalRV(values, cdf);
        BetaRV b = new BetaRV(5.0, 1.5);
        Statistic npvStat = new Statistic("NPV");
        Statistic npvLT0Stat = new Statistic("P(NPV < 0)");
        int r = 1910;
        for(int j=1; j<=r; j++){
            double s = S.getValue();
            double c = C.getValue();
            double y = Y.getValue();
            int n = (int)N.getValue();
            double i = 0.06 + (0.09 - 0.06)*b.getValue();
            double p1 = getPGivenA(i, n, c);
            double p2 = getPGivenA(i, n, y);
            double p3 = getPGivenF(i, n, s);
            double npv = -800.0 - p1 + p2 + p3;
            npvStat.collect(npv);
            npvLT0Stat.collect(npv < 0);
        }
        System.out.println(npvStat);
        System.out.println(npvLT0Stat);
    }

    public static double getPGivenF(double i, int n, double f) {
        if (i <= -1.0) {
            throw new IllegalArgumentException("interest rate was <= -1");
        }

        if (n < 0) {
            throw new IllegalArgumentException("number of periods was < 0");
        }

        double d = Math.pow((1.0 + i), n);
        return f / d;
    }

    public static double getPGivenA(double i, int n, double a) {
        if (i <= -1.0) {
            throw new IllegalArgumentException("interest rate was <= -1");
        }

        if (n < 0) {
            throw new IllegalArgumentException("number of periods was < 0");
        }

        double d = Math.pow((1.0 + i), n);
        return a * ((d - 1.0) / (i * (d)));
    }
}
