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
package jsl.simulation;

import java.nio.file.Path;
import java.util.*;

import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.observers.textfile.IPLogReport;

/**
 * A IterativeProcess is an abstract base class for modeling the execution of a
 * series of steps until some condition is met or until the number of steps have
 * been exhausted. An iterative process will stop when one of the following
 * occurs:
 *
 * a) It has no more steps to execute b) A condition becomes true after a step
 * i) A user supplied (real) time limit has been (approximately) reached ii)
 * Calling the stop() method based on some condition iii) Calling end() when in
 * state Created, Initialized, or StepCompleted
 *
 * An iterative process follows a well defined state transition pattern. An
 * iterative process has the following states: (Created, Initialized,
 * StepCompleted, Ended). An iterative process is first created and placed in
 * the Created state.
 *
 * From the Created state it can transition to Initialized (via initialize()) or
 * Ended (via end()). From the Ended state it can transition to Initialized (via
 * initialize()). From the Initialized state it can transition to StepCompleted
 * (via runNext()) or to Ended (via run() or end()). From the StepCompleted
 * state it can transition back to StepCompleted (via runNext()) or to Ended
 * (via run(), runNext(), or end()).
 *
 * Thus, a common usage of an IterativeProcess is to call initialize() after
 * construction and call runNext() to run each step of the process as needed. An
 * IterativeProcess can also run to completion by calling run(). Subclasses must
 * provide the following abstract protected methods:
 *
 * boolean hasNext(): checks if there is a next step T next(): gets an object
 * that represents the next step
 *
 * void runStep(): causes the next step to execute, if no step exists then a run
 * time exception, NoSuchStepException, will be thrown.
 *
 * If a client attempts an illegal state transition, then a run time exception,
 * IllegalStateException, will be thrown.
 *
 * Sub-classes should also consider overriding initializeIterations() and
 * endIterations() to provide specific initialization logic and logic for after
 * the process ends. When overriding these methods care should be taken to
 * properly call super.initializeIterations() and super.endIterations()
 * otherwise non-determinant behavior will occur.
 *
 * The pattern is general enough to allow IterativeProcesses to run other
 * IterativeProcesses within each step.
 *
 * @param <T> the type that represents the step
 */
abstract public class IterativeProcess<T> implements ObservableIfc, IterativeProcessIfc {

    /**
     * A counter to count the number of created to assign "unique" ids
     */
    private static long myIdCounter_;

    /**
     * Indicates whether the iterative process haD no steps to run
     */
    public static final int NO_STEPS_EXECUTED = Model.getNextEnumConstant();

    /**
     * Indicates whether the iterative process has completed running all steps
     */
    public static final int COMPLETED_ALL_STEPS = Model.getNextEnumConstant();

    /**
     * Indicates whether the iterative process has exceeded its maximum
     * execution time
     */
    public static final int EXCEEDED_EXECUTION_TIME = Model.getNextEnumConstant();

    /**
     * Indicates whether the iterative process ended due to the being stopped
     */
    public static final int MET_STOPPING_CONDITION = Model.getNextEnumConstant();

    /**
     * Indicates that the iterative process is in progress
     */
    public static final int UNFINISHED = Model.getNextEnumConstant();

    /**
     * Message used when process completes all steps
     */
    protected String NO_STEPS_EXECUTED_MSG = "No steps to run.";

    /**
     * Message used when process completes all steps
     */
    protected String COMPLETED_ALL_STEPS_MSG = "Completed all steps.";

    /**
     * Message used when process exceeds its execution time
     */
    protected String EXCEEDED_EXECUTION_TIME_MSG = "Exceeded its maximum execution time.";

    /**
     * Message used when process stops because it met a condition
     */
    protected String MET_STOPPING_CONDITION_MSG = "Stopped based on a condition.";

    /**
     * Message used when process is not finished
     */
    protected String UNFINISHED_MSG = "The process is not finished";

    /**
     * The id of this object
     */
    protected long myId;

    /**
     * The name of this step
     */
    protected String myName;

    /**
     * The wall clock time in milliseconds that the iterative process started
     */
    protected long myBeginExecutionTime;

    /**
     * The wall clock time in milliseconds that the iterative process ended
     */
    protected long myEndExecutionTime;

    /**
     * The maximum allowable execution time "wall" clock time for the iterative
     * process to complete processing in milliseconds
     */
    protected long myMaxAllowedExecutionTime;

    /**
     * A flag to indicate whether the iterative process is done A iterative
     * process can be done if: 1) it ran all of its steps 2) it was canceled by
     * a client prior to completing all of its steps 3) it exceeded its maximum
     * allowable execution time before completing all of its steps.
     */
    protected boolean myDoneFlag;

    /**
     * Indicates that the iterative process is running
     *
     */
    protected boolean myRunningFlag = false;

    /**
     * Indicates if the iterative process is currently running an individual
     * step, true if the step is in progress
     *
     */
    protected boolean myRunningStepFlag = false;

    /**
     * indicates how the iterative process ended
     */
    protected int myEndingStateIndicator;

    /**
     * A reference to an object related to the current step of the process It
     * can be passed to observers
     */
    protected T myCurrentStep;

    /**
     * A collection of all the steps that have been run by this iterative
     * process.
     */
    protected List<T> mySteps;

    /**
     * A flag to indicate whether or not the iterative process will save the
     * steps as they are created and then run in a Collection The default is
     * FALSE (they will be not be saved).
     */
    protected boolean mySaveStepOption;

    /**
     * Used to log state changes
     */
    protected IPLogReport myIPLogReport;

    /**
     * Used to indicate whether logging is on
     */
    private boolean myIPLogReportOption;

    /**
     * Can be set to indicate why the process was stopped via the
     * stopProcessing() method
     *
     */
    private String myStoppingMessage = null;

    /**
     * A flag that can be set by the user to stop the process
     *
     */
    private boolean myStoppingFlag;

    /**
     * A reference to the current state of the iterative process
     */
    protected IterativeState myState;

    /**
     * A reference to the created state for the iterative process A iterative
     * process is in the created state when it is first constructed and can then
     * only transition to the initialized state
     */
    protected final Created myCreatedState = new Created();

    /**
     * A reference to the initialized state of the iterative process A iterative
     * process is in the initialized state after the initialize() method is
     * called from a proper state.
     */
    protected final Initialized myInitializedState = new Initialized();

    /**
     * A reference to the step completed state of the iterative process A
     * iterative process is in the step completed state after the runNext method
     * is called from a proper state
     */
    protected final StepCompleted myStepCompletedState = new StepCompleted();

    /**
     * A reference to the ended state of the iterative process A iterative
     * process is in the ended state after the process is told to end
     */
    protected final Ended myEndedState = new Ended();

    /**
     * A Timer used to perform timed task e.g. displaying information about the
     * IterativeProcess
     */
    protected Timer myTimer;

    /**
     * The time between task invocations for the Timer and its TimerTask
     */
    protected long myTBConsoleUpdates;

    /**
     * The task that the timer uses
     */
    protected TimerTask myTimerTask;

    /**
     * A flag that indicates whether or not the iterative process has already
     * been initialized, false means it has not been initialized, true means
     * that it has been initialized
     */
    protected boolean myInitFlag;

    /**
     * Counts the number of steps executed since the last time the process was
     * initialized
     *
     */
    protected long myStepCounter = 0;

    /**
     * Allows the IterativeProcess to be observable. Observers are notified when
     * the state changes Created, Initialized, StepCompleted, Ended Observers
     * have access to the IterativeProcess and the current step T
     *
     */
    protected ObservableComponent myObservableComponent;

    /**
     * Constructs an iterative process with default name
     *
     */
    public IterativeProcess() {
        this(null);
    }

    /**
     * Constructs an iterative process with the given name
     *
     * @param name the name of the process
     */
    public IterativeProcess(String name) {
        super();
        setId();
        setName(name);
        myObservableComponent = new ObservableComponent();
        myEndingStateIndicator = UNFINISHED;
        myMaxAllowedExecutionTime = 0;
        mySteps = new ArrayList<T>();
        mySaveStepOption = false;
        myInitFlag = false;
        myStoppingFlag = false;
        setState(myCreatedState);
    }

    /**
     * Gets the name.
     *
     * @return The name of object.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s + " " + getId();
        } else {
            myName = str;
        }
    }

    /**
     * Returns the id for this iterative process
     *
     * @return the id
     */
    public final long getId() {
        return (myId);
    }

    /**
     * Can be overridden to supply a unique id to the object By default a simple
     * static counter is used to assign a number as instances are created
     *
     */
    protected final void setId() {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
    }

    /**
     * Gets a string for the iterative process.
     *
     * @return Yields the name and other facts for the iterative process.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Iterative Process Name: ");
        sb.append(getName());
        sb.append("\n");
        sb.append("Beginning Execution Time: ");
        sb.append(getBeginExecutionTime());
        sb.append("\n");
        sb.append("End Execution Time: ");
        sb.append(getEndExecutionTime());
        sb.append("\n");
        sb.append("Elapsed Execution Time (seconds): ");
        sb.append(getElapsedExecutionTime() / 1000.0);
        sb.append("\n");
        sb.append("Max Allowed Execution Time: ");
        if (getMaximumAllowedExecutionTime() > 0) {
            sb.append(getMaximumAllowedExecutionTime());
            sb.append(" milliseconds\n");
        } else {
            sb.append("Not Specified");
        }
        sb.append("\n");
        sb.append("Done Flag: ");
        sb.append(myDoneFlag);
        sb.append("\n");
        sb.append("Has Next: ");
        sb.append(hasNext());
        sb.append("\n");
        sb.append("Current State: ");
        sb.append(myState);
        sb.append("\n");
        sb.append("Ending State Indicator: ");
        sb.append(getEndingStateIndicatorAsString());
        sb.append("\n");
        if (myStoppingMessage != null) {
            sb.append("Stopping Message: ");
            sb.append(myStoppingMessage);
            sb.append("\n");
        }

        return (sb.toString());
    }

    /**
     * Gets the current state as a string
     *
     * @return the current state as a string
     */
    public final String getCurrentStateAsString() {
        return myState.myName;
    }

    /**
     * Returns the ending state indicator
     *
     * @return an integer representing the ending state
     */
    public final int getEndingStateIndicator() {
        return myEndingStateIndicator;
    }

    /**
     * Returns a string representation of the ending state indicator
     * COMPLETED_ALL_STEPS_MSG EXCEEDED_EXECUTION_TIME_MSG
     * MET_STOPPING_CONDITION_MSG UNFINISHED_MSG
     *
     * @return the string
     */
    public String getEndingStateIndicatorAsString() {
        if (allStepsCompleted()) {
            return (COMPLETED_ALL_STEPS_MSG);
        } else if (executionTimeExceeded()) {
            return (EXCEEDED_EXECUTION_TIME_MSG);
        } else if (stoppedByCondition()) {
            return (MET_STOPPING_CONDITION_MSG);
        } else if (isUnfinished()) {
            return (UNFINISHED_MSG);
        } else if (noStepsExecuted()) {
            return (NO_STEPS_EXECUTED_MSG);
        } else {
            return "No indicator message defined";
        }
    }

    /**
     * A flag to indicate whether the iterative process is done A iterative
     * process can be done if: 1) it ran all of its steps 2) it was ended by a
     * client prior to completing all of its steps 3) it ended because it
     * exceeded its maximum allowable execution time before completing all of
     * its steps. 4) its end condition was satisfied
     *
     * @return true if done
     */
    @Override
    public final boolean isDone() {
        return (myDoneFlag);
    }

    @Override
    public final boolean isCreated() {
        return (myState == myCreatedState);
    }

    /**
     * Returns system time in milliseconds that the iterative process started
     *
     * @return the number as a long
     */
    @Override
    public final long getBeginExecutionTime() {
        return (myBeginExecutionTime);
    }

    @Override
    public final long getElapsedExecutionTime() {
        if (myBeginExecutionTime > 0) {
            return (System.currentTimeMillis() - myBeginExecutionTime);
        } else {
            return (0);
        }
    }

    @Override
    public final long getEndExecutionTime() {
        return (myEndExecutionTime);
    }

    @Override
    public final long getMaximumAllowedExecutionTime() {
        return (myMaxAllowedExecutionTime);
    }

    @Override
    public final void setMaximumExecutionTime(long milliseconds) {
        if (milliseconds <= 0.0) {
            throw new IllegalArgumentException("The maximum number of execution time (clock time) must be > 0.0");
        }
        myMaxAllowedExecutionTime = milliseconds;
    }

    @Override
    public final String getStoppingMessage() {
        return myStoppingMessage;
    }

    /**
     * Gets the save step option, true means that the steps will be saved by the
     * iterative process into a Collection after running
     *
     * @return flag indicating the option
     */
    public final boolean getSaveStepOption() {
        return mySaveStepOption;
    }

    /**
     * Sets the save step option, true means that the steps will be saved by the
     * iterative process into a Collection after running
     *
     * @param option true if option is on
     */
    public final void setSaveStepOption(boolean option) {
        mySaveStepOption = option;
    }

    /**
     * Returns an iterator to the saved steps
     *
     * @return an iterator
     */
    public final Iterator<T> getStepIterator() {
        return (mySteps.iterator());
    }

    /**
     * Returns an unmodifiable list view of the steps that have been saved
     *
     * @return the list
     */
    public final List<T> getStepList() {
        return (Collections.unmodifiableList(mySteps));
    }

    /**
     * Returns the current step for the iterative process the step that is or
     * has just completed processing Note: Sub-classes are responsible for
     * properly setting the current step when a step is run via runStep()
     *
     * @return the current step
     */
    public final T getCurrentStep() {
        return myCurrentStep;
    }

    @Override
    public final long getNumberStepsCompleted() {
        return myStepCounter;
    }

    @Override
    public void turnOnLogReport(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file was null!");
        myIPLogReportOption = true;
        myIPLogReport = new IPLogReport(pathToFile);
        addObserver(myIPLogReport);
    }

    @Override
    public void turnOffLogReport() {
        if (myIPLogReportOption == true) {
            deleteObserver(myIPLogReport);
            myIPLogReportOption = false;
            myIPLogReport = null;
        }
    }

    @Override
    public final IPLogReport getLogReport() {
        return (myIPLogReport);
    }

    @Override
    public final void turnOnTimer(long milliseconds) {
        if (milliseconds <= 0){
            myTBConsoleUpdates = 0;
            return;
        }
        myTBConsoleUpdates = milliseconds;
    }

    @Override
    public final boolean isInitialized() {
        return (myState == myInitializedState);
    }

    @Override
    public final boolean isRunning() {
        return (myRunningFlag);
    }

    @Override
    public final boolean isRunningStep() {
        return myRunningStepFlag;
    }

    @Override
    public final boolean isStepCompleted() {
        return (myState == myStepCompletedState);
    }

    @Override
    public final boolean isEnded() {
        return (myState == myEndedState);
    }

    @Override
    public final boolean noStepsExecuted() {
        return (myEndingStateIndicator == NO_STEPS_EXECUTED);
    }

    @Override
    public final boolean allStepsCompleted() {
        return (myEndingStateIndicator == COMPLETED_ALL_STEPS);
    }

    @Override
    public final boolean executionTimeExceeded() {
        return (myEndingStateIndicator == EXCEEDED_EXECUTION_TIME);
    }

    @Override
    public final boolean stoppedByCondition() {
        return (myEndingStateIndicator == MET_STOPPING_CONDITION);
    }

    @Override
    public final boolean isUnfinished() {
        return (myEndingStateIndicator == UNFINISHED);
    }

    @Override
    public final void initialize() {
        myState.initialize();
    }

    @Override
    public final void runNext() {
        if (!hasNext()) {
            StringBuilder s = new StringBuilder();
            s.append("Iterative Process: No such step exception!\n");
            s.append(toString());
            throw new NoSuchStepException(s.toString());
        }
        myState.runNext();
    }

    @Override
    public final void run() {
        runAll_();
    }

    @Override
    public final void stop(String msg) {
        myStoppingMessage = msg;
        myStoppingFlag = true;
    }

    @Override
    public final void stop() {
        stop(null);
    }

    @Override
    public final boolean getStoppingFlag() {
        return myStoppingFlag;
    }

    /**
     * The iterative process will continue until there are no more steps or its
     * maximum execution time has been reached, whichever comes first. If this
     * method is called the iterative process will end processing (terminate)
     * before the next step and not process the next step in the process. The
     * current step will be completed. This method can be used to end the
     * process at an arbitrary step. Once ended, the process must be initialized
     * to run again.
     *
     * @param msg an option message to indicate the reason for stopping
     */
    @Override
    public final void end(String msg) {
        myStoppingMessage = msg;
        myState.end();
    }

    /**
     * The iterative process will continue until there are no more steps or its
     * maximum execution time has been reached, whichever comes first. If this
     * method is called the iterative process will end processing (terminate)
     * before the next step and not process the next step in the process. The
     * current step will be completed. This method can be used to end the
     * process at an arbitrary step. Once ended, the process must be restarted
     * via initialize().
     *
     */
    @Override
    public final void end() {
        end(null);
    }

    /**
     * This method should check to see if another step is necessary for the
     * iterative process. True means that the process has another step to be
     * executed. False, means that no more steps are available for execution.
     *
     * @return true if another step is present
     */
    abstract protected boolean hasNext();

    /**
     * This method should return the next step to be executed in the iterative
     * process or null if no more steps can be executed. It should advance the
     * current step to the next step if it is available
     *
     * @return the type of the step
     */
    abstract protected T next();

    /**
     * This method tells the iterative process to execute the current step.
     * Typical usage is to call this after calling next() to advance to the next
     * step. This method should throw a NoSuchStepException if there are no more
     * steps to run and it is told to run the step.
     *
     */
    abstract protected void runStep();

    /**
     * Can be overwritten by subclasses to have output when the console flag is
     * on
     *
     */
    protected void consoleOutput() {
    }

    @Override
    public boolean contains(ObserverIfc observer) {
        return myObservableComponent.contains(observer);
    }

    @Override
    public final void deleteObservers() {
        myObservableComponent.deleteObservers();
    }

    @Override
    public final void deleteObserver(ObserverIfc observer) {
        myObservableComponent.deleteObserver(observer);
    }

    @Override
    public final int countObservers() {
        return myObservableComponent.countObservers();
    }

    @Override
    public final void addObserver(ObserverIfc observer) {
        myObservableComponent.addObserver(observer);
    }

    /**
     * Returns if the elapsed execution time exceeds the maximum time allowed.
     * Only true if the maximum was set and elapsed time is greater than or
     * equal to getMaximumAllowedExecutionTime()
     *
     * @return true if the execution time exceeds
     * getMaximumAllowedExecutionTime()
     */
    @Override
    public final boolean isExecutionTimeExceeded() {
        if (myMaxAllowedExecutionTime <= 0) { // not set
            return false; // can't exceed what is not set
        } else {// execution time was set, check it
            return (getElapsedExecutionTime() >= myMaxAllowedExecutionTime);
        }
    }

    /**
     * The method is used to set the state and notify observers of the change
     *
     * @param state the state
     */
    protected final void setState(IterativeState state) {
        myState = state;
        myObservableComponent.notifyObservers(this, myCurrentStep);
    }

    /**
     *
     */
    protected void initializeIterations() {
        myStoppingMessage = null;
        myStoppingFlag = false;
        myDoneFlag = false;
        myRunningStepFlag = false;
        myRunningFlag = false;
//        System.out.println("**** initializeIterations() Executive running flag set to: " + myRunningFlag);
        myStepCounter = 0;

        if (mySteps != null) {
            mySteps.clear();
        }

        myBeginExecutionTime = System.currentTimeMillis();

        setState(myInitializedState);
        if (myTBConsoleUpdates > 0) {
            if (myTimer != null){
                myTimer.cancel();
                myTimer.purge();
            }
            myTimer = new Timer();
            myTimer.schedule(new ShowElapsedTimeTask(), myTBConsoleUpdates, myTBConsoleUpdates);
        }
        myInitFlag = true;

    }

    /**
     * Runs all of the steps. Each step is run, and finally the iterative
     * process is ended.
     *
     */
    protected final void runAll_() {
        if (myInitFlag == false) {
            initialize();
        }
        if (hasNext()) {
            while (!isDone()) {
                runNext();
            }
        } else {
            // no steps to execute
            myDoneFlag = true;
            myEndingStateIndicator = NO_STEPS_EXECUTED;
            myStoppingMessage = NO_STEPS_EXECUTED_MSG;
        }

        endIterations();
    }

    /**
     *
     */
    protected void runNext_() {
        myRunningFlag = true;
//        System.out.println("**** unNext_() Executive running flag set to: " + myRunningFlag);
        myRunningStepFlag = true;
        runStep();
        myRunningStepFlag = false;
        myStepCounter++;
        setState(myStepCompletedState);
        checkStoppingCondition_();
    }

    protected void checkStoppingCondition() {
    }

    protected void checkStoppingCondition_() {

        checkStoppingCondition();

        if (getStoppingFlag() == true) {
            // user called stop on the process
            myDoneFlag = true;
            if (myStoppingMessage == null) {
                // user message not available, set message to default
                myStoppingMessage = MET_STOPPING_CONDITION_MSG;
            }
            myEndingStateIndicator = MET_STOPPING_CONDITION;
        } else {
            // user did not call stop, check if it needs to stop
            if (!hasNext()) {
                // no more steps
                myDoneFlag = true;
                myEndingStateIndicator = COMPLETED_ALL_STEPS;
                myStoppingMessage = COMPLETED_ALL_STEPS_MSG;
            } else if (isExecutionTimeExceeded()) {
                myDoneFlag = true;
                myEndingStateIndicator = EXCEEDED_EXECUTION_TIME;
                myStoppingMessage = EXCEEDED_EXECUTION_TIME_MSG;
            }
        }

    }

    /**
     *
     */
    protected void endIterations() {
        myRunningFlag = false;
//        System.out.println("**** endIterations() Executive running flag set to: " + myRunningFlag);
        myRunningStepFlag = false;
        myDoneFlag = true;
        myEndExecutionTime = System.currentTimeMillis();
        setState(myEndedState);

        if (myTimer != null) {
            myTimer.cancel();
            System.out.println("Elapsed Clock Time: " + getElapsedExecutionTime() / 1000.0 + " seconds.");
            System.out.println(getName() + " ended.");
            myTimer = null;
        }
        myInitFlag = false;
    }

    protected class IterativeState {

        private String myName;

        public IterativeState(String name) {
            myName = name;
        }

        public void initialize() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nTried to initialize ");
            sb.append(getName());
            sb.append(" from an illegal state: ");
            sb.append(myState.toString());
            sb.append("\n");
            sb.append(IterativeProcess.this.toString());
            throw new IllegalStateException(sb.toString());
        }

        public void runNext() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nTried to run the next step of ");
            sb.append(getName());
            sb.append(" from an illegal state: ");
            sb.append(myState.toString());
            sb.append("\n");
            sb.append(IterativeProcess.this.toString());
            throw new IllegalStateException(sb.toString());
        }

        public void runAll() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nTried to run all the steps of ");
            sb.append(getName());
            sb.append(" from an illegal state: ");
            sb.append(myState.toString());
            sb.append("\n");
            sb.append(IterativeProcess.this.toString());
            throw new IllegalStateException(sb.toString());
        }

        public void end() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nTried to end ");
            sb.append(getName());
            sb.append(" from an illegal state: ");
            sb.append(myState.toString());
            sb.append("\n");
            sb.append(IterativeProcess.this.toString());
            throw new IllegalStateException(sb.toString());
        }

        @Override
        public String toString() {
            return (myName);
        }
    }

    protected class Created extends IterativeState {

        public Created() {
            super("CreatedState");
        }

        @Override
        public void initialize() {
            initializeIterations();
        }

        @Override
        public void end() {
            endIterations();
        }
    }

    protected class Initialized extends IterativeState {

        public Initialized() {
            super("InitializedState");
        }

        @Override
        public void runNext() {
            runNext_();
        }

        @Override
        public void runAll() {
            runAll_();
        }

        @Override
        public void end() {
            endIterations();
        }
    }

    protected class StepCompleted extends IterativeState {

        public StepCompleted() {
            super("StepCompleted");
        }

        @Override
        public void runNext() {
            runNext_();
        }

        @Override
        public void runAll() {
            runAll_();
        }

        @Override
        public void end() {
            endIterations();
        }
    }

    protected class Ended extends IterativeState {

        public Ended() {
            super("EndedState");
        }

        @Override
        public void initialize() {
            initializeIterations();
        }
    }

    protected class ShowElapsedTimeTask extends TimerTask {

        /*
         * (non-Javadoc) @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            System.out.println("Elapsed Clock Time: " + getElapsedExecutionTime() / 1000 + " seconds.");
        }
    }
}
