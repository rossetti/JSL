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
package examples.general.variables.nhpp;

import jsl.modeling.elements.variable.nhpp.PiecewiseConstantRateFunction;
import jsl.modeling.elements.variable.nhpp.PiecewiseLinearRateFunction;
import jsl.modeling.elements.variable.nhpp.PiecewiseRateFunction;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *
 * @author rossetti
 */
public class TestRateFunctions {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//       testPiecewiseConstantRateFunction1();
//        testPiecewiseConstantRateFunction2();
//        testPiecewiseConstantRateFunction3();
//        testPiecewiseConstantRateFunction4();
//        testPiecewiseConstantRateFunction5();
        testPiecewiseLinearRateFunction1();
//        testPiecewiseLinearRateFunction2();
    }

    public static void testPiecewiseLinearRateFunction1() {
        PiecewiseLinearRateFunction f = new PiecewiseLinearRateFunction(0.5, 200.0, 0.5);

        f.addRateSegment(400.0, 0.9);
        f.addRateSegment(400.0, 0.9);
        f.addRateSegment(200.0, 1.2);
        f.addRateSegment(300.0, 0.9);
        f.addRateSegment(500.0, 0.5);

        System.out.println("Rates");
        double[] rates = f.getRates();
        for (double rr : rates) {
            System.out.println("rate = " + rr);
        }

        System.out.println("Durations");
        double[] durations = f.getDurations();
        for (double rr : durations) {
            System.out.println("duration = " + rr);
        }

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
        System.out.println("rate function");
        double mt = f.getTimeRangeUpperLimit();
        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("rate(" + t + ")= " + f.getRate(t));
        }
        System.out.println("-----");
        System.out.println("cumulative rate function");
        // check the cumulative rate function

        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("cum rate(" + t + ")= " + f.getCumulativeRate(t));
        }
        System.out.println("-----");
        System.out.println("inverse cumulative rate function");
        // check the cumulative rate function
        double mr = f.getCumulativeRateRangeUpperLimit();
        for (double r = 0.0; r <= mr; r = r + 1) {
            System.out.println("inv cum rate(" + r + ")= " + f.getInverseCumulativeRate(r));
        }
    }

    public static void testPiecewiseLinearRateFunction2() {

        double[] r = {0.5, 0.5, 0.9, 0.9, 1.2, 0.9, 0.5};
        double[] d = {200.0, 400, 400, 200, 300, 500};

        PiecewiseLinearRateFunction f = new PiecewiseLinearRateFunction(d, r);

        System.out.println("Rates");
        double[] rates = f.getRates();
        for (double rr : rates) {
            System.out.println("rate = " + rr);
        }

        System.out.println("Durations");
        double[] durations = f.getDurations();
        for (double rr : durations) {
            System.out.println("duration = " + rr);
        }

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
//        System.out.println("rate function");
//        double mt = f.getTimeRangeUpperLimit();
//        for (double t = 0.0; t < mt; t = t + 1) {
//            System.out.println("rate(" + t + ")= " + f.getRate(t));
//        }
//        System.out.println("-----");
//        System.out.println("cumulative rate function");
//        // check the cumulative rate function
//
//        for (double t = 0.0; t < mt; t = t + 1) {
//            System.out.println("cum rate(" + t + ")= " + f.getCumulativeRate(t));
//        }
//        System.out.println("-----");
//        System.out.println("inverse cumulative rate function");
//        // check the cumulative rate function
//        double mr = f.getCumulativeRateRangeUpperLimit();
//        for (double rr = 0.0; rr <= mr; rr = rr + 1) {
//            System.out.println("inv cum rate(" + rr + ")= " + f.getInverseCumulativeRate(rr));
//        }
    }

    public static void testPiecewiseConstantRateFunction1() {
        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 2.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
        System.out.println("rate function");
        double mt = f.getTimeRangeUpperLimit();
        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("rate(" + t + ")= " + f.getRate(t));
        }
        System.out.println("-----");
        System.out.println("cumulative rate function");
        // check the cumulative rate function

        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("cum rate(" + t + ")= " + f.getCumulativeRate(t));
        }
        System.out.println("-----");
        System.out.println("inverse cumulative rate function");
        // check the cumulative rate function
        double mr = f.getCumulativeRateRangeUpperLimit();
        for (double r = 0.0; r <= mr; r = r + 1) {
            System.out.println("inv cum rate(" + r + ")= " + f.getInverseCumulativeRate(r));
        }
    }

    public static void testPiecewiseConstantRateFunction2() {
        PiecewiseRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 0.0);
        f.addRateSegment(15.0, 1.0);
        f.addRateSegment(20.0, 0.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
        System.out.println("rate function");
        double mt = f.getTimeRangeUpperLimit();
        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("rate(" + t + ")= " + f.getRate(t));
        }
        System.out.println("-----");
        System.out.println("cumulative rate function");
        // check the cumulative rate function

        for (double t = 0.0; t < mt; t = t + 1) {
            System.out.println("cum rate(" + t + ")= " + f.getCumulativeRate(t));
        }
        System.out.println("-----");
        System.out.println("inverse cumulative rate function");
        // check the cumulative rate function
        double mr = f.getCumulativeRateRangeUpperLimit();
        for (double r = 0.0; r <= mr; r = r + 1) {
            System.out.println("inv cum rate(" + r + ")= " + f.getInverseCumulativeRate(r));
        }
    }

    public static void testPiecewiseConstantRateFunction3() {
        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 2.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
        System.out.println("Generate non-stationary Poisson process.");
        double a = 0.0;
        double y = 0.0;
        double tau = f.getCumulativeRateRangeUpperLimit();
        ExponentialRV e = new ExponentialRV(1.0);
        int n = 0;
        y = y + e.getValue();
        while (y < tau) {
            a = f.getInverseCumulativeRate(y);
            n++;
            System.out.println("a[" + n + "] = " + a);
            y = y + e.getValue();
        }
    }

    public static void testPiecewiseConstantRateFunction4() {
        double[] d = {15.0, 20.0, 15.0};
        double[] r = {1.0, 2.0, 1.0};

        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(d, r);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        // check the rate function
        System.out.println("-----");
        System.out.println("Generate non-stationary Poisson process.");
        double a = 0.0;
        double y = 0.0;
        double tau = f.getCumulativeRateRangeUpperLimit();
        ExponentialRV e = new ExponentialRV(1.0);
        int n = 0;
        y = y + e.getValue();
        while (y < tau) {
            a = f.getInverseCumulativeRate(y);
            n++;
            System.out.println("a[" + n + "] = " + a);
            y = y + e.getValue();
        }

    }

    public static void testPiecewiseConstantRateFunction5() {
        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 2.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        System.out.println("Rates");
        double[] rates = f.getRates();
        for (double r : rates) {
            System.out.println("rate = " + r);
        }
        System.out.println("-----");

        f.multiplyRates(2.0);
        System.out.println("After multiplication by 2");
        System.out.println(f);
        System.out.println("Rates");
        for (double r : f.getRates()) {
            System.out.println("rate = " + r);
        }

    }
}
