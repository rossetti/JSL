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

package jsl.utilities.rootfinding;

import java.util.ArrayList;
import java.util.List;

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionalIterator;
import jsl.utilities.math.FunctionIfc;

public abstract class RootFinder extends FunctionalIterator {

    /**
     * Value at which the function's value is negative.
     */
    protected double xNeg;

    /**
     * Value at which the function's value is positive.
     */
    protected double xPos;

    /**
     * The value of the function at xNeg
     */
    protected double fNeg;

    /**
     * The value of the function at xPos
     */
    protected double fPos;

    /**
     * The interval for the search
     *
     */
    protected Interval myInterval;

    /**
     * The initial point for the search
     *
     */
    protected double myInitialPt;

    /**
     * Used in the static methods for finding intervals
     *
     */
    protected static int numIterations = 50;

    /**
     * used in the static methods for finding intervals
     *
     */
    protected static double searchFactor = 1.6;

    public RootFinder() {
        this(new FunctionIfc() {

            public double fx(double x) {
                return (x);
            }
        }, -1.0, 1.0);
    }

    public RootFinder(FunctionIfc func, Interval interval) {
        this(func, interval.getLowerLimit(), interval.getUpperLimit());
    }

    public RootFinder(FunctionIfc func, double xLower, double xUpper) {
        super(func);
        setInterval(xLower, xUpper);
    }

    /**
     * Returns a String representation of the finder
     *
     * @return A String with basic results
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Search interval: ");
        sb.append(myInterval);
        sb.append("\n");
        sb.append("Starting point: ");
        sb.append(myInitialPt);
        sb.append("\n");
        sb.append("Final root: ");
        sb.append(getResult());
        sb.append("\n");
        sb.append("Converged?: ");
        sb.append(hasConverged());
        sb.append("\n");
        sb.append("Desired precision: ");
        sb.append(getDesiredPrecision());
        sb.append("\n");
        sb.append("Actual precision: ");
        sb.append(getPrecision());
        sb.append("\n");

        return (sb.toString());
    }

    /**
     * Returns true if the supplied interval contains a root
     *
     * @param interval
     * @return
     */
    public final boolean hasRoot(Interval interval) {
        return (hasRoot(interval.getLowerLimit(), interval.getUpperLimit()));
    }

    /**
     * Returns true if the supplied interval contains a root
     *
     * @param xLower
     * @param xUpper
     * @return
     */
    public boolean hasRoot(double xLower, double xUpper) {
        if (xLower >= xUpper) {
            return (false);
        }

        double fL = f.fx(xLower);
        double fU = f.fx(xUpper);
        return (fL * fU <= 0);
    }

    /**
     * Returns true if the supplied interval contains a root
     *
     * @param func
     * @param xLower
     * @param xUpper
     * @return
     */
    public static boolean hasRoot(FunctionIfc func, double xLower, double xUpper) {
        if (xLower >= xUpper) {
            return (false);
        }

        double fL = func.fx(xLower);
        double fU = func.fx(xUpper);
        return (fL * fU <= 0);
    }

    /**
     * Using the supplied function and the initial interval provided, try to
     * find a bracketing interval by expanding the interval outward
     *
     * @param func
     * @param interval
     * @return
     */
    public static boolean findInterval(FunctionIfc func, Interval interval) {
        double x1 = interval.getLowerLimit();
        double x2 = interval.getUpperLimit();
        double f1 = func.fx(x1);
        double f2 = func.fx(x2);
        for (int j = 1; j <= numIterations; j++) {
            if (f1 * f2 < 0) {
                interval.setInterval(x1, x2);
                return (true);
            } else {
                if (Math.abs(f1) < Math.abs(f2)) {
                    x1 = x1 + searchFactor * (x1 - x2);
                    f1 = func.fx(x1);
                } else {
                    x2 = x2 + searchFactor * (x2 - x1);
                    f2 = func.fx(x2);
                }
            }
        }
        return (false);
    }

    /**
     * Given a function and a starting interval, subdivide the interval into n
     * subintervals and attempt to find nmax bracketing intervals that contain
     * roots
     *
     * @param func
     * @param interval
     * @param n
     * @param nmax
     * @return The list of bracketing intervals
     */
    public static List<Interval> findInterval(FunctionIfc func, Interval interval, int n, int nmax) {

        List<Interval> intervals = new ArrayList<Interval>();
        double x1 = interval.getLowerLimit();
        double x2 = interval.getUpperLimit();
        double dx = (x2 - x1) / n;
        double x = x1;
        double fp = func.fx(x1);
        double fc;// = 0.0;
        for (int j = 1; j <= n; j++) {
            x = x + dx;
            fc = func.fx(x);
            if (fc * fp <= 0.0) {
                Interval i = new Interval(x, x - dx);
                intervals.add(i);
                if (intervals.size() == nmax) {
                    return (intervals);
                }
            }
            fp = fc;
        }
        return (intervals);
    }

    /**
     * Sets the search interval for the search
     *
     * @param interval
     */
    public final void setInterval(Interval interval) {
        setInterval(interval.getLowerLimit(), interval.getUpperLimit());
    }

    /**
     * Sets the bracketing interval within which the root should be found.
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit and
     * if the function does not cross the axis within the provided interval.
     *
     * @param xLower
     * @param xUpper
     */
    public void setInterval(double xLower, double xUpper) {

        if (xLower >= xUpper) {
            throw new IllegalArgumentException("The lower limit must be < the upper limit");
        }

        double fL = f.fx(xLower);
        double fU = f.fx(xUpper);
	//	System.out.println("f("+xLower+")="+fL);
	//	System.out.println("f("+xUpper+")="+fU);

        if (fL * fU > 0.0) {
            throw new IllegalArgumentException("There is no root in the provided interval");
        }

        if (fL < 0.0) {
            fNeg = fL;
            fPos = fU;
            xNeg = xLower;
            xPos = xUpper;
        } else {
            fNeg = fU;
            fPos = fL;
            xPos = xLower;
            xNeg = xUpper;
        }

        if (myInterval == null) {
            myInterval = new Interval(xLower, xUpper);
        } else {
            myInterval.setInterval(xLower, xUpper);
        }
    }

    /**
     * Sets the initial starting point for the search. The starting point must
     * be in the interval defined for the search or an IllegalArgumentException
     * will be thrown.
     *
     * @param initialPt
     */
    public void setInitialPoint(double initialPt) {
        if (!myInterval.contains(initialPt)) {
            throw new IllegalArgumentException("The intial point is not in the interval");
        }
        myInitialPt = initialPt;
    }

    /**
     * Checks to see if the the supplied point is within the search interval
     *
     * @param x
     * @return
     */
    public final boolean contains(double x) {
        return myInterval.contains(x);
    }

    /**
     * The lower limit for the search interval
     *
     * @return
     */
    public final double getLowerLimit() {
        return myInterval.getLowerLimit();
    }

    /**
     * The upper limit for the search interval
     *
     * @return
     */
    public final double getUpperLimit() {
        return myInterval.getUpperLimit();
    }

    /**
     * Sets the bracketing interval within which the root should be found.
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit and
     * if the function does not cross the axis within the provided interval.
     *
     * @param func
     * @param interval
     */
    public final void setInterval(FunctionIfc func, Interval interval) {
        setInterval(func, interval.getLowerLimit(), interval.getUpperLimit());
    }

    /**
     * Sets the bracketing interval within which the root should be found.
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit and
     * if the function does not cross the axis within the provided interval.
     *
     * @param func Sets the function to be evaluated, must not be null
     * @param xLower
     * @param xUpper
     */
    public final void setInterval(FunctionIfc func, double xLower, double xUpper) {
        setFunction(func);
        setInterval(xLower, xUpper);
    }
}
