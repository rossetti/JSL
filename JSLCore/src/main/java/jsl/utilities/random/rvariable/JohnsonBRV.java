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
 * JohnsonB(alpha1, alpha2, min, max) random variable
 */
public final class JohnsonBRV extends AbstractRVariable {

    private final double myAlpha1;

    private final double myAlpha2;

    private final double myMin;

    private final double myMax;

    public JohnsonBRV(double alpha1, double alpha2, double min, double max) {
        this(alpha1, alpha2, min, max, JSLRandom.nextRNStream());
    }

    public JohnsonBRV(double alpha1, double alpha2, double min, double max, int streamNum) {
        this(alpha1, alpha2, min, max, JSLRandom.rnStream(streamNum));
    }

    public JohnsonBRV(double alpha1, double alpha2, double min, double max, RNStreamIfc rng) {
        super(rng);
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("alpha2 must be > 0");
        }

        if (max <= min) {
            throw new IllegalArgumentException("the min must be < than the max");
        }
        myAlpha1 = alpha1;
        myAlpha2 = alpha2;
        myMin = min;
        myMax = max;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public JohnsonBRV newInstance(RNStreamIfc rng) {
        return new JohnsonBRV(getAlpha1(), getAlpha2(), myMin, myMax, rng);
    }

    @Override
    public String toString() {
        return "JohnsonBRV{" +
                "alpha1=" + myAlpha1 +
                ", alpha2=" + myAlpha2 +
                ", min=" + myMin +
                ", max=" + myMax +
                '}';
    }

    /**
     * Gets the lower limit
     *
     * @return The lower limit
     */
    public double getMinimum() {
        return (myMin);
    }

    /**
     * Gets the upper limit
     *
     * @return The upper limit
     */
    public double getMaximum() {
        return (myMax);
    }

    /**
     * @return the first shape parameter
     */
    public double getAlpha1() {
        return myAlpha1;
    }

    /**
     * @return the second shape parameter
     */
    public double getAlpha2() {
        return myAlpha2;
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rJohnsonB(myAlpha1, myAlpha2, myMin, myMax, myRNStream);
        return v;
    }

    /**
     * The keys are "alpha1" with default value 0.0,
     * "alpha2" with default value 1.0,  "min" with default value 0.0 and "max" with
     * default value 1.0
     *
     * @return a control for JohnsonB random variables
     */
    public static RVControls makeControls() {
        return new RVControls() {
            @Override
            protected final void fillControls() {
                addDoubleControl("alpha1", 0.0);
                addDoubleControl("alpha2", 1.0);
                addDoubleControl("min", 0.0);
                addDoubleControl("max", 1.0);
                setName(RVType.JohnsonB.name());
                setRVType(RVType.JohnsonB);
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double alpha1 = getDoubleControl("alpha1");
                double alpha2 = getDoubleControl("alpha2");
                double min = getDoubleControl("min");
                double max = getDoubleControl("max");
                return new JohnsonBRV(alpha1, alpha2, min, max, rnStream);
            }
        };
    }

}
