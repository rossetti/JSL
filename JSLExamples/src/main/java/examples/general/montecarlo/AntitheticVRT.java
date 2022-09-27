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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.montecarlo;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.RNStreamProvider;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticXY;

/**
 *
 * @author rossetti
 */
public class AntitheticVRT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        example1();

        example2();

        example3();

        example4();

    }

    public static void example1() {
        // estimating the mean of a rv via antithetic variates

        UniformRV nf = new UniformRV();

        //       Normal nf = new Normal(10, 2);
        Statistic s = new Statistic("Crude Estimator");
        StatisticXY sxy = new StatisticXY();

        int m = 1000; // number of antithetic pairs
        int n = 2 * m;  // number of samples

        // sample a total of n observations
        for (int i = 1; i <= n; i++) {
            s.collect(nf);
        }

        System.out.println(s);

        s.reset();
        s.setName("AV Estimator");
        double x = 0;
        double xa = 0;
        // sample a total of n observations but pair them
        for (int i = 1; i <= n; i++) {
            if ((i % 2) == 0) {
                // even number
                nf.resetStartSubStream();
                nf.setAntitheticOption(true);
                xa = nf.getValue();
                //System.out.println("\t xa = " + xa);
                s.collect((x + xa) / 2.0);
                sxy.collectXY(x, xa);
            } else {
                // odd number
                // there are so many substreams that advancing doesn't matter
                nf.advanceToNextSubStream();
                nf.setAntitheticOption(false);
                x = nf.getValue();
                //System.out.print("x = " + x);
            }
        }
        System.out.println("--------------------");
        System.out.println("Example 1");
        System.out.println(s);
        System.out.print(sxy);
    }

    public static void example2() {
        RNStreamProvider f1 = new RNStreamProvider();

        // get a stream
        RNStreamIfc s1 = f1.nextRNStream();

        System.out.println(s1);

        RNStreamProvider f2 = new RNStreamProvider();

        // get a stream
        RNStreamIfc s2 = f2.nextRNStream();

        s2.setAntitheticOption(true);

        System.out.println(s2);

        System.out.println("Both streams have same seed but are antithetic");

        int m = 1000; // number of antithetic pairs
        int n = 2 * m;  // number of samples

        // use the antithetic streams
        NormalRV nf1 = new NormalRV(10.0, 2.0, s1);
        NormalRV nf2 = new NormalRV(10.0, 2.0, s2);
        Statistic s = new Statistic("Antithetic");
        StatisticXY sxy = new StatisticXY();

        for (int i = 1; i <= m; i++) {
            double x = nf1.getValue();
            double xa = nf2.getValue();
            sxy.collectXY(x, xa);
            s.collect((x + xa) / 2.0);
        }
        System.out.println("--------------------");
        System.out.println("Example 2");
        System.out.println(s);
        System.out.println(sxy);
    }

    public static void example3() {
        // recall that you can just do inverse transform yourself

        UniformRV uf = new UniformRV();
        Normal nf = new Normal(10.0, 2.0);

        int m = 1000; // number of antithetic pairs
        int n = 2 * m;  // number of samples
        Statistic s = new Statistic("Antithetic");
        StatisticXY sxy = new StatisticXY();

        for (int i = 1; i <= m; i++) {
            double u = uf.getValue();
            double x = nf.invCDF(u);
            double xa = nf.invCDF(1.0 - u);
            sxy.collectXY(x, xa);
            s.collect((x + xa) / 2.0);
        }
        System.out.println("--------------------");
        System.out.println("Example 3");
        System.out.println(s);
        System.out.println(sxy);

    }

    public static void example4() {
        NormalRV nf = new NormalRV(10.0, 2.0);
        RVariableIfc nfa = nf.newAntitheticInstance();

        int m = 1000; // number of antithetic pairs
        int n = 2 * m;  // number of samples
        Statistic s = new Statistic("Antithetic");
        StatisticXY sxy = new StatisticXY();

        for (int i = 1; i <= m; i++) {
            double x = nf.getValue();
            double xa = nfa.getValue();
            sxy.collectXY(x, xa);
            s.collect((x + xa) / 2.0);
        }
        System.out.println("--------------------");
        System.out.println("Example 4");
        System.out.println(s);
        System.out.println(sxy);
    }

}
