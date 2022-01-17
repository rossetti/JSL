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
 * discrete uniform(min, max) random variable
 */
public final class DUniformRV extends AbstractRVariable {

    private final int min;
    private final int max;

    public DUniformRV(int min, int max) {
        this(min, max, JSLRandom.nextRNStream());
    }

    public DUniformRV(int min, int max, int streamNum) {
        this(min, max, JSLRandom.rnStream(streamNum));
    }

    public DUniformRV(int min, int max, RNStreamIfc rng) {
        super(rng);
        if (min >= max) {
            throw new IllegalArgumentException("Lower limit must be < upper limit. lower limit = " + min + " upper limit = " + max);
        }
        this.min = min;
        this.max = max;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public DUniformRV newInstance(RNStreamIfc rng) {
        return new DUniformRV(this.min, this.max, rng);
    }

    @Override
    public String toString() {
        return "DUniformRV{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }

    /**
     * Gets the lower limit
     *
     * @return The lower limit
     */
    public double getMinimum() {
        return (min);
    }

    /**
     * Gets the upper limit
     *
     * @return The upper limit
     */
    public double getMaximum() {
        return (max);
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rDUniform(min, max, myRNStream);
        return v;
    }

    /**
     * The keys are "min" with default value 0 and "max" with
     * default value 1
     *
     * @return a control for DUniform random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.DUniform) {
            @Override
            protected final void fillControls() {
                addIntegerControl("min", 0);
                addIntegerControl("max", 1);
                setName(RVariableIfc.RVType.DUniform.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                int min = getIntegerControl("min");
                int max = getIntegerControl("max");
                return new DUniformRV(min, max, rnStream);
            }
        };
    }
}
