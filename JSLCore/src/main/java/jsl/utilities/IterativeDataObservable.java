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

import jsl.simulation.ModelElement;

/** An abstract base class for modeling data sources that users can iterate through
 *
 * 
 *
 */
public abstract class IterativeDataObservable extends DataObservable {

    /** An "enum" to indicate that this iterative data source has just been reset to observers
     *  Subclasses are responsible for using notifyObservers(RESET) to let observers
     *  know that a reset has been done.
     */
    public static final int RESET = ModelElement.getNextEnumConstant();

    /** An "enum" to indicate that this iterative data source has just reached it's end
     *  Subclasses are responsible for using notifyObservers(END_SOURCE) to let observers
     *  know that the end of the source has been reached
     */
    public static final int END_SOURCE = ModelElement.getNextEnumConstant();

    /** Indicates that the end of the source has been reached. Should be used
     *  by subclasses to indicate end of source
     *
     */
    protected boolean myEndSourceFlag = false;

    /**
     *
     */
    public IterativeDataObservable() {
        this(null);
    }

    /**
     * @param name
     */
    public IterativeDataObservable(String name) {
        super(name);

    }

    /** Indicates that the end of the source has been reached.
     * 
     * @return
     */
    public boolean getEndSourceFlag(){
        return myEndSourceFlag;
    }

    /** Indicates to clients whether there is another value after
     *  the current value for the data source
     *
     * @return
     */
    abstract public boolean hasNext();

    /** Advances to the next value and returns it.  Implementers must
     *  use setValue(double value) to properly set the current value.
     *  getValue() should then return this current value until advanced again
     *
     *  Should throw a NoSuchElementException if there is no next value
     *
     * @return
     */
    abstract public double next();

    /** This method should reset the IterativeDataSource so that it
     *  is positioned just prior to the 1st value to be returned
     *  via the use of the next() method
     *
     *  Implementers should ensure that observers are notified of the reset
     *  and that the end of source indicator correctly reflects the reset
     *
     */
    abstract public void reset();

}
