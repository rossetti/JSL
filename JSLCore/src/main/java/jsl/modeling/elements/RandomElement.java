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
package jsl.modeling.elements;

import java.util.Collection;
import java.util.List;

import jsl.simulation.ModelElement;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.robj.DEmpiricalList;

/**
 * RandomElement allows for randomly selecting objects of type T
 * according to a DEmpiricalList.  This essentially allows DEmpiricalList to
 * be a ModelElement
 */
public class RandomElement<T> extends ModelElement implements RandomElementIfc {

    /**
     * indicates whether or not the random variable's
     * distribution has it stream reset to the default
     * stream, or not prior to each experiment.  Resetting
     * allows each experiment to use the same underlying random numbers
     * i.e. common random numbers, this is the default
     * <p>
     * Setting it to true indicates that it does reset
     */
    protected boolean myResetStartStreamOption;

    /**
     * indicates whether or not the random variable's
     * distribution has it stream reset to the next substream
     * stream, or not, prior to each replication.  Resetting
     * allows each replication to better ensure that each
     * replication will be start at the same place in the
     * substreams, thereby, improving sychronization when using
     * common random numbers.
     * <p>
     * Setting it to true indicates that it does jump to
     * the next substream, true is the default
     */
    protected boolean myResetNextSubStreamOption;

    protected DEmpiricalList<T> myRandomList;

    /**
     * @param parent
     */
    public RandomElement(ModelElement parent, List<T> elements, double[] cdf) {
        this(parent, elements, cdf, null);
    }

    /**
     * @param parent
     * @param name
     */
    public RandomElement(ModelElement parent, List<T> elements, double[] cdf, String name) {
        super(parent, name);
        myRandomList = new DEmpiricalList<T>(elements, cdf);
        setWarmUpOption(false); // do not need to respond to warm events
        setResetStartStreamOption(true);
        setResetNextSubStreamOption(true);
    }

    /**
     * Gets the current Reset Start Stream Option
     *
     * @return
     */
    @Override
    public final boolean getResetStartStreamOption() {
        return myResetStartStreamOption;
    }

    /**
     * Sets the reset start stream option, true
     * means that it will be reset to the starting stream
     *
     * @param b
     */
    @Override
    public final void setResetStartStreamOption(boolean b) {
        myResetStartStreamOption = b;
    }

    /**
     * Gets the current reset next substream option
     * true means, that it is set to jump to the next substream after
     * each replication
     *
     * @return
     */
    @Override
    public final boolean getResetNextSubStreamOption() {
        return myResetNextSubStreamOption;
    }

    /**
     * Sets the current reset next substream option
     * true means, that it is set to jump to the next substream after
     * each replication
     *
     * @param b
     */
    @Override
    public final void setResetNextSubStreamOption(boolean b) {
        myResetNextSubStreamOption = b;
    }

    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return myRandomList.getRandomNumberStream();
    }

    @Override
    public final void setRandomNumberStream(RNStreamIfc stream) {
        myRandomList.setRandomNumberStream(stream);
    }

    /**
     * @return
     * @see jsl.utilities.random.robj.DEmpiricalList#getRandomElement()
     */
    public final T getRandomElement() {
        return myRandomList.getRandomElement();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#contains(java.lang.Object)
     */
    public final boolean contains(Object arg0) {
        return myRandomList.contains(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public final boolean containsAll(Collection<?> arg0) {
        return myRandomList.containsAll(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public final int indexOf(Object arg0) {
        return myRandomList.indexOf(arg0);
    }

    /**
     * @return
     * @see java.util.List#isEmpty()
     */
    public final boolean isEmpty() {
        return myRandomList.isEmpty();
    }

    /**
     * @return
     * @see java.util.List#size()
     */
    public final int size() {
        return myRandomList.size();
    }

    /**
     * Returns an unmodifiable view of the list of elements
     *
     * @return
     */
    public final List<T> getList() {
        return (myRandomList.getList());
    }

    @Override
    public final void advanceToNextSubstream() {
        myRandomList.advanceToNextSubstream();
    }

    @Override
    public final void resetStartStream() {
        myRandomList.resetStartStream();
    }

    @Override
    public final void resetStartSubstream() {
        myRandomList.resetStartSubstream();
    }

    @Override
    public final void setAntitheticOption(boolean flag) {
        myRandomList.setAntitheticOption(flag);
    }

    @Override
    public final boolean getAntitheticOption() {
        return myRandomList.getAntitheticOption();
    }

    /**
     * before any replications reset the underlying random number generator to the
     * starting stream
     */
    @Override
    protected void beforeExperiment() {
        super.beforeExperiment();
        if (getResetStartStreamOption()) {
            resetStartStream();
        }

    }

    /**
     * after each replication reset the underlying random number generator to the next
     * substream
     */
    @Override
    protected void afterReplication() {
        super.afterReplication();
        if (getResetNextSubStreamOption()) {
            advanceToNextSubstream();
        }

    }
}
