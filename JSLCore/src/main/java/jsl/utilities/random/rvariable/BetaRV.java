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

import jsl.utilities.distributions.Beta;
import jsl.utilities.random.rng.RNStreamIfc;

/**
 *  Beta(alpha1, alpha2) random variable, range (0,1)
 */
public final class BetaRV extends AbstractRVariable {

    private final Beta myBeta;

    public BetaRV(double alpha1, double alpha2){
        this(alpha1, alpha2, JSLRandom.nextRNStream());
    }

    public BetaRV(double alpha1, double alpha2, int streamNum){
        this(alpha1, alpha2, JSLRandom.rnStream(streamNum));
    }

    public BetaRV(double alpha1, double alpha2, RNStreamIfc rng){
        super(rng);
        myBeta = new Beta(alpha1, alpha2);
    }

    /**
     *
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final BetaRV newInstance(RNStreamIfc rng){
        return new BetaRV(getAlpha1(), getAlpha2(), rng);
    }

    @Override
    public String toString() {
        return "BetaRV{" +
                "alpha1=" + myBeta.getAlpha1() +
                ", alpha2=" + myBeta.getAlpha2() +
                '}';
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
        double v = myBeta.invCDF(myRNStream.randU01());
        return v;
    }

    /**
     * The keys are "alpha1", the default value is 1.0 and
     * "alpha2" with default value 1.0.
     *
     * @return a control for Beta random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.Beta) {
            @Override
            protected final void fillControls() {
                addDoubleControl("alpha1", 1.0);
                addDoubleControl("alpha2", 1.0);
                setName(RVariableIfc.RVType.Beta.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double alpha1 = getDoubleControl("alpha1");
                double alpha2 = getDoubleControl("alpha2");
                return new BetaRV(alpha1, alpha2, rnStream);
            }
        };
    }
    
}
