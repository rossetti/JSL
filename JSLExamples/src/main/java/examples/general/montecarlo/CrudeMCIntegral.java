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
package examples.general.montecarlo;

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

/**
 * @author rossetti
 *
 */
public class CrudeMCIntegral {

    protected FunctionIfc myFunction;
    private final Statistic myStatistic;
    private final Interval myInterval;
    private RNStreamIfc myStream;

    public CrudeMCIntegral(double lowerLimit, double upperLimit, FunctionIfc function) {
        this(new Interval(lowerLimit, upperLimit), function, JSLRandom.nextRNStream());
    }

    public CrudeMCIntegral(double lowerLimit, double upperLimit, FunctionIfc function, int streamNum) {
        this(lowerLimit, upperLimit, function, JSLRandom.rnStream(streamNum));
    }

    public CrudeMCIntegral(double lowerLimit, double upperLimit, FunctionIfc function, RNStreamIfc stream) {
        this(new Interval(lowerLimit, upperLimit), function, stream);
    }

    public CrudeMCIntegral(Interval interval, FunctionIfc function) {
        this(interval, function, JSLRandom.nextRNStream());
    }

    public CrudeMCIntegral(Interval interval, FunctionIfc function, RNStreamIfc stream) {
        Objects.requireNonNull(interval, "The interval was null");
        Objects.requireNonNull(stream, "The RNStreamIfc was null");
        myInterval = interval;
        myStream = stream;
        setFunction(function);
        myStatistic = new Statistic("Monte-Carlo Integration");
    }

    public void setLimits(double lowerLimit, double upperLimit) {
        myInterval.setInterval(lowerLimit, upperLimit);
    }

    public void setFunction(FunctionIfc function) {
        Objects.requireNonNull(function, "The FunctionIfc was null");
        myFunction = function;
    }

    public void runAll(int sampleSize) {
        runAll(sampleSize, true);
    }

    public void runAll(int sampleSize, boolean resetStartStream) {
        if (sampleSize < 1) {
            throw new IllegalArgumentException("The sample size must be >= 1");
        }

        myStatistic.reset();
        if (resetStartStream) {
            myStream.resetStartStream();
        }

        double r = myInterval.getWidth();
        double a = myInterval.getLowerLimit();
        double b = myInterval.getUpperLimit();
        for (int i = 1; i <= sampleSize; i++) {
            double x = JSLRandom.rUniform(a, b, myStream);
            double y = r * myFunction.fx(x);
            myStatistic.collect(y);
        }
    }

    public void runUntil(double desiredHW) {
        runUntil(desiredHW, 0.95, true);
    }

    public void runUntil(double desiredHW, boolean resetStartStream) {
        runUntil(desiredHW, 0.95, resetStartStream);
    }

    public void runUntil(double desiredHW, double confLevel, boolean resetStartStream) {
        if (desiredHW <= 0) {
            throw new IllegalArgumentException("The desired half-width must be >= 0");
        }

        myStatistic.reset();
        if (resetStartStream) {
            myStream.resetStartStream();
        }

        double r = myInterval.getWidth();
        double a = myInterval.getLowerLimit();
        double b = myInterval.getUpperLimit();
        boolean flag = false;
        while (flag != true) {
            double x = JSLRandom.rUniform(a, b, myStream);
            double y = r * myFunction.fx(x);
            myStatistic.collect(y);
            if (myStatistic.getCount() > 2) {
                flag = (myStatistic.getHalfWidth(confLevel) < desiredHW);
            }
        }

    }

    public double getEstimate() {
        return (myStatistic.getAverage());
    }

    public Statistic getStatistic() {
        return myStatistic;
    }

    @Override
    public String toString() {
        return (myStatistic.toString());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        double a = 0.0;
        double b = Math.PI;

        class SinFunc implements FunctionIfc {

            public double fx(double x) {
                return (Math.sin(x));
            }
        }

        SinFunc f = new SinFunc();
        CrudeMCIntegral mc = new CrudeMCIntegral(a, b, f);
        mc.runAll(100);
        System.out.println(mc);

        mc.runAll(100, false);
        System.out.println(mc);

        class F1 implements FunctionIfc {

            public double fx(double x) {
                return (Math.exp(-x * Math.cos(Math.PI * x)));
            }
        }

        mc.setFunction(new F1());
        mc.setLimits(0.0, 1.0);
        mc.runAll(1280);
        System.out.println(mc);
    }
}
