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
package jsl.utilities.distributions;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.RVariableIfc;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Provides a representation for a discrete distribution with
 * arbitrary values and assigned probabilities to each value.
 * Allows the specification of the distribution via a pair of arrays containing the
 * values = {v1, v2, ... , vn} and the cumulative probabilities cdf = {c1, c2, ... , 1.0}
 *
 * where if p1 is the probability associated with v1, p2 with v2, etc
 * then c1 = p1, c2 = p1 + p2, c3 = p1 + p2 + p3, etc,
 * with cn = 1.0 (the sum of all the probabilities). If cn is not 1.0, then
 * an exception is thrown.
 */
public class DEmpiricalCDF extends Distribution implements DiscreteDistributionIfc, GetRVariableIfc {

    /**
     * Holds the list of probability points
     */
    private LinkedList<ProbPoint> myProbabilityPoints;

    /**
     * @param values an array of values that will be drawn from
     * @param cdf    a cdf corresponding to the values
     */
    public DEmpiricalCDF(double[] values, double[] cdf) {
        this(values, cdf, null);
    }

    /**
     * (v[0], cdf[0], ...) represent the value and the cumulative probability of that value.
     *
     * @param values an array of values that will be drawn from
     * @param cdf    a cdf corresponding to the values
     * @param name   an optional name/label
     */
    public DEmpiricalCDF(double[] values, double[] cdf, String name) {
        super(name);
        double[] pairs = makePairs(values, cdf);
        myProbabilityPoints = new LinkedList<ProbPoint>();
        setParameters(pairs);
    }

    /**
     * Assigns the probability associated with each cdf value
     * to the integers starting at 0.
     *
     * @param cdf the probability array. must have valid probability elements
     *            and last element equal to 1. Every element must be greater than or equal
     *            to the previous element. That is, monotonically increasing.
     * @return the pairs
     */
    public static double[] makePairs(double[] cdf) {
        return makePairs(0, cdf);
    }

    /**
     * Assigns the probability associated with each cdf value
     * to the integers starting at start.
     *
     * @param start place to start assignment
     * @param cdf   the probability array. must have valid probability elements
     *              and last element equal to 1. Every element must be greater than or equal
     *              to the previous element. That is, monotonically increasing.
     * @return the pairs
     */
    public static double[] makePairs(int start, double[] cdf) {
        if (cdf == null) {
            throw new IllegalArgumentException("The probability array was null");
        }
        if (cdf[cdf.length - 1] != 1.0) {
            throw new IllegalArgumentException("The last element was not 1.0");
        }
        for (int i = 0; i < cdf.length; i++) {
            if ((cdf[i] < 0.0) || (cdf[i] > 1.0)) {
                throw new IllegalArgumentException("An individual probability was not in [0.0, 1.0]");
            }
            if (i < cdf.length - 1) {
                if (cdf[i + 1] < cdf[i]) {
                    throw new IllegalArgumentException("The cdf was not monotonically increasing.");
                }
            }
        }
        double[] pairs = new double[cdf.length * 2];
        for (int i = 0; i < cdf.length; i++) {
            pairs[2 * i] = start;
            pairs[2 * i + 1] = cdf[i];
            start = start + 1;
        }

        return pairs;
    }

    /**
     * This method takes in an Array of probability points
     * (value, cumulative probability), Eg. X[] = {v1, cp1, v2, cp2, ...},
     * as the input parameter and makes a 2D array of the value/prob pairs
     *
     * @param pairs An array holding the value, cumulative probability pairs.
     */
    public static double[][] splitPairs(double[] pairs) {
        Objects.requireNonNull(pairs, "The pairs array was null");
        int n = pairs.length / 2;
        double[][] split = new double[2][n];
        for (int i = 0; i < n; i++) {
            split[0][i] = pairs[2 * i];
            split[1][i] = pairs[2 * i + 1];
        }
        return split;
    }

    /**
     * Makes a pair array that can be used for the parameters of the DEmpiricalCDF distribution
     *
     * @param values an array of values that will be drawn from
     * @param cdf    a cdf corresponding to the values
     * @return a properly configured array of pairs for the DEmpiricalCDF distribution
     */
    public static double[] makePairs(double[] values, double[] cdf) {
        Objects.requireNonNull(values, "The values array was null");
        Objects.requireNonNull(cdf, "The values array was null");
        if (values.length != cdf.length) {
            throw new IllegalArgumentException("The length of the arrays was not equal.");
        }
        if (!JSLRandom.isValidCDF(cdf)) {
            throw new IllegalArgumentException("The cdf array does not represent a valid cdf");
        }
        double[] pairs = new double[cdf.length * 2];
        for (int i = 0; i < cdf.length; i++) {
            pairs[2 * i] = values[i];
            pairs[2 * i + 1] = cdf[i];
        }
        return pairs;
    }

    @Override
    public final DEmpiricalCDF newInstance() {
        double[] pairs = getParameters();
        double[][] splitPairs = splitPairs(pairs);
        return (new DEmpiricalCDF(splitPairs[0], splitPairs[1]));
    }

    @Override
    public final double cdf(double x) {
        ProbPoint lowpt = (ProbPoint) myProbabilityPoints.getFirst();

        if (x < lowpt.value) {
            return (0.0);
        }

        ProbPoint uppt = (ProbPoint) myProbabilityPoints.getLast();

        if (x >= uppt.value) {
            return (1.0);
        }

        ListIterator<ProbPoint> iter = myProbabilityPoints.listIterator();
        while (iter.hasNext()) {
            lowpt = (ProbPoint) iter.next();
            uppt = (ProbPoint) iter.next();
            double lv = lowpt.value;
            double uv = uppt.value;

            if ((lv <= x) && (x < uv)) {
                break;
            }
        }

        if (lowpt == null) {
            return (Double.NaN);
        } else {
            return (lowpt.cumProb);
        }
    }

    @Override
    public final double getMean() {
        double m = 0.0;
        for (ProbPoint p : myProbabilityPoints) {
            m = m + p.probability * p.value;
        }
        return (m);
    }

    @Override
    public final double getVariance() {
        double m1 = 0.0;
        double m2 = 0.0;
        for (ProbPoint pp : myProbabilityPoints) {
            double v = pp.value;
            double p = pp.probability;
            m1 = m1 + p * v;
            m2 = m2 + p * v * v;
        }
        return (m2 - m1 * m1);
    }

    /**
     * The probability mass function for this discrete distribution.
     * Returns the same as pdf.
     *
     * @param x The point to get the probability for
     * @return The probability associated with x
     */
    @Override
    public final double pmf(double x) {
        ProbPoint p = null;
        boolean ifExist = false;

        ListIterator<ProbPoint> iter = myProbabilityPoints.listIterator();
        while (iter.hasNext()) {
            p = (ProbPoint) iter.next();
            if (x == p.value) {
                ifExist = true;
                break;
            }
        }

        if (ifExist == false) {
            return (Double.NaN);
        } else {
            return (p.probability);
        }
    }

    /**
     * Returns the pmf as a string.
     *
     * @return A String of probability, value pairs.
     */
    @Override
    public String toString() {
        return (myProbabilityPoints.toString());
    }

    /**
     * Provides the inverse cumulative distribution function for the
     * distribution
     *
     * @param p The probability to be evaluated for the inverse, p must be [0,1]
     *          or
     *          an IllegalArgumentException is thrown
     * @return The inverse cdf evaluated at p
     */
    @Override
    public double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }

        double x = 0.0;
        ListIterator<ProbPoint> iter = myProbabilityPoints.listIterator();

        while (iter.hasNext()) {
            ProbPoint pp = (ProbPoint) iter.next();
            double cp = pp.cumProb;
            if (p <= cp) {
                x = pp.value;
                break;
            }
        }
        return (x);
    }

    /**
     * Sets the parameters for the distribution. Array of probability points
     * (value, cumulative probability), Eg. X[] = [v1, cp1, v2, cp2, 7,0.5],
     * as the input parameters.
     *
     * @param parameters an array of doubles representing the parameters for
     *                   the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("Input probability array does not have an even number of elements");
        }

        if (parameters[parameters.length - 1] != 1.0) {
            throw new IllegalArgumentException("CDF must sum to 1.0, last prob was not 1.0");

        }

        double cp = 0.0;// last cp
        for (int i = 0; i < parameters.length; i = i + 2) {
            ProbPoint pp = new ProbPoint(parameters[i], parameters[i + 1]);
            pp.probability = pp.cumProb - cp;
            cp = pp.cumProb;
            myProbabilityPoints.add(pp);
        }
    }

    /**
     * Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public double[] getParameters() {
        int n = 2 * myProbabilityPoints.size();
        double[] param = new double[n];

        int i = 0;
        ProbPoint p;
        ListIterator<ProbPoint> iter = myProbabilityPoints.listIterator();
        while (iter.hasNext()) {
            p = (ProbPoint) iter.next();
            param[i] = p.value;
            param[i + 1] = p.cumProb;
            i = i + 2;
        }

        return (param);
    }

    private final class ProbPoint {

        private double value;
        private double probability;
        private double cumProb;

        private ProbPoint(double v, double cp) {
            if ((cp < 0.0) || (cp > 1.0)) {
                throw new IllegalArgumentException("Probability must be in interval [0,1]");
            }
            value = v;
            cumProb = cp;
        }

        @Override
        public String toString() {
            String s = "P(x=" + value + ")= " + probability + "\t";
            s = s + "P(x<=" + value + ")= " + cumProb + "\n";
            return (s);
        }
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        double[] values = new double[myProbabilityPoints.size()];
        double[] cdf = new double[myProbabilityPoints.size()];
        int i = 0;
        for (ProbPoint probPoint : myProbabilityPoints) {
            values[i] = probPoint.value;
            cdf[i] = probPoint.cumProb;
            i++;
        }
        return new DEmpiricalRV(values, cdf, rng);
    }

    public static void main(String args[]) {
        double[] values = {1.0, 2.0, 3.0, 4.0};
        double[] cdf = {1.0 / 6.0, 3.0 / 6.0, 5.0 / 6.0, 1.0};

        DEmpiricalCDF n2 = new DEmpiricalCDF(values, cdf);
        RVariableIfc rv2 = n2.getRandomVariable();

        System.out.println("mean = " + n2.getMean());
        System.out.println("var = " + n2.getVariance());
        System.out.println("pmf");
        System.out.println(n2);

        for (int i = 1; i <= 10; i++) {
            System.out.println("x(" + i + ")= " + rv2.getValue());
        }

        values = new double[]{1.0, 2.0, 4.0, 5.0};
        cdf = new double[] {0.7, 0.8, 0.9, 1.0};

        DEmpiricalCDF d = new DEmpiricalCDF(values, cdf);
        RVariableIfc rvd = d.getRandomVariable();
        System.out.println("mean = " + d.getMean());
        System.out.println("var = " + d.getVariance());
        System.out.println("pmf");
        System.out.println(d);

        for (int i = 1; i <= 5; i++) {
            System.out.println("x(" + i + ")= " + rvd.getValue());
        }

        System.out.println();
        System.out.println("invCDF(0.2) = " + d.invCDF(0.2));
        System.out.println("invCDF(0.983) = " + d.invCDF(0.983));
        System.out.println("invCDF(" + d.cdf(1.0) + ") = " + d.invCDF(d.cdf(1.0)));

        System.out.println("done");
    }
}
