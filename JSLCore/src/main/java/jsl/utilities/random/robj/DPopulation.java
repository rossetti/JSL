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
package jsl.utilities.random.robj;

import jsl.utilities.NewInstanceIfc;
import jsl.utilities.controls.ControllableIfc;
import jsl.utilities.controls.Controls;
import jsl.utilities.random.ParametersIfc;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.SampleIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.Objects;

/** A DPopulation is a population of doubles that can be sampled from and permuted.
 * @author rossetti
 *
 */
public class DPopulation implements RandomIfc, SampleIfc, ControllableIfc, ParametersIfc, NewInstanceIfc<DPopulation> {

    /** A counter to count the number of created to assign "unique" ids
     */
    private static int myIdCounter_;

    /** The id of this object
     */
    protected int myId;

    /** Holds the name of the statistic for reporting purposes.
     */
    protected String myName;

    private RNStreamIfc myStream;

    private double[] myElements;

    /**
     *
     * @param elements the elements to sample from
     */
    public DPopulation(double[] elements) {
        this(elements, JSLRandom.nextRNStream(), null);
    }

    /**
     * @param elements the elements to sample from
     * @param stream the stream to use for sampling
     */
    public DPopulation(double[] elements, RNStreamIfc stream) {
        this(elements, stream, null);
    }

    /**
     * @param elements the elements to sample from
     * @param stream the stream to use for sampling
     * @param name the name of the population, optional
     */
    public DPopulation(double[] elements, RNStreamIfc stream, String name) {
        setId();
        setName(name);
        setRandomNumberStream(stream);
        setParameters(elements);
    }

    /** Gets the name.
     * @return The name of object.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /** Sets the name
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s;
        } else {
            myName = str;
        }
    }

    /**
     *
     * @return Returns the id
     */
    @Override
    public final int getId() {
        return (myId);
    }

    /** Returns a new instance of the random source with the same parameters
     *  but an independent generator
     *
     * @return Returns a new instance of the population with the same parameters
     *   but an different random stream
     */
    @Override
    public final DPopulation newInstance() {
        return (new DPopulation(getParameters()));
    }

    /** Returns a new instance of the random source with the same parameters
     *  but an independent generator
     *
     * @param stream a random number stream, must not null.
     * @return Returns a new instance of the population with the same parameters
     *   but an different random stream
     */
    public final DPopulation newInstance(RNStreamIfc stream) {
        return (new DPopulation(getParameters(), stream));
    }

    /** Creates a new array that contains a randomly sampled values without replacement
     *  from the existing population.
     *
     * @param sampleSize the number to sample
     * @return the sampled array
     */
    public final double[] getSampleWithoutReplacement(int sampleSize) {
        JSLRandom.sampleWithoutReplacement(myElements, sampleSize, myStream);
        double[] x = new double[sampleSize];
        System.arraycopy(myElements, 0, x, 0, x.length);
        return (x);
    }

    /** Creates a new array that contains a random permutation of the population
     *
     * @return a new array that contains a random permutation of the population
     */
    public final double[] getPermutation() {
        return (getSampleWithoutReplacement(myElements.length));
    }

    /** Causes the population to form a new permutation,
     *  The ordering of the elements in the population will be changed.
     */
    public final void permute() {
        JSLRandom.permutation(myElements, myStream);
    }

    /** Returns the value at the supplied index
     *
     * @param index must be &gt; 0 and less than size() - 1
     * @return the value at the supplied index
     */
    public final double get(int index) {
        return (myElements[index]);
    }

    /** Sets the element at the supplied index to the supplied value
     *
     * @param index an index into the array
     * @param value the value to set
     */
    public final void set(int index, double value) {
        myElements[index] = value;
    }

    /** Returns the number of elements in the population
     *
     * @return the size of the population
     */
    public final int size() {
        return (myElements.length);
    }

    /**
     * @return Gets a copy of the population array, in its current state
     */
    @Override
    public double[] getParameters() {
        double[] x = new double[myElements.length];
        System.arraycopy(myElements, 0, x, 0, myElements.length);
        return x;
    }

    /**
     *
     * @param elements Copies the values from the supplied array to the population array
     */
    @Override
    public final void setParameters(double[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("The element array was null");
        }
        if (elements.length == 0){
            throw new IllegalArgumentException("The element array had no elements.");
        }
        myElements = new double[elements.length];
        System.arraycopy(elements, 0, myElements, 0, elements.length);
    }

    protected class DPopControls extends Controls {

        protected void fillControls() {
            addDoubleArrayControl("parameters", getParameters());
        }
    }

    @Override
    public Controls getControls() {
        return new DPopControls();
    }

    @Override
    public void setControls(Controls controls) {
        if (controls == null) {
            throw new IllegalArgumentException("The supplied controls were null!");
        }
        setParameters(controls.getDoubleArrayControl("parameters"));
    }

    /** Returns a randomly selected element from the population.  All
     *  elements are equally likely.
     * @return the randomly selected element
     */
    @Override
    public final double getValue() {
        return sample();
    }

    @Override
    public final double sample(){
        return myElements[getRandomIndex()];
    }

    /** Returns a random index into the population (assuming elements numbered starting at zero)
     *
     * @return a random index
     */
    protected final int getRandomIndex() {
        return (myStream.randInt(0, myElements.length - 1));
    }

    @Override
    public final void advanceToNextSubstream() {
        myStream.advanceToNextSubstream();
    }

    @Override
    public final void resetStartStream() {
        myStream.resetStartStream();
    }

    @Override
    public final void resetStartSubstream() {
        myStream.resetStartSubstream();
    }

    @Override
    public final boolean getAntitheticOption() {
        return myStream.getAntitheticOption();
    }

    @Override
    public final void setAntitheticOption(boolean flag) {
        myStream.setAntitheticOption(flag);
    }


    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return myStream;
    }

    @Override
    public final void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myStream = stream;
    }

    @Override
    public String toString() {
        return (toString(myElements));
    }

    public static String toString(double[] x) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            sb.append("Element(");
            sb.append(i);
            sb.append(") = ");
            sb.append(x[i]);
            sb.append(System.lineSeparator());
        }
        return (sb.toString());
    }

    protected final void setId() {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
    }
}
