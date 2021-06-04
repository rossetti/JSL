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
package jsl.utilities.statistic;

import jsl.utilities.GetValueIfc;

import java.util.Objects;

/**
 * This interface represents a general set of methods for data collection. The
 * collect() method takes in the supplied data and collects it in some manner as
 * specified by the collector.
 *
 * @author rossetti
 */
public interface CollectorIfc {

    /**
     * Collects statistics on the values returned by the supplied GetValueIfc
     *
     * @param v the object that returns the value to be collected
     */
    default void collect(GetValueIfc v) {
        Objects.requireNonNull(v, "The supplied GetValueIfc was null");
        collect(v.getValue());
    }

    /**
     * Collects statistics on the boolean value true = 1.0, false = 0.0
     *
     * @param value the value to collect on
     */
    default void collect(boolean value) {
        collect((value) ? 1.0 : 0.0);
    }

    /**
     * Collect on the supplied value
     *
     * @param value a double representing the observation
     */
    void collect(double value);

    /**
     * Collects statistics on the values in the supplied array.
     *
     * @param values the values, must not be null
     */
    default void collect(double[] values) {
        Objects.requireNonNull(values, "The data array was null");
        for (double v : values) {
            collect(v);
        }
    }

    /**
     * Resets the collector as if no observations had been collected.
     */
    void reset();
}
