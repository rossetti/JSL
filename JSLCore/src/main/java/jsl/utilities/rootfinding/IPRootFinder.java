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

import jsl.simulation.IterativeProcess;
import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.math.JSLMath;

public abstract class IPRootFinder extends IterativeProcess<RootFinderStep> {

    /**
     * Function for which the zero should be found.
     */
    protected FunctionIfc f;

    /**
     * The interval for the search
     */
    protected Interval myInterval;

    /**
     * The initial point for the search
     */
    protected double myInitialPt = Double.NaN;

    /**
     * The maximum number of iterations permitted to find the root
     * The default is 100
     */
    protected int myMaxIterations = 100;

    /**
     * Used in the static methods for finding intervals
     */
    protected static int numIterations = 50;

    /**
     * used in the static methods for finding intervals
     */
    protected static double searchFactor = 1.6;

    /**
     * Defines a function and an interval for searching for a root
     *
     * @param func     must not be null, must have a root in the interval
     * @param interval
     */
    public IPRootFinder(FunctionIfc func, Interval interval) {
        this(func, interval.getLowerLimit(), interval.getUpperLimit());
    }

    /**
     * Defines a function and an interval for searching for a root
     *
     * @param func   must not be null, must have a root in the interval
     * @param xLower must be less than xUpper
     * @param xUpper must be greater than xLower
     */
    public IPRootFinder(FunctionIfc func, double xLower, double xUpper) {
        super();
        setFunction(func);
        setInterval(xLower, xUpper);
        myCurrentStep = new RootFinderStep();
    }

    /**
     * Returns the last evaluated value that was considered
     * for the root of the function
     */
    public double getRoot() {
        return myCurrentStep.myX;
    }

    /**
     * Returns the value of the function at the last considered
     * possible root of the function
     *
     * @return
     */
    public double getFunctionAtRoot() {
        return myCurrentStep.myFofX;
    }

    /**
     * @param epsilon double
     * @return double
     */
    public double relativePrecision(double epsilon) {
        return relativePrecision(epsilon, Math.abs(getRoot()));
    }

    /**
     * @param epsilon double
     * @param x       double
     * @return double
     */
    public double relativePrecision(double epsilon, double x) {
        return x > JSLMath.getDefaultNumericalPrecision()
                ? epsilon / x : epsilon;
    }

    /**
     * @return the maximum number of iterations
     */
    public int getMaxIterations() {
        return myMaxIterations;
    }

    /**
     * @param maxIterations the maximum number of iterations
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1)
            throw new IllegalArgumentException("The maximum number of iterations must be > 0");
        myMaxIterations = maxIterations;
    }

    /**
     * Returns true if the number of iterations is &lt; max iterations
     *
     * @return
     */
    public boolean hasIterations() {
        return (myStepCounter < myMaxIterations);
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
        if (xLower >= xUpper)
            return (false);

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
        if (xLower >= xUpper)
            return (false);

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
     * subintervals and attempt to find nmax bracketing intervals that contain roots
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
        double fc = 0.0;
        for (int j = 1; j <= n; j++) {
            x = x + dx;
            fc = func.fx(x);
            if (fc * fp <= 0.0) {
                Interval i = new Interval(x, x - dx);
                intervals.add(i);
                if (intervals.size() == nmax)
                    return (intervals);
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
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit
     * and if the function does not cross the axis within the provided interval.
     *
     * @param xLower
     * @param xUpper
     */
    public void setInterval(double xLower, double xUpper) {

        if (xLower >= xUpper)
            throw new IllegalArgumentException("The lower limit must be < the upper limit");

        double fL = f.fx(xLower);
        double fU = f.fx(xUpper);

        if (fL * fU > 0.0){
            String s = String.format("There was no root in the interval [%f, %f] %n with function values [%f, %f]",
                    xLower, xUpper, fL, fU);
            throw new IllegalArgumentException(s);
        }

        if (myInterval == null)
            myInterval = new Interval(xLower, xUpper);
        else {
            myInterval.setInterval(xLower, xUpper);
        }
    }

    /**
     * Returns the initial point used for the search
     *
     * @return
     */
    public double getInitialPoint() {
        return myInitialPt;
    }

    /**
     * Sets the initial starting point for the search. The starting point
     * must be in the interval defined for the search or an IllegalArgumentException
     * will be thrown.
     *
     * @param initialPt
     */
    public void setInitialPoint(double initialPt) {
        if (!myInterval.contains(initialPt))
            throw new IllegalArgumentException("The initial point is not in the interval");
        myInitialPt = initialPt;
    }

    /**
     * Enumerates equally spaced points in the interval and returns
     * the point that has the function value closest to zero
     *
     * @return
     */
    public double recommendInitialPoint() {
        return recommendInitialPoint(numIterations);
    }

    /**
     * Enumerates nmax equally spaced points in the interval and returns
     * the point that has the function value closest to zero
     *
     * @param nmax
     * @return
     */
    public double recommendInitialPoint(int nmax) {
        if (nmax <= 1)
            return (getLowerLimit() + getUpperLimit()) / 2.0;

        double x = getLowerLimit();
        double mx = 0.0;
        double my = Double.MAX_VALUE;
        double y = 0;
        double delta = (getUpperLimit() - getLowerLimit()) / nmax;

        for (int i = 1; i <= nmax; i++) {
            y = f.fx(x);
//			System.out.println("x = " + x + " f(x) = " + y);
            if (Math.abs(y) < my) {
                my = Math.abs(y);
                mx = x;
            }
            x = x + delta;
        }
        return mx;
    }

    /**
     * Checks to see if the the supplied point is within the search
     * interval
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
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit
     * and if the function does not cross the axis within the provided interval.
     *
     * @param func
     * @param interval
     */
    public final void setInterval(FunctionIfc func, Interval interval) {
        setInterval(func, interval.getLowerLimit(), interval.getUpperLimit());
    }

    /**
     * Sets the bracketing interval within which the root should be found.
     * Throws IllegalArgumentExceptons if the lower limit is &gt; upper limit
     * and if the function does not cross the axis within the provided interval.
     *
     * @param func   Sets the function to be evaluated, must not be null
     * @param xLower
     * @param xUpper
     */
    public final void setInterval(FunctionIfc func, double xLower, double xUpper) {
        setFunction(func);
        setInterval(xLower, xUpper);
    }

    /**
     * Returns a String representation of the finder
     *
     * @return A String with basic results
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID " + getId() + "\n");
        sb.append("Name " + getName() + "\n");
        sb.append("Search interval: " + myInterval + "\n");
        sb.append("Starting point: " + myInitialPt + "\n");
        sb.append("Maximum number of iterations permitted: " + myMaxIterations + "\n");
        sb.append("Number of iterations taken: " + getNumberStepsCompleted() + "\n");
        sb.append("Elapsed execution time: " + getElapsedExecutionTime() + " milliseconds \n");
        sb.append("Current root: " + getRoot() + "\n");
        sb.append("Current function value at root: " + getFunctionAtRoot() + "\n");

        return (sb.toString());
    }

    /**
     * @param func OneVariableFunction
     */
    protected void setFunction(FunctionIfc func) {
        if (func == null)
            throw new IllegalArgumentException("The function must not be null");
        f = func;
    }

    protected void setRoot(double x) {
        myCurrentStep.myX = x;
        myCurrentStep.myFofX = f.fx(x);
    }

    /**
     * Check to see if the result has been attained.
     *
     * @return boolean
     */
    abstract public boolean hasConverged();

}
