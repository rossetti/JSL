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
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.InverseCDFRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.ShiftedRV;

/**
 * Represents a Distribution that has been Shifted (translated to the right)
 * The shift must be &gt;= 0.0
 */
public class ShiftedDistribution extends Distribution implements LossFunctionDistributionIfc {

    protected DistributionIfc myDistribution;

    protected LossFunctionDistributionIfc myLossFunctionDistribution;

    protected double myShift;

    /**
     * Constructs a shifted distribution based on the provided distribution
     *
     * @param distribution the distribution to shift
     * @param shift        The linear shift
     */
    public ShiftedDistribution(DistributionIfc distribution, double shift) {
        this(distribution, shift, null);
    }

    /**
     * Constructs a shifted distribution based on t he provided distribution
     *
     * @param distribution the distribution to shift
     * @param shift        The linear shift
     * @param name         an optional name/label
     */
    public ShiftedDistribution(DistributionIfc distribution, double shift, String name) {
        super(name);
        setDistribution(distribution, shift);
    }

    @Override
    public final ShiftedDistribution newInstance() {
        DistributionIfc d = (DistributionIfc) myDistribution.newInstance();
        return (new ShiftedDistribution(d, myShift));
    }

    /**
     * Changes the underlying distribution and the shift
     *
     * @param distribution must not be null
     * @param shift        must be &gt;=0.0
     */
    public final void setDistribution(DistributionIfc distribution, double shift) {
        if (distribution == null) {
            throw new IllegalArgumentException("The distribution must not be null");
        }
        myDistribution = distribution;
        setShift(shift);
    }

    /**
     * Changes the shift
     *
     * @param shift must be &gt;=0.0
     */
    public final void setShift(double shift) {
        if (shift < 0.0) {
            throw new IllegalArgumentException("The shift should not be < 0.0");
        }
        myShift = shift;
    }

    /**
     * Sets the parameters of the shifted distribution
     * shift = parameter[0]
     * If supplied, the other elements of the array are used in setting the
     * parameters of the underlying distribution.  If only the shift is supplied
     * as a parameter, then the underlying distribution's parameters are not changed
     * (and do not need to be supplied)
     */
    public final void setParameters(double[] parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("The parameters array was null");
        }
        setShift(parameters[0]);
        if (parameters.length == 1) {
            return;
        }

        double[] y = new double[parameters.length - 1];

        for (int i = 0; i < y.length; i++) {
            y[i] = parameters[i + 1];
        }
        myDistribution.setParameters(y);
    }

    @Override
    public final double cdf(double x) {
        if (x < myShift) {
            return 0.0;
        } else {
            return myDistribution.cdf(x - myShift);
        }
    }

    @Override
    public final double getMean() {
        return myShift + myDistribution.getMean();
    }

    /**
     * Gets the parameters for the shifted distribution
     * shift = parameter[0]
     * The other elements of the returned array are
     * the parameters of the underlying distribution
     */
    public final double[] getParameters() {
        double[] x = myDistribution.getParameters();
        double[] y = new double[x.length + 1];

        y[0] = myShift;
        for (int i = 0; i < x.length; i++) {
            y[i + 1] = x[i];
        }
        return y;
    }

    @Override
    public double getVariance() {
        return myDistribution.getVariance();
    }

    @Override
    public double invCDF(double p) {
        return (myDistribution.invCDF(p) + myShift);
    }

    @Override
    public double firstOrderLossFunction(double x) {
        LossFunctionDistributionIfc cdf = (LossFunctionDistributionIfc) myDistribution;
        return cdf.firstOrderLossFunction(x - myShift);
    }

    @Override
    public double secondOrderLossFunction(double x) {
        LossFunctionDistributionIfc cdf = (LossFunctionDistributionIfc) myDistribution;
        return cdf.secondOrderLossFunction(x - myShift);
    }

    public double thirdOrderLossFunction(double x) {
        Poisson first = (Poisson) myDistribution;
        return first.thirdOrderLossFunction(x - myShift);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new InverseCDFRV(newInstance(), rng);
    }
}
