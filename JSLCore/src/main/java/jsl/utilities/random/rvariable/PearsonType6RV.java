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
 * Pearson Type 6(alpha1, alpha2, beta) random variable
 */
public final class PearsonType6RV extends AbstractRVariable {

    private final double myAlpha1;
    private final double myAlpha2;
    private final double myBeta;

    public PearsonType6RV(double alpha1, double alpha2, double beta) {
        this(alpha1, alpha2, beta, JSLRandom.nextRNStream());
    }

    public PearsonType6RV(double alpha1, double alpha2, double beta, int streamNum) {
        this(alpha1, alpha2, beta, JSLRandom.rnStream(streamNum));
    }

    public PearsonType6RV(double alpha1, double alpha2, double beta, RNStreamIfc rng) {
        super(rng);
        if (alpha1 <= 0.0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0.0");
        }
        if (alpha2 <= 0.0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0.0");
        }
        if (beta <= 0.0) {
            throw new IllegalArgumentException("The scale parameter must be > 0.0");
        }
        myAlpha1 = alpha1;
        myAlpha2 = alpha2;
        myBeta = beta;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public PearsonType6RV newInstance(RNStreamIfc rng) {
        return new PearsonType6RV(getAlpha1(), getAlpha2(), myBeta, rng);
    }

    @Override
    public String toString() {
        return "PearsonType6RV{" +
                "alpha1=" + myAlpha1 +
                ", alpha2=" + myAlpha2 +
                ", beta=" + myBeta +
                '}';
    }

    public double getAlpha1() {
        return myAlpha1;
    }

    public double getAlpha2() {
        return myAlpha2;
    }

    public double getBeta() {
        return myBeta;
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rPearsonType6(myAlpha1, myAlpha2, myBeta, myRNStream);
        return v;
    }

    /**
     * The keys are "alpha1" with default value 2.0 and "alpha2" with
     * default value 3.0, and "beta" with default value 1.0
     *
     * @return a control for PearsonType6 random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.PearsonType6) {
            @Override
            protected final void fillControls() {
                addDoubleControl("alpha1", 2.0);
                addDoubleControl("alpha2", 3.0);
                addDoubleControl("beta", 1.0);
                setName(RVariableIfc.RVType.PearsonType6.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double alpha1 = getDoubleControl("alpha1");
                double alpha2 = getDoubleControl("alpha2");
                double beta = getDoubleControl("beta");
                return new PearsonType6RV(alpha1, alpha2, beta, rnStream);
            }
        };
    }
}
