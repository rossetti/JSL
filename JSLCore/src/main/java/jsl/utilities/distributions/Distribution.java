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

import jsl.utilities.Interval;
import jsl.utilities.NewInstanceIfc;
import jsl.utilities.controls.ControllableIfc;
import jsl.utilities.controls.Controls;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.InverseCDFRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.rootfinding.BisectionRootFinder;

/**
 * An Distribution provides a skeletal implementation for classes that must
 * implement the DistributionIfc. This class is an abstract class. Subclasses
 * must provide concrete implementations.
 *
 *
 */
public abstract class Distribution implements DistributionIfc, ControllableIfc, NewInstanceIfc {

    private static BisectionRootFinder myRootFinder;

    private static Interval myInterval;

    /** A counter to count the number of created to assign "unique" ids
     */
    private static int myIdCounter_;

    /** The id of this object
     */
    protected int myId;

    /** Holds the name of the name of the object for the IdentityIfc
     */
    protected String myName;

    /**
     * Constructs a probability distribution
     */
    public Distribution() {
        this(null);
    }

    /**
     * Constructs a probability distribution
     *
     * @param name a String name @returns a valid Distribution
     */
    public Distribution(String name) {
        setId();
        setName(name);
    }

    /**
     *
     * @return the assigned name
     */
    public final String getName() {
        return myName;
    }

    /** Sets the name
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            myName = this.getClass().getSimpleName();
        } else {
            myName = str;
        }
    }

    /**
     *
     * @return a number identifier
     */
    public final int getId() {
        return (myId);
    }

    protected final void setId() {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
    }

    @Override
    public Controls getControls() {
        return new RandomControls();
    }

    @Override
    public void setControls(Controls controls) {
        if (controls == null) {
            throw new IllegalArgumentException("The supplied controls were null!");
        }
        setParameters(controls.getDoubleArrayControl("parameters"));
    }

    @Override
    public final double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    @Override
    abstract public Distribution newInstance();

    @Override
    public RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new InverseCDFRV(newInstance(), rng);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Mean ");
        sb.append(getMean());
        sb.append(System.lineSeparator());
        sb.append("Variance ");
        sb.append(getVariance());
        sb.append(System.lineSeparator());
        return (sb.toString());
    }

    protected class RandomControls extends Controls {

        protected void fillControls(){
            addDoubleArrayControl("parameters", getParameters());
        }
    }

    /**
     * Computes the inverse CDF by using the bisection method [ll,ul] must
     * contain the desired value. Initial search point is (ll+ul)/2.0
     *
     * [ll, ul] are defined on the domain of the CDF, i.e. the X values
     *
     * @param cdf a reference to the cdf
     * @param p must be in [0,1]
     * @param ll lower limit of search range, must be &lt; ul
     * @param ul upper limit of search range, must be &gt; ll
     * @return the inverse of the CDF evaluated at p
     */
    public static double inverseContinuousCDFViaBisection(final ContinuousDistributionIfc cdf, final double p,
            double ll, double ul) {
        return inverseContinuousCDFViaBisection(cdf, p, ll, ul, (ll + ul) / 2.0);
    }

    /**
     * Computes the inverse CDF by using the bisection method [ll,ul] must
     * contain the desired value
     *
     * [ll, ul] are defined on the domain of the CDF, i.e. the x values
     *
     * @param cdf a reference to the cdf
     * @param p must be in [0,1]
     * @param ll lower limit of search range, must be &lt; ul
     * @param ul upper limit of search range, must be &gt; ll
     * @param initialX an initial starting point that must be in [ll,ul]
     * @return the inverse of the CDF evaluated at p
     */
    public static double inverseContinuousCDFViaBisection(final ContinuousDistributionIfc cdf, final double p,
            double ll, double ul, double initialX) {

        if (ll >= ul) {
            String msg = "Supplied lower limit " + ll + " must be less than upper limit " + ul;
            throw new IllegalArgumentException(msg);
        }

        if ((p < ll) || (p > ul)) {
            String msg = "Supplied probability was " + p + " Probability must be [0,1)";
            throw new IllegalArgumentException(msg);
        }

        if (myInterval == null) { // lazy initialization
            myInterval = new Interval(ll, ul);
        } else {
            myInterval.setInterval(ll, ul);
        }

        if (myRootFinder == null) {// lazy initialization
            myRootFinder = new BisectionRootFinder();
        }

        FunctionIfc f = new FunctionIfc() {

            @Override
            public double fx(double x) {
                return cdf.cdf(x) - p;
            }
        };

        if (!myRootFinder.hasRoot(ll, ul)) {
            String msg = "[" + ll + " ,  " + ul + " ] does not contain a root";
            throw new IllegalArgumentException(msg);
        }
        myRootFinder.setInterval(f, myInterval);
        myRootFinder.setInitialPoint(initialX);
        myRootFinder.evaluate();

        return myRootFinder.getResult();
    }

    /** Searches starting at the value start until the CDF &gt; p
     *  "start" must be the smallest possible value for the range of the CDF
     *  as an integer.  This requirement is NOT checked
     *
     *  Each value is incremented by 1. Thus, the range of possible
     *  values for the CDF is assumed to be {start, start + 1, start + 2, etc.}
     *
     * @param df a reference to the discrete distribution
     * @param p the probability to evaluate, must be (0,1)
     * @param start the initial starting search position
     * @return the found inverse of the CDF found for p
     */
    public static double inverseDiscreteCDFViaSearchUp(DiscreteDistributionIfc df, double p, int start) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be [0,1)");
        }

        int i = start;
        while (p > df.cdf(i)) {
            i++;
        }
        return i;
    }
}
