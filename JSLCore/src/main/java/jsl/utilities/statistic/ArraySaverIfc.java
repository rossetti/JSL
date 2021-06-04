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

import java.util.Objects;

/**
 * An interface to define the behavior of saving data to an array
 *
 * @author rossetti
 */
public interface ArraySaverIfc {

    /**
     * The default increment for the array size
     *
     */
    int DEFAULT_DATA_ARRAY_SIZE = 1000;

    /**
     * Indicates whether or not the save data option is on
     * true = on, false = off
     *
     * @return true if save option is on
     */
    boolean getSaveOption();

    /**
     * Used to save data to an array.  This method should not save
     * anything unless the save option is on.
     *
     * @param x the data to save to the array
     */
    void save(double x);

    /** Saves all values in the array
     *
     * @param values the values to save
     */
    default void save(double[] values){
        Objects.requireNonNull(values, "The array was null");
        for(double value: values){
            save(value);
        }
    }

    /**
     * Returns a copy of the data saved while the
     * saved data option was turned on, will return
     * null if no data were collected
     *
     * @return the array of data
     */
    double[] getSavedData();

    /**
     * Controls the amount that the saved data array will grow by
     * after it has been filled up.  If the potential number of
     * data points is known, then this method can be used so that
     * arrays do not have to be copied during collection
     * The array size will start at this value and then increment by
     * this value whenever full
     *
     * @param n the growth increment
     */
    void setArraySizeIncrement(int n);

    /**
     * Sets the save data option
     * true = on, false = off
     *
     * If true, the data will be saved to an array
     * If this option is toggled, then only the data
     * when the option is true will be saved.
     *
     * @param flag true means save the data
     */
    void setSaveOption(boolean flag);

    /**
     *  Should clear the saved data from the array
     */
    void clearSavedData();
}
