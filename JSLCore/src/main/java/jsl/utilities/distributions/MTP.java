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
import jsl.utilities.random.rvariable.InverseCDFRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** Represents a Mixed translated Poisson random variable
 * 
 * 
 *
 */
public class MTP extends Distribution implements LossFunctionDistributionIfc {

    protected double myMixProb1;

    protected double myMixProb2;

    protected double myMean;

    protected ShiftedLossFunctionDistribution SD1;

    protected ShiftedLossFunctionDistribution SD2;

    double[] parameter;

    /**
     *  defaults mixing prob = 0.5, shift = 0.0, rate = 1.0
     */
    public MTP() {
        this(0.5, 0.0, 1.0, null);
    }

    /** Constructs an MTP with mixing probabilities 1-mixProb and mixProb with shifts of
     * shift and shift+1 with rates equal to rate
     * @param mixProb the mixing probability
     * @param shift the shift
     * @param rate the rate
     */
    public MTP(double mixProb, double shift, double rate) {
        this(mixProb, shift, rate, null);
    }

    /** Constructs an MTP using the supplied parameters
     *
     * @param mixProb the mixing probability
     * @param shift the shift
     * @param rate the rate
     * @param name an optional name/label
     */
    public MTP(double mixProb, double shift, double rate, String name) {
        super(name);
        myMixProb1 = mixProb;
        myMixProb2 = 1 - myMixProb1;
        myMean = rate;
        //System.out.println("Mean1: "+myMean1);
        //System.out.println("Mean2: "+myMean2);
        SD1 = new ShiftedLossFunctionDistribution(new Poisson(rate), shift);
        SD2 = new ShiftedLossFunctionDistribution(new Poisson(rate), shift + 1);
    }

    /** Constructs an MTP with array of parameters
     *
     *
     * parameters[0] - mixing probability;
     * parameters[1] - shift;
     * parameters[2] - rate;
     * @param parameters the parameter array
     */
    public MTP(double[] parameters) {
        this(parameters[0], parameters[1], parameters[2], null);
    }

    /** Returns a new instance of the random source with the same parameters
     *  but an independent generator
     *
     * @return a new instance with the same parameters
     */
    public final MTP newInstance() {
        return (new MTP(getParameters()));
    }

    @Override
    public double cdf(double x) {
        return myMixProb1 * SD1.cdf(x) + (myMixProb2 * SD2.cdf(x));
    }

    @Override
    public double getMean() {

        return myMixProb1 * SD1.getMean() + myMixProb2 * SD2.getMean();
    }

    @Override
    public double[] getParameters() {
        double[] x = SD1.myDistribution.getParameters();
        double[] y = SD1.getParameters();
        double[] z = new double[x.length + 2];

        z[0] = myMixProb1;
        z[1] = y[0];
        z[2] = x[0];

        return z;
    }

    @Override
    public double getVariance() {
        return myMixProb1 * ((SD1.getMean() * SD1.getMean()) + myMean) + myMixProb2 * ((SD2.getMean() * SD2.getMean()) + myMean) - (getMean() * getMean());
    }

    @Override
    public double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be [0,1)");
        }
        //if(JSLMath.equal(p, 1.0))
        //throw new IllegalArgumentException("Supplied probability was 1.0 Probability must be [0,1)");

        int i = 0;
        while (p > cdf(i)) {
            i++;
        }
        return i;
    }

    @Override
    public void setParameters(double[] parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("The parameters array was null");
        }
        setParameters(parameters[2], parameters[1], parameters[0]);
    }

    @Override
    public double complementaryCDF(double x) {
        return myMixProb1 * SD1.complementaryCDF(x) + myMixProb2 * SD2.complementaryCDF(x);
    }

    @Override
    public double firstOrderLossFunction(double x) {

        return myMixProb1 * SD1.firstOrderLossFunction(x) + myMixProb2 * SD2.firstOrderLossFunction(x);
    }

    @Override
    public double secondOrderLossFunction(double x) {

        return myMixProb1 * SD1.secondOrderLossFunction(x) + myMixProb2 * SD2.secondOrderLossFunction(x);
    }

    public double thirdOrderLossFunction(double x) {
        return myMixProb1 * SD1.thirdOrderLossFunction(x) + myMixProb2 * SD2.thirdOrderLossFunction(x);
    }

    /**
     *
     * @param rate the rate
     * @param shift the shift
     * @param mixProbability the mixing probability
     */
    public void setParameters(double rate, double shift, double mixProbability) {
        if (rate <= 0.0) {
            throw new IllegalArgumentException("Rate should be > 0.0");
        }
        if (shift < 0.0) {
            throw new IllegalArgumentException("shift should be >= 0.0");
        }
        if (mixProbability > 1.0) {
            throw new IllegalArgumentException("Mixing probability should be between 0 and 1");
        }
        SD1.setShift(shift);
        SD2.setShift(shift + 1);
        myMixProb1 = mixProbability;
        myMixProb2 = 1 - myMixProb1;
        parameter = new double[1];
        parameter[0] = rate;
        SD1.myDistribution.setParameters(parameter);
        SD2.myDistribution.setParameters(parameter);
        myMean = rate;
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new InverseCDFRV(newInstance(), rng);
    }

    public static void main(String[] args) {

        MTP mtp = new MTP(0.5, 1, 0.1);

        System.out.println("Second order loss function: " + mtp.secondOrderLossFunction(2));
    }
}
