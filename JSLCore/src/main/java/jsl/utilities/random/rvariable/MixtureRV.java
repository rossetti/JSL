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

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.robj.DEmpiricalList;

import java.util.ArrayList;
import java.util.List;

public final class MixtureRV extends AbstractRVariable {

    private final DEmpiricalList<RVariableIfc> myRVList;

    /**
     * @param list a list holding the random variables to select from
     * @param cdf  the cumulative probability associated with each element of the list
     */
    public MixtureRV(List<RVariableIfc> list, double[] cdf) {
        this(list, cdf, JSLRandom.nextRNStream());
    }

    /**
     * @param list      a list holding the random variables to select from
     * @param cdf       the cumulative probability associated with each element of the list
     * @param streamNum the stream number
     */
    public MixtureRV(List<RVariableIfc> list, double[] cdf, int streamNum) {
        this(list, cdf, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param list   a list holding the random variables to select from
     * @param cdf    the cumulative probability associated with each element of the list
     * @param stream the source of the randomness
     * @throws NullPointerException if stream is null
     */
    public MixtureRV(List<RVariableIfc> list, double[] cdf, RNStreamIfc stream) {
        super(stream);
        myRVList = new DEmpiricalList<>(list, cdf, stream);
    }

    @Override
    protected double generate() {
        return myRVList.getRandomElement().getValue();
    }

    @Override
    public MixtureRV newInstance(RNStreamIfc rng) {
        List<RVariableIfc> list = new ArrayList<>(myRVList.getList());
        return new MixtureRV(list, myRVList.getCDF(), rng);
    }
}
