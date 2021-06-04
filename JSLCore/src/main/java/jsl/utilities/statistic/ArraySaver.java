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

import java.util.Arrays;

/**
 * A class to save data to an expanding array.
 */
public class ArraySaver implements ArraySaverIfc {

    /**
     * The array to collect the data if the saved flag is true
     * Uses lazy initialization. Doesn't allocate array until
     * save is attempted and save option is on.
     */
    private double[] myData;

    /**
     * Used to set the initial array size when the collect data option is
     * turned on
     */
    private int myDataArraySize = DEFAULT_DATA_ARRAY_SIZE;

    /**
     * Counts the number of data points that were saved to the save array
     */
    private int mySaveCount = 0;

    /**
     * A flag to indicate whether or not the saver should save the data as
     * it is collected.  If this flag is true, the data will be saved
     * when the save() method is called.
     */
    private boolean mySaveDataFlag;

    /**
     *  Creates an ArraySaver with the saving data option ON
     */
    public ArraySaver(){
        this(true);
    }

    /**
     *
     * @param saveDataFlag true means save the data
     */
    public ArraySaver(boolean saveDataFlag) {
        mySaveDataFlag = saveDataFlag;
    }

    @Override
    public final void clearSavedData() {
        if (myData == null) {
            return;
        }
        myData = null;
        mySaveCount = 0;
    }

    @Override
    public final boolean getSaveOption() {
        return mySaveDataFlag;
    }

    @Override
    public final double[] getSavedData() {
        if (myData == null) {
            return new double[0];
        }
        return Arrays.copyOf(myData, mySaveCount);
    }

    @Override
    public final void save(double x) {
        if (getSaveOption() == false) {
            return;
        }
        if (myData == null) {
            myData = new double[myDataArraySize];
        }
        // need to save x into the array
        mySaveCount++;
        if (mySaveCount > myData.length) {
            // need to grow the array
            myData = Arrays.copyOf(myData, myData.length + myDataArraySize);
        }
        myData[mySaveCount - 1] = x;
    }

    @Override
    public final void setArraySizeIncrement(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("The array size increment must be > 0");
        }
        myDataArraySize = n;
    }

    @Override
    public final void setSaveOption(boolean flag) {
        mySaveDataFlag = flag;
    }
}
