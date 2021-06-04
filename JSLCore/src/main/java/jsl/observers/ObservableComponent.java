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
package jsl.observers;

import java.util.ArrayList;
import java.util.List;

/**  The Java observer/observable pattern has a number of flaws.  This class
 *  provides a base implementation of the observer/observable pattern that
 *  mitigates those flaws.  This allows observers to be added and called
 *  in the order added to the component.  The basic usage of this class is to
 *  have a class have an instance of ObservableComponent while implementing
 *  the ObservableIfc.  The notifyObservers() method can be used to notify
 *  attached observers whenever necessary.
 *
 * @author rossetti
 */
public class ObservableComponent implements ObservableIfc {

    /** The list of observers
     *
     */
    private final List<ObserverIfc> myObservers = new ArrayList<ObserverIfc>();

    @Override
    public void addObserver(ObserverIfc observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Attempted to attach a null observer");
        }
        if (myObservers.contains(observer)) {
            throw new IllegalArgumentException("The supplied observer is already attached");
        }

        myObservers.add(observer);
    }

    @Override
    public void deleteObserver(ObserverIfc observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Attempted to delete a null observer");
        }
        myObservers.remove(observer);
    }

    @Override
    public void deleteObservers() {
        myObservers.clear();
    }

    @Override
    public boolean contains(ObserverIfc observer) {
        return myObservers.contains(observer);
    }

    @Override
    public int countObservers() {
        return myObservers.size();
    }

    /** Notify the observers
     * 
     * @param theObserved
     * @param arg 
     */
    public void notifyObservers(Object theObserved, Object arg){
        for(ObserverIfc o: myObservers){
            o.update(theObserved, arg);
        }
    }
}
