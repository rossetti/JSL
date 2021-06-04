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
package examples.montecarlo;

import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class EstimateRenewals {

    private Statistic myStat;

    private RVariableIfc myRV;

    private int myMaxIterations = 100000;

    private int myMinIterations = 30;

    private double myDesiredErrorTol = 0.001;

    private double myIntervalLength = 1.0;

    public EstimateRenewals() {
        this(new ExponentialRV(1.0), 1.0, 0.001);
    }

    public EstimateRenewals(RVariableIfc d, double interval){
        this(d, interval, 0.001);
    }
    
    public EstimateRenewals(RVariableIfc d, double interval, double tol) {
        setRandomVariable(d);
        setInterval(interval);
        setTolerance(tol);
        myStat = new Statistic();
    }

    public final void setConfidenceLevel(double level) {
        myStat.setConfidenceLevel(level);
    }

    public final double getVariance() {
        return myStat.getVariance();
    }

    public final double getAverage() {
        return myStat.getAverage();
    }

    public final void setRandomVariable(RVariableIfc d) {
        if (d == null) {
            throw new IllegalArgumentException("The supplied distribution was null");
        }
        myRV = d;
    }

    public final void setInterval(double interval) {
        if (interval <= 0.0) {
            throw new IllegalArgumentException("The supplied interval was <= 0");
        }
        myIntervalLength = interval;
    }

    public final double getInterval(){
        return myIntervalLength;
    }

    public final void setTolerance(double tol) {
        if (tol <= 0.0) {
            throw new IllegalArgumentException("The supplied tolerance was <= 0");
        }
        myDesiredErrorTol = tol;
    }

    public final double getTolerance(){
        return myDesiredErrorTol;
    }
    
    public int getMaxIterations() {
        return myMaxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        myMaxIterations = maxIterations;
    }

    public int getMinIterations() {
        return myMinIterations;
    }

    public void setMinIterations(int minIterations) {
        myMinIterations = minIterations;
    }

    public final Statistic getStatistic(){
        return myStat.newInstance();
    }

    @Override
    public String toString(){
        return myStat.toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //PoissonProcess();
        EstimateRenewals test = new EstimateRenewals();
        test.setInterval(10.0);
        test.estimate();
        System.out.println(test);

    }

    private double generate() {
        double t = 0.0;
        double n = 0;
        do {
            t = t + myRV.getValue();
            if (t <= myIntervalLength) {
                n = n + 1;
            }
        } while (t <= myIntervalLength);
        return n;
    }

    public void estimate(){
        myStat.reset();
        // always do the min number of samples
        for(int i=1;i<=myMinIterations;i++){
            myStat.collect(generate());
        }
        int k = myMaxIterations - myMinIterations;
        for (int i=1;i<=k;i++){
            double hw = myStat.getHalfWidth();
            if (hw <= myDesiredErrorTol){
                break;
            }else{
                myStat.collect(generate());
            }
        }
    }

}
