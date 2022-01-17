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

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.*;

/** Randomly selects the elements in the list according to a supplied CDF across the items
 *
 * @param <T> the type of elements in the list
 */
public class DEmpiricalList<T> implements RElementIfc<T> {

    protected final List<T> myElements;

    protected final double[] myCDF;

    protected RNStreamIfc myRNG;

    /**
     *
     * @param elements the list of elements, must not be null
     * @param cdf an array holding the cumulative probabilities across the elements in the list
     */
    public DEmpiricalList(List<T> elements, double[] cdf) {
        this(elements, cdf, JSLRandom.nextRNStream());
    }

    /**
     *
     * @param elements the list of elements, must not be null
     * @param cdf an array holding the cumulative probabilities across the elements in the list
     * @param rng the underlying random number stream to use for randomness
     */
    public DEmpiricalList(List<T> elements, double[] cdf, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The RNStreamIfc was null");
        if (elements == null) {
            throw new IllegalArgumentException("The list of elements was null");
        }
        if (cdf == null) {
            throw new IllegalArgumentException("The list of probabilities was null");
        }
        if (elements.size() < cdf.length) {
            throw new IllegalArgumentException("The number of objects was less than the number of probabilities.");
        }
        myElements = new ArrayList<T>(elements);
        myCDF = Arrays.copyOf(cdf, cdf.length);
        myRNG = rng;

    }

    /**
     * @return a copy of the underlying CDF array
     */
    public double[] getCDF() {
        return Arrays.copyOf(myCDF, myCDF.length);
    }

    @Override
    public T getRandomElement() {
        return JSLRandom.randomlySelect(myElements, myCDF, myRNG);
    }

    @Override
    public void advanceToNextSubstream() {
        myRNG.advanceToNextSubstream();
    }

    @Override
    public void resetStartStream() {
        myRNG.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        myRNG.resetStartSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myRNG.setAntitheticOption(flag);
    }

    @Override
    public final boolean getAntitheticOption() {
        return myRNG.getAntitheticOption();
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#contains(java.lang.Object)
     */
    public final boolean contains(Object arg0) {
        return myElements.contains(arg0);
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#containsAll(java.util.Collection)
     */
    public final boolean containsAll(Collection<?> arg0) {
        return myElements.containsAll(arg0);
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#indexOf(java.lang.Object)
     */
    public final int indexOf(Object arg0) {
        return myElements.indexOf(arg0);
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#isEmpty()
     */
    public final boolean isEmpty() {
        return myElements.isEmpty();
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#size()
     */
    public final int size() {
        return myElements.size();
    }

    /* (non-Javadoc)
     * @see jsl.utilities.random.RandomListIfc#getList()
     */
    public final List<T> getList() {
        return (Collections.unmodifiableList(myElements));
    }

    public static void main(String[] args) {

        List<String> cities = Arrays.asList("KC", "CH", "NY");

        DEmpiricalList<String> originSet = new DEmpiricalList<>(cities, new double[]{0.4, 0.8, 1.0});

        for (int i = 1; i <= 10; i++) {
            System.out.println(originSet.getRandomElement());
        }

        Map<String, DEmpiricalList<String>> od = new HashMap<>();

        DEmpiricalList<String> kcdset = new DEmpiricalList<>(
                Arrays.asList("CO", "AT", "NY"),
                new double[]{0.2, 0.6, 1.0}
        );

        var chdset = new DEmpiricalList<>(
                Arrays.asList("AT", "NY", "KC"),
                new double[]{0.2, 0.6, 1.0}
        );

        DEmpiricalList<String> nydset = new DEmpiricalList<>(
                Arrays.asList("AT", "KC", "CH"),
                new double[]{0.2, 0.6, 1.0}
        );

        od.put("KC", kcdset);
        od.put("CH", chdset);
        od.put("NY", nydset);

        for (int i = 1; i <= 10; i++) {
            String key = originSet.getRandomElement();
            DEmpiricalList<String> rs = od.get(key);
            System.out.println(rs.getRandomElement());
        }
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myRNG;
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myRNG = stream;
    }
}
