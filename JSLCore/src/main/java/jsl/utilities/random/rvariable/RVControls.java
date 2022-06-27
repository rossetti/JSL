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

import jsl.utilities.controls.Controls;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.reporting.JSONUtil;

import java.util.Objects;

public abstract class RVControls extends Controls {

    private RVariableIfc.RVType type;

    public RVControls() {
        super();
    }

    /**
     * @return the type of the random variable
     */
    public final RVariableIfc.RVType getType() {
        return type;
    }

    /** Used internally to set the type
     *
     * @param type the type
     */
    final void setRVType(RVariableIfc.RVType type){
        Objects.requireNonNull(type, "The supplied type was null");
        this.type = type;
    }

    /**
     * @return an instance of the random variable based on the current control parameters,
     * with a new stream
     */
    public final RVariableIfc makeRVariable() {
        return makeRVariable(JSLRandom.nextRNStream());
    }

    /**
     * @param streamNumber a number representing the desired stream based on the RNStreamProvider
     * @return an instance of the random variable based on the current control parameters using the designated
     * stream number
     */
    public final RVariableIfc makeRVariable(int streamNumber) {
        return makeRVariable(JSLRandom.rnStream(streamNumber));
    }

    /**
     * @param rnStream the stream to use
     * @return an instance of the random variable based on the current control parameters
     */
    abstract public RVariableIfc makeRVariable(RNStreamIfc rnStream);

}
