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

import jsl.utilities.Identity;
import jsl.utilities.IdentityIfc;
import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Allows a constant to pretend to be a random variable
 */
public class ConstantRV implements RVariableIfc, IdentityIfc {

    /**
     * A constant to represent zero for sharing
     */
    public final static ConstantRV ZERO = new ConstantRV(0.0);
    /**
     * A constant to represent one for sharing
     */
    public final static ConstantRV ONE = new ConstantRV(1.0);

    /**
     * A constant to represent two for sharing
     */
    public final static ConstantRV TWO = new ConstantRV(2.0);

    /**
     * A constant to represent positive infinity for sharing
     */
    public final static ConstantRV POSITIVE_INFINITY = new ConstantRV(Double.POSITIVE_INFINITY);

    protected double myValue;
    private final Identity myIdentity;

    public ConstantRV(double value) {
        myIdentity = new Identity();
        myValue = value;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final ConstantRV newInstance(RNStreamIfc rng) {
        return new ConstantRV(myValue);
    }

    /**
     * @return a new instance with same parameter value
     */
    public final ConstantRV newInstance() {
        return new ConstantRV(myValue);
    }

    @Override
    public String toString() {
        return "ConstantRV{" +
                "value=" + myValue +
                '}';
    }

    @Override
    public final String getName() {
        return myIdentity.getName();
    }

    @Override
    public final int getId() {
        return myIdentity.getId();
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myIdentity.setName(str);
    }


    @Override
    public final double sample() {
        return myValue;
    }

    @Override
    public final void resetStartStream() {

    }

    @Override
    public final void resetStartSubstream() {

    }

    @Override
    public final void advanceToNextSubstream() {

    }

    @Override
    public final void setAntitheticOption(boolean flag) {

    }

    @Override
    public final boolean getAntitheticOption() {
        return false;
    }

    @Override
    public double getPreviousValue() {
        return myValue;
    }

    @Override
    public final RVariableIfc newAntitheticInstance() {
        return new ConstantRV(myValue);
    }

    /**
     * The keys are "value", the default value is 1.0
     *
     * @return a control for Constant random variables
     */
    public static RVControls makeControls() {
        return new RVControls() {
            @Override
            protected final void fillControls() {
                addDoubleControl("value", 1.0);
                setName(RVariableIfc.RVType.Constant.name());
                setRVType(RVariableIfc.RVType.Constant);
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double value = getDoubleControl("value");
                return new ConstantRV(value);
            }
        };
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return JSLRandom.getDefaultRNStream();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {

    }
}
