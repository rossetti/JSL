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

/**
 * 
 */
package jsl.utilities;

import java.util.NoSuchElementException;

/**
 * @author rossetti
 *
 */
public class DataObservableArray extends IterativeDataObservable {

    protected double[] myValues;

    protected int myCurrentIndex = -1;

    /**
     * @param values
     */
    public DataObservableArray(double[] values) {
        this(values, values.length, null);
    }

    /**
     * @param values
     * @param name
     */
    public DataObservableArray(double[] values, String name) {
        this(values, values.length, name);
    }

    /**
     * @param values
     * @param n
     * @param name
     */
    public DataObservableArray(double[] values, int n, String name) {
        super(name);
        setValues(values, n);
    }

    /* (non-Javadoc)
     * @see forecasting.IterativeDataSource#hasNext()
     */
    public boolean hasNext() {
        boolean f = (myCurrentIndex + 1 < myValues.length);
        if (f == true){
            if (myEndSourceFlag == false)
                notifyObservers(END_SOURCE);
            myEndSourceFlag = true;
        }
        return f;
    }

    /* (non-Javadoc)
     * @see forecasting.IterativeDataSource#next()
     */
    public double next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Attempted to go past the end of the data source");
        }
        myCurrentIndex++;
        setValue(myValues[myCurrentIndex]);
        return getValue();
    }

    /* (non-Javadoc)
     * @see forecasting.IterativeDataSource#reset()
     */
    public void reset() {
        myCurrentIndex = -1;
        notifyObservers(RESET);
        myEndSourceFlag = false;
    }

    /** The size of the array
     *
     * @return
     */
    public int size() {
        return (myValues.length);
    }

    /** Returns the value at index i
     *  zero indexed
     *
     * @param i
     * @return
     */
    public double getValue(int i) {
        return (myValues[i]);
    }

    //  ===========================================
    //      PROTECTED METHODS
    //  ===========================================
    protected void setValues(double[] values, int n) {
        if (values == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (n < 1) {
            throw new IllegalArgumentException("The number of values to copy/set must be >= 1");
        }

        myValues = new double[n];

        System.arraycopy(values, 0, myValues, 0, n);

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataSource name = " + getName() + "\n");
        sb.append("index \t value \n");
        for (int i = 0; i < myValues.length; i++) {
            sb.append(i + "\t" + myValues[i] + "\n");
        }
        sb.append("\n");
        return (sb.toString());
    }
}
