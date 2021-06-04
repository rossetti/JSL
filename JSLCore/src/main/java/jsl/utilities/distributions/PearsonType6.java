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
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.PearsonType6RV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 * Represents a Pearson Type VI distribution,
 * see Law (2007) Simulation Modeling and Analysis, McGraw-Hill, pg 294
 * <p>
 * Code contributed by Nabil Lehlou
 */
public class PearsonType6 extends Distribution implements ContinuousDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private double myAlpha1;

    private double myAlpha2;

    private double myBeta;

    private Beta myBetaCDF;

    private double myBetaA1A2;

    /**
     * Creates a PearsonTypeVI distribution with
     * alpha1 = 2.0
     * alpha2 = 3.0
     * beta = 1.0
     */
    public PearsonType6() {
        this(2.0, 3.0, 1.0, null);
    }

    /**
     * Creates a PearsonTypeVI distribution
     *
     * @param alpha1 shape 1
     * @param alpha2 shape 2
     * @param beta   scale
     */
    public PearsonType6(double alpha1, double alpha2, double beta) {
        this(alpha1, alpha2, beta, null);
    }

    /**
     * Creates a PearsonTypeVI distribution
     * <p>
     * parameters[0] = alpha1
     * parameters[1] = alpha2
     * parameters[2] = beta
     *
     * @param parameters the parameter array
     */
    public PearsonType6(double[] parameters) {
        this(parameters[0], parameters[1], parameters[2], null);
    }

    /**
     * Creates a PearsonTypeVI distribution
     *
     * @param alpha1 shape 1
     * @param alpha2 shape 2
     * @param beta   scale
     * @param name   an optional name/label
     */
    public PearsonType6(double alpha1, double alpha2, double beta, String name) {
        super(name);
        setParameters(alpha1, alpha2, beta);
    }

    @Override
    public final PearsonType6 newInstance() {
        return (new PearsonType6(getParameters()));
    }

    @Override
    public final Interval getDomain() {
        return new Interval(0, Double.POSITIVE_INFINITY);
    }

    /**
     * @param alpha1 shape 1
     * @param alpha2 shape 2
     * @param beta   scale
     */
    public final void setParameters(double alpha1, double alpha2, double beta) {
        setScale(beta);
        setShapeParameters(alpha1, alpha2);
    }

    /**
     * @param alpha1 shape 1
     * @param alpha2 shape 2
     */
    public final void setShapeParameters(double alpha1, double alpha2) {
        if (alpha1 <= 0.0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0.0");
        }
        if (alpha2 <= 0.0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0.0");
        }

        myAlpha1 = alpha1;
        myAlpha2 = alpha2;
        myBetaA1A2 = Beta.betaFunction(myAlpha1, myAlpha2);
        if (myBetaCDF == null) {
            myBetaCDF = new Beta(alpha1, alpha2);
        } else {
            myBetaCDF.setParameters(alpha1, alpha2);
        }
    }

    /**
     * @param beta the scale
     */
    public void setScale(double beta) {
        if (beta <= 0.0) {
            throw new IllegalArgumentException("The scale parameter must be > 0.0");
        }

        myBeta = beta;
    }

    /**
     * params[0] = alpha1
     * params[1] = alpha2
     * params[2] = beta
     *
     * @param params the parameter array
     */
    @Override
    public void setParameters(double[] params) {
        setParameters(params[0], params[1], params[2]);
    }

    /**
     * params[0] = alpha1
     * params[1] = alpha2
     * params[2] = beta
     *
     * @return the parameter array
     */
    @Override
    public double[] getParameters() {
        return new double[]{myAlpha1, myAlpha2, myBeta};
    }

    /**
     * @return
     */
    @Override
    public double pdf(double x) {
        if (x <= 0) {
            return 0;
        }
        return (Math.pow(x / myBeta, myAlpha1 - 1.0)) / (myBeta * myBetaA1A2 * Math.pow(1.0 + x / myBeta, myAlpha1 + myAlpha2));
    }

    @Override
    public double cdf(double x) {
        if (x <= 0) {
            return 0;
        }
        return myBetaCDF.cdf(x / (x + myBeta));
    }

    @Override
    public double invCDF(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }

        double fib = myBetaCDF.invCDF(p);
        return (myBeta * fib) / (1.0 - fib);
    }

    /**
     * @return Returns the mean or Double.NaN if alpha2 &lt;= 1.0
     */
    @Override
    public double getMean() {
        if (myAlpha2 <= 1) {
            return Double.NaN;
        }

        return myBeta * myAlpha1 / (myAlpha2 - 1);
    }

    /**
     * @return Returns the variance or Double.NaN if alpha2 &lt;= 2.0
     */
    @Override
    public double getVariance() {
        if (myAlpha2 <= 2) {
            return Double.NaN;
        }

        return myBeta * myBeta * myAlpha1 * (myAlpha1 + myAlpha2 - 1) / ((myAlpha2 - 2) * (myAlpha2 - 1.0) * (myAlpha2 - 1.0));
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new PearsonType6RV(myAlpha1, myAlpha2, myBeta, rng);
    }
}
