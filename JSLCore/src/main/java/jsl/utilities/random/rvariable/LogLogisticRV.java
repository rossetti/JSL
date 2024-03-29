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

/**
 * LogLogistic(shape, scale) random variable
 */
public final class LogLogisticRV extends ParameterizedRV {

    private final double myShape;
    private final double myScale;

    public LogLogisticRV(double shape, double scale) {
        this(shape, scale, JSLRandom.nextRNStream());
    }

    public LogLogisticRV(double shape, double scale, int streamNum) {
        this(shape, scale, JSLRandom.rnStream(streamNum));
    }

    public LogLogisticRV(double shape, double scale, RNStreamIfc rng) {
        super(rng);
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape parameter must be positive");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale parameter must be positive");
        }
        this.myShape = shape;
        this.myScale = scale;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public LogLogisticRV newInstance(RNStreamIfc rng) {
        return new LogLogisticRV(this.getShape(), this.getScale(), rng);
    }

    @Override
    public String toString() {
        return "LogLogisticRV{" +
                "shape=" + myShape +
                ", scale=" + myScale +
                '}';
    }

    /**
     * Gets the shape
     *
     * @return The shape parameter as a double
     */
    public double getShape() {
        return myShape;
    }

    /**
     * Gets the scale parameter
     *
     * @return The scale parameter as a double
     */
    public double getScale() {
        return myScale;
    }

    @Override
    protected double generate() {
        return JSLRandom.rLogLogistic(myShape, myScale, myRNStream);
    }

    /**
     * The parameter names are "shape" and "scale"
     *
     * @return the parameters for LogLogistic random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.LogLogisticRVParameters();
        parameters.changeDoubleParameter("shape", myShape);
        parameters.changeDoubleParameter("scale", myScale);
        return parameters;
    }

}
