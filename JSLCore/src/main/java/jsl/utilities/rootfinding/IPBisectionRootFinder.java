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

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.math.JSLMath;


/**
 * @author rossetti
 */
public class IPBisectionRootFinder extends IPRootFinder {

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
     * Desired precision.
     */
    protected double myDesiredPrecision = JSLMath.getDefaultNumericalPrecision();

    /**
     * @param func
     * @param interval
     */
    public IPBisectionRootFinder(FunctionIfc func, Interval interval) {
        this(func, ((interval.getUpperLimit() + interval.getLowerLimit()) / 2.0), interval.getLowerLimit(), interval.getUpperLimit());
    }

    /**
     * @param func
     * @param lower
     * @param upper
     */
    public IPBisectionRootFinder(FunctionIfc func, double lower, double upper) {
        super(func, lower, upper);
        setInitialPoint((lower + upper) / 2.0);
    }

    /**
     * @param func
     * @param lower
     * @param upper
     */
    public IPBisectionRootFinder(FunctionIfc func, double initialPt, double lower, double upper) {
        super(func, lower, upper);
        setInitialPoint(initialPt);
    }

    /**
     * @return the xNeg
     */
    public double getXNeg() {
        return xNeg;
    }

    /**
     * @return the xPos
     */
    public double getXPos() {
        return xPos;
    }

    /**
     * @return the fNeg
     */
    public double getFNeg() {
        return fNeg;
    }

    /**
     * @return the fPos
     */
    public double getFPos() {
        return fPos;
    }

    public double getPrecision() {
        return (Math.abs(xPos - xNeg));
    }

    /**
     * Returns the desired precision.
     */
    public double getDesiredPrecision() {
        return myDesiredPrecision;
    }

    /**
     * Defines the desired precision.
     */
    public void setDesiredPrecision(double prec) {
        if (prec <= 0)
            throw new IllegalArgumentException("Non-positive precision: " + prec);
        myDesiredPrecision = prec;
    }

    /* (non-Javadoc)
     * @see jsl.simulation.IterativeProcess#hasNext()
     */
    @Override
    protected boolean hasNext() {
        if (hasConverged() || (myStepCounter >= myMaxIterations))
            return false;
        else
            return true;
    }

    /**
     * Check to see if the result has been attained.
     *
     * @return boolean
     */
    public boolean hasConverged() {
        return getPrecision() < myDesiredPrecision;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("Desired precision: " + getDesiredPrecision() + "\n");
        sb.append("Actual precision: " + getPrecision() + "\n");
        sb.append("Converged? " + hasConverged() + "\n");
        sb.append("xNeg: " + xNeg + "\n");
        sb.append("fNeg: " + fNeg + "\n");
        sb.append("xPos: " + xPos + "\n");
        sb.append("fPos: " + fPos + "\n");
        sb.append(myCurrentStep);
        return (sb.toString());
    }

    protected void initializeIterations() {
        super.initializeIterations();
        double xLower = getLowerLimit();
        double xUpper = getUpperLimit();

        double fL = f.fx(xLower);
        double fU = f.fx(xUpper);

        if (fL * fU > 0.0)
            throw new IllegalArgumentException("There is no root in the provided interval");

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
    }

    /* (non-Javadoc)
     * @see jsl.simulation.IterativeProcess#next()
     */
    @Override
    protected RootFinderStep next() {

        if (!hasNext())
            return null;

        setRoot((xPos + xNeg) * 0.5);

        return myCurrentStep;
    }

    /* (non-Javadoc)
     * @see jsl.simulation.IterativeProcess#runStep()
     */
    @Override
    protected void runStep() {

        myCurrentStep = next();

        if (getFunctionAtRoot() > 0)
            xPos = getRoot();
        else
            xNeg = getRoot();

        if (mySaveStepOption == true) {
            RootFinderStep s = new RootFinderStep();
            s.myX = getRoot();
            s.myFofX = getFunctionAtRoot();
            mySteps.add(s);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        FunctionIfc f = new FunctionIfc() {
            public double fx(double x) {
                return x * x * x + 4.0 * x * x - 10.0;
            }
        };

        IPBisectionRootFinder b = new IPBisectionRootFinder(f, 1.0, 2.0);

        b.run();

        System.out.println(b);

    }

}
