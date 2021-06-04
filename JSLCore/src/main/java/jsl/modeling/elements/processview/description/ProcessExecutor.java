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
package jsl.modeling.elements.processview.description;

import jsl.simulation.IllegalStateException;
import java.util.*;

import jsl.modeling.elements.entity.Entity;

public class ProcessExecutor {

    /**
     * A reference to the process description that is being executed
     */
    private ProcessDescription myProcessDescription;

    /**
     * A reference to the list of commands for the process description that is being
     * executed
     */
    private List<ProcessCommand> myCommandList;

    /**
     * The index (zero based) to the current command to be executed in
     * the process description
     */
    private int myCurrentCommandIndex;

    /**
     *  A reference to the entity that is currently involved
     *  in the execution of the process description for this executor
     */
    private Entity myCurrentEntity;

    /**
     * A reference to the current state of process executor
     */
    private ProcessExecutorState myState;

    /** Indicates that the process executor is jumping to
     *  another command
     */
    private boolean myJumpToFlag;

    /**
     * A reference to the created state for the iterative process A process
     * executor is in the created state when it is initially constructed and can
     * then only transition to the initialized state
     */
    private Created myCreatedState = new Created();

    /**
     * A reference to the initialized state of the process executor A process
     * executor is in the initialized state after the initialize() method is
     * called from a proper state.
     */
    private Initialized myInitializedState = new Initialized();

    /**
     * A reference to suspended state of the process executor A process executor
     * is in the suspended state after the suspend method is called from a
     * proper state
     */
    private Suspended mySuspendedState = new Suspended();

    /**
     * A reference to Executing state of the process executor A process executor
     * is in the Executing state after the resume method is called from a proper
     * state
     */
    private Executing myExecutingState = new Executing();

    /**
     * A reference to the terminated state of the process executor A process
     * executor is in the terminated state after the terminate method is called
     */
    private Terminated myTerminatedState = new Terminated();

    /** Holds the listeners that are notified prior to execution
     *
     */
    private List<ProcessExecutorListenerIfc> myBeforeExecutionListeners;

    /** Holds the listeners that are notified after the execution terminates
     *
     */
    private List<ProcessExecutorListenerIfc> myAfterExecutionListeners;

    /** Creates a process executor given the supplied
     *  process description and entity
     *
     * @param processDescription the description
     * @param entity the entity
     */
    protected ProcessExecutor(ProcessDescription processDescription, Entity entity) {
        if (processDescription == null) {
            throw new IllegalArgumentException(
                    "ProcessDescription must be non-null!");
        }

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Entity must be non-null!");
        }

        myBeforeExecutionListeners = new LinkedList<ProcessExecutorListenerIfc>();
        myAfterExecutionListeners = new LinkedList<ProcessExecutorListenerIfc>();

        myProcessDescription = processDescription;
        myCommandList = myProcessDescription.getProcessCommands();
        myCurrentCommandIndex = -1;
        myState = myCreatedState;
        myCurrentEntity = entity;
        myCurrentEntity.setProcessExecutor(this);
    }

    /** Returns the total number of commands
     *  in the associated process description
     * @return The number of commands in the associated process description
     */
    public final int getNumberOfCommands() {
        return (myCommandList.size());
    }

    /** Returns an integer representing the location of the current command
     *  within process description, zero based
     *
     * @return A integer representing the location of the command
     */
    public final int getCurrentCommandIndex() {
        return (myCurrentCommandIndex);
    }

    /** Gets the entity currently executing
     *
     * @return the entity
     */
    public final Entity getCurrentEntity() {
        return (myCurrentEntity);
    }

    /** Adds a listener to be called prior to the execution
     *
     * @param listener the listener to add
     */
    public final void addBeforeExecutionListener(ProcessExecutorListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to add a null listener");
        }
        if (myBeforeExecutionListeners.contains(listener)) {
            throw new IllegalArgumentException("Supplied listener was already added.");
        }
        myBeforeExecutionListeners.add(listener);
    }

    /** Removes the listener that is called prior to the execution
     *
     * @param listener the listener to remove
     */
    public final void removeBeforeExecutionListener(ProcessExecutorListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to remove a null listener");
        }
        myBeforeExecutionListeners.remove(listener);
    }

    /** Adds a listener to be called after the execution
     *
     * @param listener the listener to add
     */
    public final void addAfterExecutionListener(ProcessExecutorListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to add a null listener");
        }
        if (myAfterExecutionListeners.contains(listener)) {
            throw new IllegalArgumentException("Supplied listener was already added.");
        }
        myAfterExecutionListeners.add(listener);
    }

    /** Removes the listener that is called after the execution
     *
     * @param listener the listener to remove
     */
    public final void removeAfterExecutionListener(ProcessExecutorListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to remove a null listener");
        }
        myAfterExecutionListeners.remove(listener);
    }

    /** After being created the process executor must be intialized
     * before being started
     */
    public final void initialize() {
        myCurrentCommandIndex = -1;
        myState.initialize();
    }

    /** Starts the process executor executing at the first command
     * in the associated process description
     */
    public final void start() {
        start(0);
    }

    /** Starts the process executor executing at the command indicated
     *  by the commandIndex.  Note: It is up to the user to ensure that
     *  the command is an appropriate location to start executing
     *
     * @param commandIndex represents the index in the sequence of commands,
     *        index = 0, represents the first command
     * @throws IndexOutOfBoundsException - if the index is out of range {@literal (index < 0 || index >= Number of commands)}.
     */
    public void start(int commandIndex) {
        if (commandIndex < 0 || commandIndex >= myCommandList.size()) {
            throw new IndexOutOfBoundsException("Index was out of range, zero based referencing");
        }
        notifyBeforeExecutionListeners();
        myState.start(commandIndex);
    }

    /** Resumes the execution of the process executor at the next command
     *  after the previously executed command
     */
    public final void resume() {
        resume(getNextCommandIndex());
    }

    /** Resumes the execution of the process executor at the command indicated
     *  by the commandIndex.  Note: It is up to the user to ensure that
     *  the indicated command is an appropriate location to resume executing
     *  A command index out of range will cause the process executor to terminate
     * 
     * @param commandIndex the index to resume
     */
    public void resume(int commandIndex) {
        myState.resume(commandIndex);
    }

    /** Cause the execution of the process executor to jump to the command indicated
     *  by the commandIndex.  Note: It is up to the user to ensure that
     *  the indicated command is an appropriate location to continue executing.
     *  This method is only valid in executing state.
     *  A command index out of range will cause the process executor to terminate
     *
     * @param commandIndex the index to jump to
     */
    public void jumpTo(int commandIndex) {
        myState.jumpTo(commandIndex);
    }

    /**  Suspends the execution of the process executor at the current command
     *
     */
    public void suspend() {
        myState.suspend();
    }

    /** Terminates the execution of the process executor
     *
     */
    public void terminate() {
        myState.terminate();
        myProcessDescription.processExecutorTerminated(this);
        notifyAfterExecutionListeners();
    }

    /** Creates a sub-process executor on this executor that will
     *  execute the given process description
     *
     * @param processDescription the description
     * @return A process executor to execute the sub-process
     */
    public ProcessExecutor createSubProcessExecutor(ProcessDescription processDescription) {
        if (processDescription == null) {
            throw new IllegalArgumentException(
                    "ProcessDescription must be non-null!");
        }

        if (processDescription == myProcessDescription) {
            throw new IllegalArgumentException(
                    "ProcessDescription must be different from current process description.");
        }

        return (new SubProcessExecutor(this, processDescription, myCurrentEntity));
    }

    /**
     * Checks if the process executor is in the created state. After the process
     * executor has been created this method will return true
     *
     * @return true if created
     */
    public final boolean isCreated() {
        return (myState == myCreatedState);
    }

    /**
     * Checks if the process executor is in the initialized state. After the
     * process executor has been initialized this method will return true
     *
     * @return true if initialized
     */
    public final boolean isInitialized() {
        return (myState == myInitializedState);
    }

    /**
     * Checks if the process executor is in the terminated state After the
     * process executor has been terminated this method will return true
     *
     * @return true if terminated
     */
    public final boolean isTerminated() {
        return (myState == myTerminatedState);
    }

    /**
     * Checks if the process executor is in the suspended state After the
     * process executor has been suspended this method will return true
     *
     * @return true if suspended
     */
    public final boolean isSuspended() {
        return (myState == mySuspendedState);
    }

    /**
     * Checks if the process executor is in the Executing state After the
     * process executor has been resumed this method will return true
     *
     * @return true if executing
     */
    public final boolean isExecuting() {
        return (myState == myExecutingState);
    }

    /** Gets the command associated with the current command index
     *  This method returns null if the index is not valid (out of range).
     *
     * @return the command at the current command index
     */
    protected final ProcessCommand getCurrentCommand() {
        int i = getCurrentCommandIndex();
        if (i < 0 || i >= myCommandList.size()) {
            return (null);
        } else {
            return ((ProcessCommand) myCommandList.get(i));
        }
    }

    /** Sets the index to the current command.  The indexing is zero based,
     *  i.e. 0=first command
     *  An index &lt; 0 or &gt; number of commands, implies no valid command, i.e.
     *  the current command will be null
     * @param index the index to set
     */
    protected final void setCurrentCommandIndex(int index) {
        if (index == myCurrentCommandIndex) {
            throw new IllegalArgumentException("Tried to set current command index to last command index.\n"
                    + " causing infinite loop! Current index is = " + index);
        }

        myCurrentCommandIndex = index;
    }

    /** The main method that executes commands within the ProcessExecutor
     */
    protected final void execute() {

        while (!isTerminated() && !isSuspended()) {
            ProcessCommand c = getCurrentCommand();
            if (c != null) {
                c.execute(this);
                if (isExecuting()) {// still in the executing state
                    // either we are going to execute the next command or we are going to another command
                    if (myJumpToFlag == true) {
                        // next command determined by jumpTo
                        // reset flag for next command
                        myJumpToFlag = false;
                    } else { // not jumping, need to execute the next command
                        setCurrentCommandIndex(getNextCommandIndex());
                    }
                }
            } else {
                terminate();
            }
        }
    }

    /** Used to get the command index that is next to be executed
     *
     * @return the next index
     */
    protected final int getNextCommandIndex() {
        return (getCurrentCommandIndex() + 1);
    }

    /** Used to set the state of the executor
     *
     * @param state the state
     */
    protected final void setState(ProcessExecutorState state) {
        if (state == null) {
            throw new IllegalArgumentException("ProcessExecutorState must be non-null!");
        }
        myState = state;
    }

    /** Notifies the before execution listeners
     */
    protected final void notifyBeforeExecutionListeners() {
        for (ProcessExecutorListenerIfc a : myBeforeExecutionListeners) {
            a.update(this);
        }
    }

    /** Notifies the before execution listeners
     */
    protected final void notifyAfterExecutionListeners() {
        for (ProcessExecutorListenerIfc a : myAfterExecutionListeners) {
            a.update(this);
        }
    }

    private class ProcessExecutorState {

        private String myName;

        public ProcessExecutorState(String name) {
            myName = name;
        }

        public void initialize() {
            throw new IllegalStateException(
                    "Tried to initialize from an illegal state: " + myState);
        }

        public void start(int commandIndex) {
            throw new IllegalStateException(
                    "Tried to start from an illegal state: " + myState);
        }

        public void suspend() {
            throw new IllegalStateException(
                    "Tried to suspend from an illegal state: " + myState);
        }

        public void jumpTo(int commandIndex) {
            throw new IllegalStateException(
                    "Tried to jumpTo a ProcessCommand from an illegal state: " + myState);
        }

        public void resume(int commandIndex) {
            throw new IllegalStateException(
                    "Tried to resume at a ProcessCommand from an illegal state: " + myState);
        }

        public void terminate() {
            throw new IllegalStateException(
                    "Tried to terminate from an illegal state: " + myState);
        }

        public String toString() {
            return (myName);
        }
    }

    private final class Created extends ProcessExecutorState {

        public Created() {
            super("CreatedState");
        }

        public void initialize() {
            // Go from created state to initialized, set state to initialized
            setState(myInitializedState);

        }
    }

    private final class Initialized extends ProcessExecutorState {

        public Initialized() {
            super("InitializedState");
        }

        public void start(int commandIndex) {
            // we are in the initialized state
            // we want to start executing at the supplied command index

            setCurrentCommandIndex(commandIndex);

            // set the state to executing
            setState(myExecutingState);

            // tell the executor to begin executing
            execute();

        }

        public void terminate() {
            setState(myTerminatedState);
        }
    }

    private final class Executing extends ProcessExecutorState {

        public Executing() {
            super("ExecutingState");
        }

        public void jumpTo(int commandIndex) {
            // we are in the executing state and want to
            // continue executing the indicated index

            setCurrentCommandIndex(commandIndex);

            // set the flag to indicate that we are jumping
            myJumpToFlag = true;

            // no need to set the state to executing, since we're
            // all ready in the executing state

        }

        public void suspend() {
            setState(mySuspendedState);
        }

        public void terminate() {
            setState(myTerminatedState);
        }
    }

    private final class Suspended extends ProcessExecutorState {

        public Suspended() {
            super("SuspendedState");
        }

        public void resume(int commandIndex) {
            // we are in the suspended state
            // we want to continue executing at the indicated index

            setCurrentCommandIndex(commandIndex);

            // set the state to executing
            setState(myExecutingState);

            // tell the executor to continue executing
            execute();

        }

        public void terminate() {
            setState(myTerminatedState);
        }
    }

    private final class Terminated extends ProcessExecutorState {

        public Terminated() {
            super("TerminatedState");
        }

        public void initialize() {
            setState(myInitializedState);
        }
    }
}
