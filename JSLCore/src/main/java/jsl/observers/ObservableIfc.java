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

/** The Java Observer/Observable implementation has a number of flaws.
 *  This class represents an interface for objects that can be observed.
 *  Essentially, observable objects promise the basic management of classes
 *  that implement the ObserverIfc
 *
 * @author rossetti
 */
public interface ObservableIfc {

    /** Allows the adding (attaching) of an observer to the observable
     *
     * @param observer the observer to attach
     */
    void addObserver(ObserverIfc observer);

    /** Allows the deletion (removing) of an observer from the observable
     *
     * @param observer the observer to delete
     */
    void deleteObserver(ObserverIfc observer);

    /** Returns true if the observer is already attached
     *
     * @param observer the observer to check
     * @return true if attached
     */
    boolean contains(ObserverIfc observer);

    /** Deletes all the observers from the observable
     *
     */
    void deleteObservers();

    /** Returns how many observers are currently observing the observable
     *
     * @return number of observers 
     */
    int countObservers();
}
