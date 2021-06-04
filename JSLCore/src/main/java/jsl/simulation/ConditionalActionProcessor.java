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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.simulation;

import jsl.utilities.misc.OrderedList;

/**  Processes the ConditionalActions to check if their testCondition()
 *  is true, if so the action is executed.  All actions are checked until
 *  no action's testCondition() is true
 *  To prevent conditional cycling the number of rescans is limited to
 *  DEFAULT_MAX_SCANS, which can be changed by the user or turned off via
 *  setMaxScanFlag()
 *
 * @author rossetti
 */
public class ConditionalActionProcessor {

    private static int myActionCounter;
    
    public static final int DEFAULT_MAX_SCANS = 1000;

    public static final int DEFAULT_PRIORITY = 1;

    protected int myMaxScans = DEFAULT_MAX_SCANS;

    private boolean myMaxScanFlag = true;

    protected OrderedList<ConditionalAction> myActions;

    public ConditionalActionProcessor() {
        myActions = new OrderedList<ConditionalAction>();
    }

    /** Registers the action with the default priority
     *
     * @param action the action
     */
    public void register(ConditionalAction action){
        register(action, DEFAULT_PRIORITY);
    }
    
    /** Registers the action with the given priority
     *
     * @param action the action
     * @param priority the priority
     */
    public void register(ConditionalAction action, int priority){
        if (action == null){
            throw new IllegalArgumentException("The supplied action was null");
        }
        action.setId(++myActionCounter);
        action.setPriority(priority);
        myActions.add(action);
    }

    /** Changes the priority of a previously registered action
     *
     * @param action the action
     * @param priority the priority
     */
    public void changePriority(ConditionalAction action, int priority){
        unregister(action);
        action.setPriority(priority);
        myActions.add(action);
    }

    /** Unregisters the action from the simulation
     *
     * @param action the action
     */
    public void unregister(ConditionalAction action){
        if (action == null){
            throw new IllegalArgumentException("The supplied action was null");
        }

        if (!myActions.contains(action)){
            throw new IllegalArgumentException("The supplied action is not registered");
        }
        myActions.remove(action);
    }

    /** Unregisters all actions that were previously registered.
     *
     */
    public final void unregisterAllActions(){
        myActions.clear();
    }

    /** Returns true at least one ConditionalAction was executed
     *  false means all actions tested false
     * 
     * @return true if at least one
     */
    protected boolean executeConditionalActions() {
        boolean test = false;
        for (ConditionalAction c : myActions) {
            if (c.testCondition()) {
                c.action();
                test = true;
            }
        }
        return test;
    }

    /** Returns the maximum number of scans during the c phase
     *
     * @return the max
     */
    public final int getMaxScans() {
        return myMaxScans;
    }

    /** Sets the maximum number of scans
     *
     * @param max, must be &gt; 0
     */
    public final void setMaxScans(int max){
        if (max <= 0){
            throw new IllegalArgumentException("The max scans must be > 0");
        }
        myMaxScans = max;
    }

    protected void performCPhase() {
        if (myActions.isEmpty()){
            return;
        }
        boolean test = true;
        int i = 0;
        while (test) {
            test = executeConditionalActions();
            i++;
            if (getMaxScanFlag()) {
                if (i >= getMaxScans()) {
                    throw new TooManyScansException();
                }
            }
        }
    }

    /** Returns the maximum scan flag
     *
     * @return true means scans are monitored
     */
    public final boolean getMaxScanFlag(){
        return myMaxScanFlag;
    }

    /** Sets the maximum scan checking flag.  If true
     *  the maximum number of scans is monitored during
     *  the c phase
     *
     * @param flag true means monitor scans
     */
    public final void setMaxScanFlag(boolean flag){
        myMaxScanFlag = flag;
    }
}
