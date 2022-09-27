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

abstract public class AbstractMVRVariable implements MVRVariableIfc, IdentityIfc {

    private final Identity myIdentity;

    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myRNG;

    public AbstractMVRVariable(RNStreamIfc rng) {
        myIdentity = new Identity();
        setRandomNumberStream(rng);
    }

    @Override
    public final String getName() {
        return myIdentity.getName();
    }

    @Override
    public final int getId() {
        return myIdentity.getId();
    }

    /** Sets the name
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myIdentity.setName(str);
    }

    /**
     *
     * @return the underlying random number source
     */
    public final RNStreamIfc getRandomNumberStream() {
        return (myRNG);
    }

    /**
     * Sets the underlying random number source
     *
     * @param stream the reference to the random number generator, must not be null
     */
    public final void setRandomNumberStream(RNStreamIfc stream) {
        if (stream == null) {
            throw new NullPointerException("RngIfc rng must be non-null");
        }
        myRNG = stream;
    }

    @Override
    public final void resetStartStream() {
        myRNG.resetStartStream();
    }

    @Override
    public final void resetStartSubStream() {
        myRNG.resetStartSubStream();
    }

    @Override
    public final void advanceToNextSubStream() {
        myRNG.advanceToNextSubStream();
    }

    @Override
    public final void setAntitheticOption(boolean flag) {
        myRNG.setAntitheticOption(flag);
    }

    @Override
    public final boolean getAntitheticOption() {
        return myRNG.getAntitheticOption();
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myRNG.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myRNG.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myRNG.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myRNG.setResetStartStreamOption(b);
    }
}
