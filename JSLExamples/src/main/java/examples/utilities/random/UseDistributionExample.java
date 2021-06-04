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
package examples.utilities.random;

import jsl.utilities.distributions.Binomial;
import jsl.utilities.distributions.DUniform;
import jsl.utilities.distributions.Normal;
import jsl.utilities.distributions.Uniform;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.RNStreamProvider;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 *
 * @author rossetti
 */
public class UseDistributionExample {

    public static void main(String[] args) {

       // binomialExample();
        longExample();
        //testNormal();
    }

    public static void binomialExample(){
        // make and use a Binomial(p, n) distribution
        int n = 10;
        double p = 0.8;
        System.out.println("n = " + n);
        System.out.println("p = " + p);
        Binomial bnDF = new Binomial(p, n);
        System.out.println("mean = " + bnDF.getMean());
        System.out.println("variance = " + bnDF.getVariance());
        // compute some values
        System.out.printf("%3s %15s %15s %n", "k", "p(k)", "cdf(k)");
        for (int i = 0; i <= 10; i++) {
            System.out.printf("%3d %15.10f %15.10f %n", i, bnDF.pmf(i), bnDF.cdf(i));
        }
        System.out.println();
        // change the probability and number of trials
        bnDF.setParameters(0.5, 20);
        System.out.println("mean = " + bnDF.getMean());
        System.out.println("variance = " + bnDF.getVariance());
        // make random variables based on the distributions
        RVariableIfc brv = bnDF.getRandomVariable();
        System.out.printf("%3s %15s %n", "n", "Values");
        // generate some values
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            int x = (int)brv.getValue();
            System.out.printf("%3d %15d %n", i+1, x);
        }
    }

    public static void longExample(){
        //create the distributions
        // make and use a Uniform(a, b) distribution
        Uniform uDF = new Uniform(10.0, 20.0);
        // make and use a DUniform(a, b) distribution
        DUniform duDF = new DUniform(5, 10);
        // make and use a Binomial(p, n) distribution
        Binomial bnDF = new Binomial(0.8, 10);
        // make and use a Normal(mean, var) distribution
        Normal nDF = new Normal(20, 5.0);
        System.out.printf("%10s %10s %10s %10s %10s\n", "i", "u",
                "du", "bn", "n");

        // make random variables based on the distributions
        RVariableIfc urv = uDF.getRandomVariable();
        RVariableIfc drv = duDF.getRandomVariable();
        RVariableIfc brv = bnDF.getRandomVariable();
        RVariableIfc nrv = nDF.getRandomVariable();

        for (int i = 1; i <= 5; i++) {
            double u = urv.getValue();
            double du = drv.getValue();
            double bn = brv.getValue();
            double n = nrv.getValue();
            System.out.printf("%10d %10.4f %10.1f %10.1f %10.3f\n", i, u,
                    du, bn, n);
        }

        // reset some streams and run again
        brv.resetStartStream();
        nrv.resetStartStream();
        System.out.println();
        System.out.printf("%10s %10s %10s %10s %10s\n", "i", "u",
                "du", "bn", "n");
        for (int i = 1; i <= 5; i++) {
            double u = urv.getValue();
            double du = drv.getValue();
            double bn = brv.getValue();
            double n = nrv.getValue();
            System.out.printf("%10d %10.4f %10.1f %10.1f %10.3f\n", i, u,
                    du, bn, n);
        }

        // changing a distributions parameters
        nDF.setMean(100.0);
        nDF.setVariance(4.0);
        double[] param = new double[2];
        param[0] = 50.0;
        param[1] = 100.0;
        uDF.setParameters(param);
        System.out.println();
        System.out.printf("%10s %10s %10s\n", "i", "u", "n");

        // get new random variables based on the changed distributions
        urv = uDF.getRandomVariable();
        nrv = nDF.getRandomVariable();
        for (int i = 1; i <= 5; i++) {
            double u = urv.getValue();
            double n = nrv.getValue();
            System.out.printf("%10d %10.3f %10.3f\n", i, u, n);
        }

        //illustrate fine stream control
        // make a provider for creating streams
        RNStreamProvider f1 = new RNStreamProvider();

        // get the first stream from the provider
        RNStreamIfc f1s1 = f1.nextRNStream();

        // make another provider, the providers are identical
        RNStreamProvider f2 = new RNStreamProvider();

        // thus the first streams returned are identical
        RNStreamIfc f2s1 = f2.nextRNStream();

        // now tell the stream to produce antithetic random numbers
        // f2s1 and f1s1 are now antithetic to each other
        f2s1.setAntitheticOption(true);
        System.out.println();
        System.out.printf("%10s %10s %10s %10s\n", "i", "u1", "u2", "u1 + u2");
        for (int i = 1; i <= 10; i++) {
            double u1 = f1s1.randU01();
            double u2 = f2s1.randU01();
            System.out.printf("%10d %10.3f %10.3f %10.3f\n", i, u1, u2, u1+u2);
        }

        // just directly make random variables based on the streams
        // set up the normals to use the antithetics
        NormalRV n1 = new NormalRV(20, 4, f1s1);
        NormalRV n2 = new NormalRV(20, 4, f2s1);
        System.out.println();
        System.out.printf("%10s %10s %10s\n", "i", "n1", "n2");
        for (int i = 1; i <= 10; i++) {
            double x = n1.getValue();
            double y = n2.getValue();
            System.out.printf("%10d %10.3f %10.3f\n", i, x, y);
        }

    }

    public static void testNormal(){
        System.out.println(Normal.stdNormalCDF(7.0));
    }
}
