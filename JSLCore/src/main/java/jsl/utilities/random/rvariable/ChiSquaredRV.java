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
 * Chi-Squared(degrees of freedom) random variable
 */
public final class ChiSquaredRV extends AbstractRVariable {

    private final double dof;

    public ChiSquaredRV(double dof) {
        this(dof, JSLRandom.nextRNStream());
    }

    public ChiSquaredRV(double dof, int streamNum) {
        this(dof, JSLRandom.rnStream(streamNum));
    }

    public ChiSquaredRV(double dof, RNStreamIfc rng) {
        super(rng);
        if (dof <= 0.0) {
            throw new IllegalArgumentException("Chi-Squared degrees of freedom must be > 0.0");
        }
        this.dof = dof;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public ChiSquaredRV newInstance(RNStreamIfc rng) {
        return new ChiSquaredRV(this.dof, rng);
    }

    @Override
    public String toString() {
        return "ChiSquaredRV{" +
                "dof=" + dof +
                '}';
    }

    /**
     * @return the dof value
     */
    public double getDegreesOfFreedom() {
        return dof;
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rChiSquared(dof, myRNStream);
        return v;
    }

    /**
     * The keys are "dof", the default value is 1.0
     *
     * @return a control for Chi-Squared random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.ChiSquared) {
            @Override
            protected final void fillControls() {
                addDoubleControl("dof", 1.0);
                setName(RVariableIfc.RVType.ChiSquared.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double dof = getDoubleControl("dof");
                return new ChiSquaredRV(dof, rnStream);
            }
        };
    }

}
