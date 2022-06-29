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

import jsl.calendar.CalendarIfc;
import jsl.calendar.PriorityQueueEventCalendar;
import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.observers.scheduler.ExecutiveTraceReport;
import jsl.observers.textfile.IPLogReport;
import jsl.utilities.IdentityIfc;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The Executive controls the execution of events, permits the scheduling of
 * events, updates the current time, and manages conditional actions.
 *
 * The Executive uses an instance of a class that extends IterativeProcess to
 * control its execution.
 *
 * The Executive uses an instance of a class that implements the CalendarIfc
 * interface to manage the time ordered execution of events.
 *
 * The Executive uses an instance of a ConditionalActionProcessor to manage the
 * execution of ConditionalActions.
 *
 * The event calendar and attached conditional actions are cleared after the
 * initialize() method is called
 *
 * The Executive can be pre-loaded with events and conditional actions prior to
 * invoking the run() method if and only if the initialize() method had already
 * been called. If the run() method is called without calling initialize() the
 * calendar and conditional actions are cleared.
 *
 * If an ending event is not scheduled using the scheduleEndEvent() method and
 * no real clock time execution limit has been set, then a default message will
 * be sent to JSL.LOGGER.warning(). This message can be turned off by calling
 * setTerminationWarningMessageOption(false)
 *
 * @author rossetti
 */
public class Executive implements IdentityIfc, ObservableIfc, IterativeProcessIfc {

    /**
     * Used when observers are notified after all events are executed
     *
     */
    public final static int AFTER_EXECUTION = ModelElement.getNextEnumConstant();

    /**
     * Used when observers are notified of initialization
     *
     */
    public final static int INITIALIZED = ModelElement.getNextEnumConstant();

    /**
     * Used to indicate to observers that an event will be executed
     *
     */
    public final static int BEFORE_EVENT = ModelElement.getNextEnumConstant();

    /**
     * Used to indicate to observers that an event will be executed
     *
     */
    public final static int AFTER_EVENT = ModelElement.getNextEnumConstant();

    /**
     * Keeps track of the status for observers
     *
     */
    private int myObserverState;

    /**
     * A counter to count the number of objects created to assign "unique" ids
     */
    private static int myIdCounter_;

    /**
     * The name of this object
     */
    private String myName;

    /**
     * The id of this object
     */
    private final int myId;

    /**
     * The reference to the event calendar
     */
    private final CalendarIfc myEventCalendar;

    /**
     * The current simulated time
     */
    private double myCurrentTime;

    /**
     * A reference to the event that just executed
     */
    private JSLEvent myLastExecutedEvent;

    /**
     * A counter that tracks the number of events that have been scheduled
     */
    private long myNumEventsScheduled;

    /**
     * Records the number of events scheduled during the execution
     *
     */
    private double myNumEventsScheduledDuringExecution;

    /**
     * A counter that tracks the number of event executed
     */
    private long myNumEventsExecuted;

    /**
     * Allows the object to be observed
     */
    protected ObservableComponent myObservableComponent;

    /**
     * Controls the execution of events over time
     */
    protected EventExecutionProcess myEventExecutionProcess;

    /**
     * Provides for 3 phase method for conditional events
     *
     */
    private ConditionalActionProcessor myConditionalActionProcessor;

    /**
     * The event that represents the end of the event processing
     *
     */
    private JSLEvent myEndEvent;

    /**
     * The time that the executive actually ended. This may be different than
     * getScheduledEndTime()
     *
     */
    private double myActualEndingTime;

    /**
     * A flag to indicate that the scheduler is to be traced
     */
    private boolean myTraceReportFlag;

    /**
     * A reference to a tracing report
     */
    private ExecutiveTraceReport myTraceReport;

    /**
     * A flag to control whether or not a warning is issues if the user does not
     * set the end event
     *
     */
    private boolean myTerminationWarningMsgOption = true;

    private Simulation mySimulation;

    public Executive() {
        this(null, null);
    }

    public Executive(CalendarIfc c) {
        this(null, c);
    }

    public Executive(String name, CalendarIfc c) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        setName(name);
        if (c == null) {
            c = new PriorityQueueEventCalendar();
        }
        myEventCalendar = c;
        myNumEventsScheduled = 0;
        myNumEventsScheduledDuringExecution = Double.NaN;
        myNumEventsExecuted = 0;
        myCurrentTime = 0.0;
        myActualEndingTime = Double.NaN;
        myLastExecutedEvent = null;
        myObserverState = 0;
        myEndEvent = null;
        myConditionalActionProcessor = new ConditionalActionProcessor();
        myObservableComponent = new ObservableComponent();
        myEventExecutionProcess = new EventExecutionProcess();
    }

    /** Used internally by Simulation to assign itself to the Executive
     *
     * @param simulation the simulation to set
     */
    protected final void setSimulation(Simulation simulation){
        Objects.requireNonNull(simulation, "The supplied simulation was null");
        mySimulation = simulation;
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
     * Returns the id for this object
     *
     * @return the id
     */
    @Override
    public final int getId() {
        return (myId);
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            myName = this.getClass().getSimpleName();
        } else {
            myName = str;
        }
    }

    /**
     *
     * @return true if the flag permits the message to be printed
     */
    public final boolean getTerminationWarningMessageOption() {
        return myTerminationWarningMsgOption;
    }

    /**
     * False turns off the message
     *
     * @param flag true means off
     */
    public final void setTerminationWarningMessageOption(boolean flag) {
        myTerminationWarningMsgOption = flag;
    }

    /**
     * Can be used by observers to check what occurred
     *
     * @return an integer representation of the observer state
     */
    public final int getObserverState() {
        return (myObserverState);
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

    @Override
    public boolean contains(ObserverIfc observer) {
        return myObservableComponent.contains(observer);
    }

    @Override
    public void turnOnLogReport(Path pathToFile) {
        myEventExecutionProcess.turnOnLogReport(pathToFile);
    }

    @Override
    public void turnOffLogReport() {
        myEventExecutionProcess.turnOffLogReport();
    }

    protected final void notifyObservers(JSLEvent e) {
        myObservableComponent.notifyObservers(this, e);
    }

    /**
     * Returns the time the execution was scheduled to end
     *
     * @return the scheduled end time or Double.POSITIVE_INFINITY
     */
    public final double getScheduledEndTime() {
        if (myEndEvent == null) {
            return Double.POSITIVE_INFINITY;
        }
        return myEndEvent.getTime();
    }

    /**
     * This method allows a previously *executed* event to be reused The event
     * must have already been removed from the calendar through the natural
     * execute event mechanism and have been executed.
     *
     * NOTE: If an event is canceled it remains in the scheduler until its
     * originally scheduled event time. Then and only then does it become
     * unscheduled at which time the event can be rescheduled. If a client needs
     * to reschedule a canceled event prior to the originally scheduled event
     * time, then just use scheduleEvent() to make a new event.
     *
     * @param <T> the type of the event message
     * @param event The event that needs rescheduling, cannot be null and cannot already be scheduled
     * @param time represents the inter-event time, i.e. the interval from the
     * current time to when the event will need to occur. Cannot be negative
     */
    public final <T> void reschedule(JSLEvent<T> event, double time) {
        Objects.requireNonNull(event, "The supplied event was null");
        if (event.isScheduled()) {
            throw new IllegalArgumentException("Attempted to reschedule an already scheduled event.");
        }
        event.setCanceledFlag(false);
        event.setTime(getTime() + time);
        schedule(event);
    }

    /**
     * Creates an event and schedules it onto the event calendar
     *
     * @param <T> the type of the event message
     * @param listener represents an ActionListener that will handle the change
     * of state logic, cannot be null
     * @param time represents the inter-event time, i.e. the interval from the
     * current time to when the event will need to occur, Cannot be negative
     * @param priority is used to influence the ordering of events
     * @param message is a generic Object that may represent data to be
     * transmitted with the event, may be null
     * @param name the name of the event, can be null
     * @param theElementScheduling the element doing the scheduling, cannot be null
     * @return a valid JSLEvent
     */
    public final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> listener,
            double time, int priority, T message, String name, ModelElement theElementScheduling) {

        // create the event
        JSLEvent<T> event = new JSLEvent<>();

        // prepare the event
        event.setName(name);
        event.setEventAction(listener);
        event.setTime(getTime() + time);
        event.setPriority(priority);
        event.setMessage(message);
        event.setModelElement(theElementScheduling);

        schedule(event);
        return (event);
    }

    /**
     * Tells the event calendar to cancel the provided event. The event must
     * have been scheduled otherwise an IllegalArgumentException is thrown.
     *
     * @param e A reference to the event to be canceled.
     */
    public final void cancel(JSLEvent e) {
        if (!e.isScheduled()) {
            throw new IllegalArgumentException("Attempted to cancel an unscheduled event.");
        }
        myEventCalendar.cancel(e);
    }

    /**
     * Allows a check of the event calendar to see if it is empty, i.e. it does
     * not have any more events. Note: Depending on how the event calendar
     * handles canceled events, canceled events may or may not be removed. In
     * the default, canceled events are not removed from the calendar, until
     * after their event time has occurred.
     *
     * @return True is empty, False is not empty
     */
    public final boolean isEmpty() {
        return myEventCalendar.isEmpty();
    }

    /**
     * Returns the current simulated time as a double
     *
     * @return Simulated time as a double
     *
     */
    public final double getTime() {
        return (myCurrentTime);
    }

    /**
     * @return A string representing the Executive.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Number of events scheduled: ");
        sb.append(getNumberEventsScheduled());
        sb.append(System.lineSeparator());
        sb.append("Number of events scheduled during execution: ");
        sb.append(getNumberEventsScheduledDuringExecution());
        sb.append(System.lineSeparator());
        sb.append("Number of events executed: ");
        sb.append(getTotalNumberEventsExecuted());
        sb.append(System.lineSeparator());
        sb.append("Scheduled end time: ");
        sb.append(getScheduledEndTime());
        sb.append(System.lineSeparator());
        sb.append("Actual Ending time: ");
        sb.append(getActualEndingTime());
        sb.append(System.lineSeparator());
        sb.append("Current time: ");
        sb.append(getTime());
        sb.append(System.lineSeparator());
        sb.append(myEventExecutionProcess);
        return (sb.toString());
    }

    /**
     * Gets the number of events currently scheduled
     *
     * @return number currently scheduled
     */
    public final double getNumberEventsScheduled() {
        return myNumEventsScheduled;
    }

    /**
     * Gets the number of events that were scheduled during the execution
     *
     * @return number during the execution
     */
    public final double getNumberEventsScheduledDuringExecution() {
        return myNumEventsScheduledDuringExecution;
    }

    /**
     * Gets the total number of events executed
     *
     * @return the total number of events executed
     */
    public final double getTotalNumberEventsExecuted() {
        return myNumEventsExecuted;
    }

    /**
     * The simulated time that the Executive actually ended
     *
     * @return the simulated time that the Executive actually ended
     */
    public double getActualEndingTime() {
        return myActualEndingTime;
    }

    /**
     * Returns true if an event has been scheduled to stop execution at
     * getTimeHorizon()
     *
     * @return true if scheduled
     */
    public final boolean isEndEventScheduled() {
        return (myEndEvent != null);
    }

    /**
     * Checks to see if the event calendar has another event
     *
     * @return true if it has another event
     */
    public final boolean hasNextEvent() {
        return !myEventCalendar.isEmpty();
    }

    /**
     * Returns a reference to the next event or null if no event exists. Does
     * not remove or execute the event. The event remains at the "top" of the
     * calendar.
     *
     * @return a reference to the next event
     */
    public final JSLEvent peekNextEvent() {
        return myEventCalendar.peekNext();
    }

    /**
     * Returns a reference to the last executed event or null if no events have
     * been executed or no more events
     *
     * @return the last event executed
     */
    public final JSLEvent getLastExecutedEvent() {
        return myLastExecutedEvent;
    }

    /**
     * Initialize the executive, making it ready to run events This clears any
     * events in the calendar and prepares for execution
     */
    @Override
    public final void initialize() {
        myEventExecutionProcess.initialize();
    }

    /**
     * Causes the next event to be executed if it exists
     *
     */
    public final void executeNextEvent() {
        myEventExecutionProcess.runNext();
    }

    /*
     * Executes all the events in the calendar, clears the calendar if the
     * Executive has not been initialized
     *
     */
    public final void executeAllEvents() {
        if (!willTerminate()) {
            // no end event scheduled, no max exec time, warn the user
            if (getTerminationWarningMessageOption()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Executive: In initializeIterations()\n");
                sb.append("The executive was told to run all events \n");
                sb.append("without an end event scheduled.\n");
                sb.append("There was no maximum real-clock execution time specified. \n");
                sb.append("The user is responsible for ensuring that the Executive is stopped.\n");
                Simulation.LOGGER.warn(sb.toString());
                System.out.flush();
            }
        }
        myEventExecutionProcess.run();
    }

    @Override
    public final void end() {
        String msg = "The executive was told to end by the user at time " + getTime();
        myEventExecutionProcess.end(msg);
    }

    @Override
    public final void end(String msg) {
        myEventExecutionProcess.end(msg);
    }

    @Override
    public final boolean isUnfinished() {
        return myEventExecutionProcess.isUnfinished();
    }

    public final boolean isTimedOut() {
        return myEventExecutionProcess.executionTimeExceeded();
    }

    @Override
    public final boolean isStepCompleted() {
        return myEventExecutionProcess.isStepCompleted();
    }

    @Override
    public final boolean isRunning() {
        return myEventExecutionProcess.isRunning();
    }

    @Override
    public final boolean isInitialized() {
        return myEventExecutionProcess.isInitialized();
    }

    @Override
    public final boolean isCreated() {
        return myEventExecutionProcess.isCreated();
    }

    @Override
    public final boolean isEnded() {
        return myEventExecutionProcess.isEnded();
    }

    public final boolean isEndConditionMet() {
        return myEventExecutionProcess.stoppedByCondition();
    }

    @Override
    public final boolean isDone() {
        return myEventExecutionProcess.isDone();
    }

    public final boolean isCompleted() {
        return myEventExecutionProcess.allStepsCompleted();
    }

    @Override
    public final long getMaximumAllowedExecutionTime() {
        return myEventExecutionProcess.getMaximumAllowedExecutionTime();
    }

    @Override
    public final void setMaximumExecutionTime(long milliseconds) {
        myEventExecutionProcess.setMaximumExecutionTime(milliseconds);
    }

    @Override
    public final long getEndExecutionTime() {
        return myEventExecutionProcess.getEndExecutionTime();
    }

    @Override
    public final long getElapsedExecutionTime() {
        return myEventExecutionProcess.getElapsedExecutionTime();
    }

    @Override
    public final long getBeginExecutionTime() {
        return myEventExecutionProcess.getBeginExecutionTime();
    }

    @Override
    public final String getStoppingMessage() {
        return myEventExecutionProcess.getStoppingMessage();
    }

    /**
     * This method creates an instance of the default ExecutiveTraceReport and
     * tells it to observe the scheduler.
     */
    public final void turnOnDefaultEventTraceReport() {
        turnOnDefaultEventTraceReport(null);
    }

    /**
     * This method creates an instance of the default ExecutiveTraceReport and
     * tells it to observe the scheduler.
     *
     * @param name specifies a prefix name for the report
     */
    public final void turnOnDefaultEventTraceReport(String name) {
        if (myTraceReportFlag == false) {
            String s;
            if (name != null) {
                s = name + " " + getName() + " TraceReport";
            } else {
                s = getName() + " TraceReport";
            }
            Path path = mySimulation.getOutputDirectory().getOutDir().resolve(s);
            myTraceReport = new ExecutiveTraceReport(path);
            addObserver(myTraceReport);
            myTraceReportFlag = true;
        }
    }

    /**
     * Removes the default ExecutiveTraceReport as an observer of the Executive
     * and sets it to null.
     */
    public final void turnOffDefaultEventTraceReport() {
        if (myTraceReportFlag == true) {
            deleteObserver(myTraceReport);
            myTraceReportFlag = false;
            myTraceReport = null;
        }
    }

    /**
     * Returns the default event trace report or null if it has not yet been
     * turned on.
     *
     * @return the ExecutiveTraceReport
     */
    public final Optional<ExecutiveTraceReport> getDefaultExecutiveTraceReport() {
        return Optional.ofNullable(myTraceReport);
    }

    public final void unregisterAllActions() {
        myConditionalActionProcessor.unregisterAllActions();
    }

    public void unregister(ConditionalAction action) {
        myConditionalActionProcessor.unregister(action);
    }

    public final void setMaxScans(int max) {
        myConditionalActionProcessor.setMaxScans(max);
    }

    public final void setMaxScanFlag(boolean flag) {
        myConditionalActionProcessor.setMaxScanFlag(flag);
    }

    public final void register(ConditionalAction action, int priority) {
        myConditionalActionProcessor.register(action, priority);
    }

    public final void register(ConditionalAction action) {
        myConditionalActionProcessor.register(action);
    }

    public final int getMaxScans() {
        return myConditionalActionProcessor.getMaxScans();
    }

    public final boolean getMaxScanFlag() {
        return myConditionalActionProcessor.getMaxScanFlag();
    }

    public final void changePriority(ConditionalAction action, int priority) {
        myConditionalActionProcessor.changePriority(action, priority);
    }

    @Override
    public final void stop() {
        myEventExecutionProcess.stop();
    }

    @Override
    public final void stop(String msg) {
        myEventExecutionProcess.stop(msg);
    }

    /**
     * Executes the provided event
     *
     * @param event represents the next event to execute or null
     */
    protected void execute(JSLEvent event) {

        try {
            if (event != null) {
                // the event is no longer scheduled
                event.setScheduledFlag(false);
                if (event.getCanceledFlag() == false) {
                    // event was not cancelled
                    // update the current simulation time to the event time
                    myCurrentTime = event.getTime();
                    myObserverState = BEFORE_EVENT;
                    notifyObservers(event);
                    event.execute();
                    myLastExecutedEvent = event;
                    myNumEventsExecuted = myNumEventsExecuted + 1;
                    myObserverState = AFTER_EVENT;
                    notifyObservers(event);
                    performCPhase();
                }
            }
        } catch (RuntimeException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("######################################");
            sb.append(System.lineSeparator());
            sb.append("A RuntimeException occurred near this event:");
            sb.append(System.lineSeparator());
            sb.append(event);
            sb.append(System.lineSeparator());
            sb.append("######################################");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            if (event != null) {
                if (event.getModelElement() != null) {
                    Simulation sim = event.getModelElement().getSimulation();
                    sb.append(sim);
                }
            }
            Simulation.LOGGER.error(sb.toString());
            throw e;
        }

    }

    protected void performCPhase() {
        if (myConditionalActionProcessor == null) {
            return;
        }

        JSLEvent ne = peekNextEvent();
        if (ne == null) {
            return;
        } else if (ne.getTime() > getTime()) {
            myConditionalActionProcessor.performCPhase();
        }
    }

    protected void setConditionalActionProcessor(ConditionalActionProcessor p) {
        myConditionalActionProcessor = p;
    }

    protected void schedule(JSLEvent e) {
        if (isCreated() || isEnded()) {
            StringBuilder sb = new StringBuilder();
            sb.append("An event was scheduled when the Executive is in the created or ended state.");
            sb.append(System.lineSeparator());
            sb.append("Since the Executive has not yet been initialized this event will not execute.");
            sb.append(System.lineSeparator());
            sb.append("The offending event is : ").append(e);
            sb.append(System.lineSeparator());
            sb.append("It is likely that the user scheduled the event in a ScheduleElement's constructor or outside the context of run().");
            sb.append(System.lineSeparator());
            sb.append("Do not schedule initial events in a constructor.  Use the initialize() method instead.");
            sb.append(System.lineSeparator());
            sb.append("Do not schedule initial events prior to executing the simulation.  Use the initialize() method instead.");
            sb.append(System.lineSeparator());
            Simulation.LOGGER.warn(sb.toString());
            System.out.flush();
        }
        if (e.isScheduled()) {
            throw new JSLEventException("Event has already been scheduled!");
        }

        if (e.getTime() < getTime()) {
            throw new JSLEventException("Event to be Scheduled before Current Time!");
        }

        if (e.getTime() <= getScheduledEndTime()) {
            myNumEventsScheduled = myNumEventsScheduled + 1;
            e.setId(myNumEventsScheduled);
            myEventCalendar.add(e);
            e.setScheduledFlag(true);
        }
    }

    /**
     * Schedules the ending of the executive at the provided time
     *
     * @param time the time of the ending event, must be &gt; 0
     * @param theElement the associated model element
     * @return the scheduled event
     */
    public final JSLEvent scheduleEndEvent(double time, ModelElement theElement) {
//TODO consider making protected
// the only side-effect is in the Executive test suite
        if (time <= 0.0) {
            throw new IllegalArgumentException("The time must be > 0.0");
        }

        if (isEndEventScheduled()) {
            // already scheduled end event, cancel it
            cancel(myEndEvent);
        }
        //System.out.println("Executive: scheduling end of replication at time: " + time);
        // schedule the new time
        myEndEvent = scheduleEvent(new EndEventAction(), time,
                JSLEvent.DEFAULT_END_REPLICATION_EVENT_PRIORITY, null,
                "End Replication", theElement);
        return myEndEvent;
    }

    /**
     * Returns the Executive's end event if scheduled
     *
     * @return The Executive's end event if scheduled, or null if not
     */
    public final JSLEvent getEndEvent() {
        return myEndEvent;
    }

    @Override
    public boolean isExecutionTimeExceeded() {
        return myEventExecutionProcess.isExecutionTimeExceeded();
    }

    @Override
    public long getNumberStepsCompleted() {
        return myEventExecutionProcess.getNumberStepsCompleted();
    }

    @Override
    public IPLogReport getLogReport() {
        return myEventExecutionProcess.getLogReport();
    }

    @Override
    public void turnOnTimer(long milliseconds) {
        myEventExecutionProcess.turnOnTimer(milliseconds);
    }

    @Override
    public boolean allStepsCompleted() {
        return myEventExecutionProcess.allStepsCompleted();
    }

    @Override
    public boolean executionTimeExceeded() {
        return myEventExecutionProcess.executionTimeExceeded();
    }

    @Override
    public boolean stoppedByCondition() {
        return myEventExecutionProcess.stoppedByCondition();
    }

    @Override
    public void runNext() {
        executeNextEvent();
    }

    @Override
    public void run() {
        executeAllEvents();
    }

    @Override
    public boolean getStoppingFlag() {
        return myEventExecutionProcess.getStoppingFlag();
    }

    @Override
    public boolean isRunningStep() {
        return myEventExecutionProcess.isRunningStep();
    }

    @Override
    public boolean noStepsExecuted() {
        return myEventExecutionProcess.noStepsExecuted();
    }

    private class EndEventAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            String msg = "Executive: Scheduled end event occurred at time " + getTime();
            myEventExecutionProcess.stop(msg);
        }
    }

    /**
     * This method is called before any events are executed and after
     * initializing the iterative process. It can be used to insert behavior
     * after initializing
     *
     */
    protected void beforeExecutingAnyEvents() {
    }

    /**
     * This method is called after executing all events when ending the
     * iterative process. It can be used to insert behavior after the executive
     * ends
     *
     */
    protected void afterExecution() {
    }

    /**
     * Tells the event calendar to clear all the events. Resets the simulation
     * time to 0.0. Resets the event identification counter. Unregisters all
     * actions Notifies observers of initialization
     */
    protected void initializeCalendar() {
        myEndEvent = null;
        myLastExecutedEvent = null;
        myCurrentTime = 0.0;
        myActualEndingTime = Double.NaN;
        myEventCalendar.clear();
        unregisterAllActions();
        myNumEventsScheduled = 0;
        myNumEventsExecuted = 0;
        myObserverState = INITIALIZED;
        notifyObservers(null);
    }

    /**
     * If the Executive has an end event or maximum allowed execution time, then
     * return true
     *
     * @return true if the executive has an end event or max allowed execution
     * time
     */
    public final boolean willTerminate() {
        boolean flag = true;
        if (!isEndEventScheduled()) {
            if (getMaximumAllowedExecutionTime() == 0) {
                flag = false;
            }
        }
        return flag;
    }

    protected class EventExecutionProcess extends IterativeProcess<JSLEvent> {

        @Override
        protected final void initializeIterations() {
            super.initializeIterations();
            initializeCalendar();
            beforeExecutingAnyEvents();
        }

        @Override
        protected final void endIterations() {
            super.endIterations();
            // record the actual ending time
            myActualEndingTime = getTime();
            // record # events scheduled during execution
            myNumEventsScheduledDuringExecution = myNumEventsScheduled;
            // set observer state and notify observers
            myObserverState = AFTER_EXECUTION;
            notifyObservers(null);
            afterExecution();
        }

        @Override
        protected boolean hasNext() {
            return (hasNextEvent());
        }

        @Override
        protected JSLEvent next() {
            return (myEventCalendar.nextEvent());
        }

        @Override
        protected void runStep() {
            myCurrentStep = next();
            execute(myCurrentStep);
        }
    }
}
