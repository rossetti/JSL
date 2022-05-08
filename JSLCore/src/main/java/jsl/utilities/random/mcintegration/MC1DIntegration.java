package jsl.utilities.random.mcintegration;

import jsl.utilities.Interval;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

public class MC1DIntegration {

    private int initialSampleSize = 50;
    private int maxSampleSize = 20000;
    private double desiredRelPrecision = 0.00001;
    private boolean resetStreamOption = false;
    private final Statistic statistic = new Statistic();
    private final Interval myInterval;
    private final MC1DFunctionIfc myFunction;
    private final MC1DRVariableIfc mySampler;

    public MC1DIntegration(Interval interval, MC1DFunctionIfc function, MC1DRVariableIfc sampler) {
        Objects.requireNonNull(interval, "The interval was null!");
        Objects.requireNonNull(sampler, "The MC1DRVariableIfc was null!");
        Objects.requireNonNull(function, "The MC1DFunctionIfc was null!");
        if (!interval.equals(sampler.getRange())){
            throw new IllegalArgumentException("The sampler does not have the same range as the integration interval!");
        }
        if(!sampler.getRange().equals(function.getDomain())){
            throw new IllegalArgumentException("The sampler's range does not match the domain of the function being integrated!");
        }
        this.myInterval = interval;
        this.myFunction = function;
        this.mySampler = sampler;
    }

    public double evaluate(){
        statistic.reset();
        if (resetStreamOption){
            mySampler.resetStartStream();
        }
        runInitialSample();
        double estimate = runSample();

        return estimate;
    }

    protected double runSample() {

        return 0.0;
    }

    /**
     *  The purpose of the initial sample is to estimate the variability
     *  and determine an approximate number of additional samples needed
     *  to meet the desired relative precision.
     */
    protected void runInitialSample() {
    }

    /**
     *  Sample for n evaluations or until relative precision criterion is met
     * @param n the number of samples to take
     */
    protected void sample(int n){
        
    }

    public int getInitialSampleSize() {
        return initialSampleSize;
    }

    public void setInitialSampleSize(int initialSampleSize) {
        if (initialSampleSize < 2.0) {
            throw new IllegalArgumentException("The initial sample size must be >= 2");
        }
        this.initialSampleSize = initialSampleSize;
    }

    public int getMaxSampleSize() {
        return maxSampleSize;
    }

    public void setMaxSampleSize(int maxSampleSize) {
        if (maxSampleSize < initialSampleSize) {
            throw new IllegalArgumentException("The maximum sample size must be >= " + initialSampleSize);
        }
        this.maxSampleSize = maxSampleSize;
    }

    public double getDesiredRelPrecision() {
        return desiredRelPrecision;
    }

    public void setDesiredRelPrecision(double desiredRelPrecision) {
        if (desiredRelPrecision <= 0.0) {
            throw new IllegalArgumentException("The desired relative precision must be > 0.0");
        }
        this.desiredRelPrecision = desiredRelPrecision;
    }

    public boolean isResetStreamOption() {
        return resetStreamOption;
    }

    public void setResetStreamOption(boolean resetStreamOption) {
        this.resetStreamOption = resetStreamOption;
    }
}
