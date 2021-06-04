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
 *  JohnsonB(alpha1, alpha2, min, max) random variable
 */
public final class GeneralizedBetaRV extends AbstractRVariable {

    private final double myMin;

    private final double myMax;

    private final BetaRV myBeta;

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max){
        this(alpha1, alpha2, min, max, JSLRandom.nextRNStream());
    }

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max, int streamNum){
        this(alpha1, alpha2, min, max, JSLRandom.rnStream(streamNum));
    }

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max, RNStreamIfc rng){
        super(rng);
        myBeta = new BetaRV(alpha1, alpha2, rng);
        if (max <= min) {
            throw new IllegalArgumentException("the min must be < than the max");
        }
        myMin = min;
        myMax = max;
    }

    /**
     *
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final GeneralizedBetaRV newInstance(RNStreamIfc rng){
        return new GeneralizedBetaRV(getAlpha1(), getAlpha2(), myMin, myMax, rng);
    }

    @Override
    public String toString() {
        return "GeneralizedBetaRV{" +
                "alpha1=" + myBeta.getAlpha1() +
                ", alpha2=" + myBeta.getAlpha2() +
                ", min=" + myMin +
                ", max=" + myMax +
                '}';
    }

    /** Gets the lower limit
     * @return The lower limit
     */
    public final double getMinimum() {
        return (myMin);
    }

    /** Gets the upper limit
     * @return The upper limit
     */
    public final double getMaximum() {
        return (myMax);
    }

    /**
     *
     * @return the first shape parameter
     */
    public final double getAlpha1() {
        return myBeta.getAlpha1();
    }

    /**
     *
     * @return the second shape parameter
     */
    public final double getAlpha2() {
        return myBeta.getAlpha2();
    }

    @Override
    protected final double generate() {
        double x = myBeta.getValue();
        double v = myMin + (myMax - myMin) * x;
        return v;
    }

    /**
     * The keys are "alpha1" with default value 1.0,
     * "alpha2" with default value 1.0,  "min" with default value 0.0 and "max" with
     * default value 1.0
     *
     * @return a control for GeneralizeBeta random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.GeneralizedBeta) {
            @Override
            protected final void fillControls() {
                addDoubleControl("alpha1", 1.0);
                addDoubleControl("alpha2", 1.0);
                addDoubleControl("min", 0.0);
                addDoubleControl("max", 1.0);
                setName(RVariableIfc.RVType.GeneralizedBeta.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double alpha1 = getDoubleControl("alpha1");
                double alpha2 = getDoubleControl("alpha2");
                double min = getDoubleControl("min");
                double max = getDoubleControl("max");
                return new GeneralizedBetaRV(alpha1, alpha2, min, max, rnStream);
            }
        };
    }
}
