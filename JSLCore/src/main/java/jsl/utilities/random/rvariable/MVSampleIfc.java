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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  An interface for getting multi-variable samples, each sample has many values
 *  held in an array
 */
public interface MVSampleIfc {

    /**
     *
     * @return the expected size of the array from sample()
     */
    int getDimension();

    /**
     *
     * @return generates an array of random values of size getDimension()
     */
    default double[] sample(){
        double[] array = new double[getDimension()];
        sample(array);
        return(array);
    }

    /** Fills the supplied array with a sample of values. This method
     *  avoids the creation of a new array.  The size of the array
     *  must match getDimension()
     *
     * @param array the array to fill with the sample
     */
    void sample(double[] array);

    /**
     * Generates a list holding the randomly generated arrays of size getDimension()
     *
     * @param sampleSize the amount to fill
     * @return A list holding the generated arrays
     */
    default List<double[]> sample(int sampleSize) {
        List<double[]> list = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            list.add(sample());
        }
        return (list);
    }

    /**
     * Fills the supplied list of arrays with randomly generated samples
     *
     * @param values the list to fill
     */
    default void sample(List<double[]> values) {
        if (values == null) {
            throw new IllegalArgumentException("The supplied list was null");
        }
        for (int i = 0; i < values.size(); i++) {
            values.add(sample());
        }
    }
}
