/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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

import jsl.utilities.distributions.InverseCDFIfc;
import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Objects;

/**
 *  Facilitates the creation of random variables from distributions that implement InverseCDFIfc
 */
public class InverseCDFRV extends AbstractRVariable {

    private final InverseCDFIfc myInverse;

    /** Makes one using the next stream from the underlying stream provider
     *
     * @param invFun the inverse of the distribution function, must not be null
     */
    public InverseCDFRV(InverseCDFIfc invFun) {
        this(invFun, JSLRandom.nextRNStream());
    }

    /** Makes one using the supplied stream number to assign the stream
     *
     * @param invFun the inverse of the distribution function, must not be null
     * @param streamNum a positive integer
     */
    public InverseCDFRV(InverseCDFIfc invFun, int streamNum){
        this(invFun, JSLRandom.rnStream(streamNum));
    }

    /**
     *
     * @param invFun the inverse of the distribution function, must not be null
     * @param rng a random number stream, must not be null
     */
    public InverseCDFRV(InverseCDFIfc invFun, RNStreamIfc rng){
        super(rng);
        Objects.requireNonNull(invFun, "The supplied inverse function was null");
        myInverse = invFun;
    }

    @Override
    protected double generate() {
        return myInverse.invCDF(myRNStream.randU01());
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new InverseCDFRV(this.myInverse, rng);
    }
}
