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
package jsl.utilities.random.rng;

import jsl.utilities.distributions.Normal;

import java.util.Objects;

/**
 * This class computes some tests on random numbers
 */
public class RNGTEST {

    public static final double[][] a = {
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 4529.4, 9044.9, 13568, 18091, 22615, 27892},
        {0.0, 9044.9, 18097, 27139, 36187, 45234, 55789},
        {0.0, 13568, 27139, 40721, 54281, 67852, 83685},
        {0.0, 18091, 36187, 54281, 72414, 90470, 111580},
        {0.0, 22615, 45234, 67852, 90470, 113262, 139476},
        {0.0, 27892, 55789, 83685, 111580, 139476, 172860}
    };

    public static final double[] b = {0.0, 1. / 6., 5. / 24., 11. / 120., 19. / 720., 29. / 5040., 1. / 840.};

    /** Use for large degrees of freedom
     *
     * @param df degrees of freedom
     * @param confidenceLevel the confidence level for the statistical test
     * @return the approximate chi-squared value
     */
    public static double approxChiSQValue(int df, double confidenceLevel) {
        if (df <= 0) {
            throw new IllegalArgumentException("The degrees of freedom must be > 0");
        }
        if ((confidenceLevel <= 0) || (confidenceLevel >= 1)) {
            throw new IllegalArgumentException("The confidence level must be (0,1)");
        }
        double z = Normal.stdNormalInvCDF(1.0 - confidenceLevel);
        double t2 = 2.0 / (9 * df);
        double t1 = z * Math.sqrt(t2);
        double t3 = (1.0 - t2 - t1);
        double x = df * t3 * t3 * t3;
        return (x);
    }

    /** Performs 1-D chi-squared test
     *
     * @param rng the thing that produces U(0,1) numbers, must not null
     * @param n number of random numbers to test
     * @param k the number of intervals in the test
     * @return the chi-squared test statistic
     */
    public static double chiSquaredTest(RandU01Ifc rng, long n, int k) {
        Objects.requireNonNull(rng,"The RandU01Ifc was null" );
        if (n < 0) {
            throw new IllegalArgumentException("The number of random numbers was < 0");
        }
        if (k < 1) {
            throw new IllegalArgumentException("The number of intervals was < 1");
        }
        double[] f = new double[k + 1];
        // tabulate the frequencies
        for (long i = 1; i <= n; i++) {
            double u = rng.randU01();
            int j = (int) Math.ceil(k * u);
            f[j] = f[j] + 1;
        }
        double sum = 0.0;
        double e = ((double) n) / ((double) k);
        for (int j = 1; j <= k; j++) {
            sum = sum + (f[j] - e) * (f[j] - e);
        }
        sum = sum / e;
        return (sum);
    }

    /** Performs the 2-D chi-squared serial test
     *
     * @param rng the thing that produces U(0,1) numbers, must not null
     * @param n number of random numbers to test
     * @param k the number of intervals in the test for each dimension
     * @return the chi-squared test statistic
     */
    public static double serial2DTest(RandU01Ifc rng, long n, int k) {
        Objects.requireNonNull(rng,"The RandU01Ifc was null" );
        if (n < 0) {
            throw new IllegalArgumentException("The number of random numbers was < 0");
        }
        if (k < 1) {
            throw new IllegalArgumentException("The number of intervals was < 1");
        }
        double[][] f = new double[k + 1][k + 1];
        // tabulate the frequencies
        for (long i = 1; i <= n; i++) {
            double u1 = rng.randU01();
            int j1 = (int) Math.ceil(k * u1);
            double u2 = rng.randU01();
            int j2 = (int) Math.ceil(k * u2);
            f[j1][j2] = f[j1][j2] + 1;
        }
        double sum = 0.0;
        double dk = (double)k;
        double e = ((double) n )/ (dk * dk);
        for (int j1 = 1; j1 <= k; j1++) {
            for (int j2 = 1; j2 <= k; j2++) {
                sum = sum + (f[j1][j2] - e) * (f[j1][j2] - e);
            }
        }
        sum = sum / e;
        return (sum);
    }

    /** Performs the 3-D chi-squared serial test
     *
     * @param rng the thing that produces U(0,1) numbers, must not null
     * @param n number of random numbers to test
     * @param k the number of intervals in the test for each dimension
     * @return the chi-squared test statistic
     */
    public static double serial3DTest(RandU01Ifc rng, long n, int k) {
        Objects.requireNonNull(rng,"The RandU01Ifc was null" );
        if (n < 0) {
            throw new IllegalArgumentException("The number of random numbers was < 0");
        }
        if (k < 1) {
            throw new IllegalArgumentException("The number of intervals was < 1");
        }
        double[][][] f = new double[k + 1][k + 1][k + 1];
        // tabulate the frequencies
        for (long i = 1; i <= n; i++) {
            double u1 = rng.randU01();
            int j1 = (int) Math.ceil(k * u1);
            double u2 = rng.randU01();
            int j2 = (int) Math.ceil(k * u2);
            double u3 = rng.randU01();
            int j3 = (int) Math.ceil(k * u3);
            f[j1][j2][j3] = f[j1][j2][j3] + 1;
        }
        double sum = 0.0;
        double dk = (double)k;
        double e = ((double) n) / (dk * dk * dk);
        for (int j1 = 1; j1 <= k; j1++) {
            for (int j2 = 1; j2 <= k; j2++) {
                for (int j3 = 1; j3 <= k; j3++) {
                    sum = sum + (f[j1][j2][j3] - e) * (f[j1][j2][j3] - e);
                }
            }
        }
        sum = sum / e;
        return (sum);
    }

    /** Performs the correlation test
     *
     * @param rng the thing that produces U(0,1) numbers, must not null
     * @param lag the lag to test
     * @param n the number to sample
     * @return the test statistic
     */
    public static double correlationTest(RandU01Ifc rng, int lag, long n) {
        Objects.requireNonNull(rng,"The RandU01Ifc was null" );
        if (lag <= 0) {
            throw new IllegalArgumentException("The lag <= 0");
        }
        long h = (long) Math.floor((n - 1.0) / lag) - 1;
        if (h <= 0) {
            throw new IllegalArgumentException("(long)Math.floor((n-1)/lag) - 1 <= 0");
        }

        double sum = 0.0;
        double u2 = 0.0;
        double u1 = rng.randU01();
        for (long k = 0; k <= h; k++) {
            for (int j = 1; j <= lag; j++) {
                u2 = rng.randU01();
            }
            sum = sum + u1 * u2;
            u1 = u2;
        }
        double rho = (12.0 / (h + 1.0)) * sum - 3.0;
        double varrho = (13.0 * h + 7.0) / ((h + 1) * (h + 1));
        double asubj = rho / Math.sqrt(varrho);
        return (asubj);
    }

    /** Performs the runs up test
     *
     * @param rng the thing that produces U(0,1) numbers, must not null
     * @param n the number to sample
     * @return the test statistic
     */
    public static double runsUpTest(RandU01Ifc rng, long n) {
        Objects.requireNonNull(rng,"The RandU01Ifc was null" );
        if (n < 0) {
            throw new IllegalArgumentException("The number of random numbers was < 0");
        }
        double[] r = new double[7];
        double A = rng.randU01();
        int J = 1;
        for (long i = 2; i <= n; i++) {
            double B = rng.randU01();
            if (A >= B) {
                J = Math.min(J, 6);
                r[J] = r[J] + 1;
                J = 1;
            } else {
                J = J + 1;
            }
            //Replace A by B
            A = B;
        }
        J = Math.min(J, 6);
        r[J] = r[J] + 1;

        //Compute R
        double R = 0;
        for (int i = 1; i <= 6; i++) {
            for (int j = 1; j <= 6; j++) {
                R = R + a[i][j] * (r[i] - n * b[i]) * (r[j] - n * b[j]);
            }
        }
        return (R / n);
    }
}
