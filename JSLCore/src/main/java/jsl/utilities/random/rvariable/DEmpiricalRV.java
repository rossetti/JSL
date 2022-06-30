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

package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Arrays;

/**
 * Discrete Empirical Random Variable. Randomly selects from the supplied
 * values in the value array according to the supplied CDF array. The CDF array
 * must have valid probability elements and last element equal to 1.
 * Every element must be greater than or equal to the previous element in the CDF array.
 * That is, monotonically increasing.
 */
public final class DEmpiricalRV extends ParameterizedRV {

    private final double[] myValues;
    private final double[] myCDF;

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param values array to select from
     * @param cdf    the cumulative probability associated with each element of
     *               array
     */
    public DEmpiricalRV(double[] values, double[] cdf) {
        this(values, cdf, JSLRandom.nextRNStream());
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param values    array to select from
     * @param cdf       the cumulative probability associated with each element of
     *                  array
     * @param streamNum the stream number
     */
    public DEmpiricalRV(double[] values, double[] cdf, int streamNum) {
        this(values, cdf, JSLRandom.rnStream(streamNum));
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param values array to select from
     * @param cdf    the cumulative probability associated with each element of
     *               array
     * @param rng    the source of randomness
     */
    public DEmpiricalRV(double[] values, double[] cdf, RNStreamIfc rng) {
        super(rng);
        if (rng == null) {
            throw new IllegalArgumentException("The supplied RngIfc was null");
        }
        if (values == null) {
            throw new IllegalArgumentException("The supplied values array was null");
        }
        if (cdf == null) {
            throw new IllegalArgumentException("The supplied cdf was null");
        }
        if (!JSLRandom.isValidCDF(cdf)) {
            throw new IllegalArgumentException("The supplied cdf was not valid");
        }
        if (values.length != cdf.length) {
            throw new IllegalArgumentException("The arrays did not have the same length.");
        }
        myValues = Arrays.copyOf(values, values.length);
        myCDF = Arrays.copyOf(cdf, cdf.length);
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public DEmpiricalRV newInstance(RNStreamIfc rng) {
        return new DEmpiricalRV(this.myValues, this.myCDF, rng);
    }

    /**
     * @return the values to select from
     */
    public double[] getValues() {
        return Arrays.copyOf(myValues, myValues.length);
    }

    /**
     * @return the cdf to select with
     */
    public double[] getCDF() {
        return Arrays.copyOf(myCDF, myCDF.length);
    }

    @Override
    public String toString() {
        return "DEmpiricalRV{" +
                "values=" + Arrays.toString(myValues) +
                ", cdf=" + Arrays.toString(myCDF) +
                '}';
    }

    @Override
    protected double generate() {
        if (myCDF.length == 1) {
            return myValues[0];
        }
        int i = 0;
        double value = myValues[i];
        double u = myRNStream.randU01();
        while (myCDF[i] <= u) {
            i = i + 1;
            value = myValues[i];
        }
        return value;
    }

    /**
     * The parameter names are "values" and "cdf"
     *
     * @return the parameters for DEmpirical random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.DEmpiricalRVParameters();
        parameters.changeDoubleArrayParameter("values", myValues);
        parameters.changeDoubleArrayParameter("cdf", myCDF);
        return parameters;
    }

}
