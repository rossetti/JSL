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
import java.util.Objects;

/**
 *  An abstract base class for building random variables.  Implement
 *  the random generation procedure in the method generate().
 */
abstract public class AbstractRVariable implements RVariableIfc, IdentityIfc {

    private final Identity myIdentity;

    private double myPrevValue;

    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myRNStream;

    /**
     *
     * @param stream the source of the randomness
     * @throws NullPointerException if rng is null
     *
     */
    public AbstractRVariable(RNStreamIfc stream) {
        myRNStream = Objects.requireNonNull(stream,"RNStreamIfc stream must be non-null" );
        myIdentity = new Identity();
        myPrevValue = Double.NaN;
    }

    /** Makes a new instance.  False allows the new instance to keep using
     * the same underlying source of random numbers.
     *
     * @param newRNG true means use new stream. This is same as newInstance(). False
     *               means clone uses same underlying source of randomness
     * @return a new instance configured based on current instance
     */
    public final RVariableIfc newInstance(boolean newRNG){
        if (newRNG){
            return newInstance();
        } else {
            return newInstance(this.myRNStream);
        }
    }

    /**
     *
     * @return the randomly generated variate
     */
    abstract protected double generate();

    /** Sets the last (previous) randomly generated value. Used within sample()
     *
     * @param value the value to assign
     */
    protected final void setPreviousValue(double value){
        myPrevValue = value;
    }

    @Override
    public final double sample(){
        double x = generate();
        setPreviousValue(x);
        return x;
    }

    @Override
    public final double getValue(){
        return sample();
    }

    @Override
    public final double getPreviousValue() {
        return myPrevValue;
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

    @Override
    public final RVariableIfc newAntitheticInstance() {
        return newInstance(myRNStream.newAntitheticInstance());
    }

    @Override
    public final void resetStartStream() {
        myRNStream.resetStartStream();
    }

    @Override
    public final void resetStartSubstream() {
        myRNStream.resetStartSubstream();
    }

    @Override
    public final void advanceToNextSubstream() {
        myRNStream.advanceToNextSubstream();
    }

    @Override
    public final void setAntitheticOption(boolean flag) {
        myRNStream.setAntitheticOption(flag);
    }

    @Override
    public final boolean getAntitheticOption() {
        return myRNStream.getAntitheticOption();
    }

    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return (myRNStream);
    }

    @Override
    public final void setRandomNumberStream(RNStreamIfc stream) {
        myRNStream = Objects.requireNonNull(stream,"RNStreamIfc stream must be non-null" );
    }
}
