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
package jsl.modeling.elements.component;

import jsl.simulation.*;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.IllegalStateException;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.RVariableIfc;

import java.util.LinkedList;
import java.util.List;

public class Component extends SchedulingElement {

    // ===========================================
    // CLASS CONSTANTS
    // ===========================================
    /**
     * Indicates that the transporter has changed state to its observers
     */
    public static final int STATE_CHANGE = ModelElement.getNextEnumConstant();

    //  ===========================================
    //      Attributes
    //  ===========================================
    /** A variable representing the time associated with
     *  operating the component for the current operation
     */
    protected double myOperationTime;

    /** A random variable representing the time associated with
     *  operating the component.
     */
    protected RandomVariable myOperationTimeRV;

    /** A distribution governing the time associated with operating
     *  the component
     */
    protected RVariableIfc myOperationTimeCDF;

    /** If the component has been scheduled to operate this
     *  event represents the end of the operation time
     */
    protected JSLEvent myEndOperationEvent;

    /** Listens for the end operation event
     */
    private EndOperationListener myEndOperationListener = new EndOperationListener();

    /** Represents the amount of time to failure when a failure event has been scheduled
     */
    protected double myTimeToFailure;

    /** A random variable representing the amount of time to failure when a failure event has been scheduled
     */
    protected RandomVariable myTimeToFailureRV;

    /** A CDF governing the amount of time to failure when a failure event has been scheduled
     */
    protected RVariableIfc myTimeToFailureCDF;

    /** If the component has been scheduled to fail this
     *  event represents the end of the time to failure
     */
    protected JSLEvent myFailureEvent;

    /** Listens for the failure event
     */
    private FailureListener myFailureListener = new FailureListener();

    /** A variable representing the amount of time to repair the component
     *  for the current repair
     */
    protected double myRepairTime;

    /** A random variable representing the amount of time to repair the component
     */
    protected RandomVariable myRepairTimeRV;

    /** Governs the RV representing the amount of time to repair the component
     */
    protected RVariableIfc myRepairTimeCDF;

    /** Represents the amount of time to repair the component
     */
    protected JSLEvent myEndRepairEvent;

    /** Indicates that the component has been repaired since
     *  it was last operated
     */
    protected boolean myRepairedSinceLastOperatedFlag;

    /** Listens for the end of repair event
     */
    private EndRepairListener myEndRepairListener = new EndRepairListener();

    /** Keeps track of the current state of the component
     */
    private ComponentState myState;

    /** Remembers the previous state of the component
     */
    private ComponentState myPreviousState;

    /**
     * The component is in the created state right after it
     * has been constructed
     *
     */
    protected ComponentState myCreatedState;

    /**
     *  Before being operated, the component must be placed
     *  in the activated state
     */
    protected ComponentState myAvailableState;

    /**
     * The component can be deactivated, it can do nothing
     * but be activated
     */
    protected ComponentState myUnavailableState;

    /**
     * The component can be placed in the failed state after
     * it has been placed in the operating state
     */
    protected ComponentState myFailedState;

    /**
     * The component can be placed in the operating state
     * after it has been activated
     */
    protected ComponentState myOperatingState;

    /**
     * The component can be placed in the repairing state
     * after it has failed.
     */
    protected ComponentState myRepairingState;

    /** Indicates whether or not the component will start operating
     *  at initialization, the default is false
     */
    private boolean myOperateAtInitializationFlag = false;

    /** Indicates whether or not the component will automatically
     *  restart operating after it completes an operation
     *
     */
    private boolean myAutomaticRestartAfterOperatingFlag = false;

    /** Indicates whether or not the component will automatically
     *  start repair after it fails
     *
     */
    private boolean myAutomaticStartRepairAfterFailureFlag = false;

    /** Indicates whether or not the component will automatically
     *  start operating after it completes repair
     *
     */
    private boolean myAutomaticRestartAfterRepairFlag = false;

    /** A list to hold listeners for the change of state of the component
     *
     */
    private List<ComponentStateChangeListenerIfc> myStateChangeListeners;

    /** Every component may belong to a composite (assembly).
     *  An assembly may hold many components.  The assembly is
     *  notified of component state changes prior to any other listeners
     *
     */
    private ComponentAssembly myAssembly;

    //  ===========================================
    //      Constructors
    //  ===========================================
    public Component(ModelElement parent) {
        this(parent, null, null, null, null);
    }

    /** Creates a component.  The following defaults are used:
     *
     *  timeToFailureCDF = new Constant(Double.POSITIVE_INFINITY)
     *  operationTimeCDF = new Constant(0.0)
     *  repairTimeCDF = new Constant(0.0)
     *
     * @param parent The model element serving as the parent
     * @param name The name of the component
     */
    public Component(ModelElement parent, String name) {
        this(parent, null, null, null, name);
    }

    /** Creates a component.  The following defaults are used:
     *
     *  operationTimeCDF = new Constant(0.0)
     *  repairTimeCDF = new Constant(0.0)
     *
     * @param parent The model element serving as the parent
     * @param timeToFailureCDF The time to failure CDF
     * @param name The name of the component
     */
    public Component(ModelElement parent, RVariableIfc timeToFailureCDF, String name) {
        this(parent, timeToFailureCDF, null, null, name);
    }

    /** Creates a component.  The following defaults are used:
     *
     *  repairTimeCDF = new Constant(0.0)
     *
     * @param parent The model element serving as the parent
     * @param timeToFailureCDF The time to failure CDF
     * @param operationTimeCDF The operation time CDF
     * @param name The name of the component
     */
    public Component(ModelElement parent, RVariableIfc timeToFailureCDF, RVariableIfc operationTimeCDF, String name) {
        this(parent, timeToFailureCDF, operationTimeCDF, null, name);
    }

    /** Creates a component.  If the time to failure CDF, operation time CDF, or
     *  repair time CDF are null then the following defaults are used:
     *
     *  timeToFailureCDF = new Constant(Double.POSITIVE_INFINITY)
     *  operationTimeCDF = new Constant(0.0)
     *  repairTimeCDF = new Constant(0.0)
     *
     * @param parent The model element serving as the parent
     * @param timeToFailureCDF The time to failure CDF
     * @param operationTimeCDF The operation time CDF
     * @param repairTimeCDF The repair time CDF
     */
    public Component(ModelElement parent, RVariableIfc timeToFailureCDF, RVariableIfc operationTimeCDF, RVariableIfc repairTimeCDF) {
        this(parent, timeToFailureCDF, operationTimeCDF, repairTimeCDF, null);
    }

    /** Creates a component.  If the time to failure CDF, operation time CDF, or
     *  repair time CDF are null then the following defaults are used:
     *
     *  timeToFailureCDF = new Constant(Double.POSITIVE_INFINITY)
     *  operationTimeCDF = new Constant(0.0)
     *  repairTimeCDF = new Constant(0.0)
     *
     * @param parent The model element serving as the parent
     * @param timeToFailureCDF The time to failure CDF
     * @param operationTimeCDF The operation time CDF
     * @param repairTimeCDF The repair time CDF
     * @param name The name of the component
     */
    public Component(ModelElement parent, RVariableIfc timeToFailureCDF, RVariableIfc operationTimeCDF,
                     RVariableIfc repairTimeCDF, String name) {
        super(parent, name);

        if (timeToFailureCDF == null) {
            myTimeToFailureCDF = ConstantRV.POSITIVE_INFINITY;
        } else {
            myTimeToFailureCDF = timeToFailureCDF;
        }

        if (operationTimeCDF == null) {
            myOperationTimeCDF = ConstantRV.ZERO;
        } else {
            myOperationTimeCDF = operationTimeCDF;
        }

        if (repairTimeCDF == null) {
            myRepairTimeCDF = ConstantRV.ZERO;
        } else {
            myRepairTimeCDF = repairTimeCDF;
        }

        myTimeToFailureRV = new RandomVariable(this, myTimeToFailureCDF);
        myOperationTimeRV = new RandomVariable(this, myOperationTimeCDF);
        myRepairTimeRV = new RandomVariable(this, myRepairTimeCDF);

        myCreatedState = new Created();
        myAvailableState = new Available();
        myUnavailableState = new Unavailable();
        myFailedState = new Failed();
        myOperatingState = new Operating();
        myRepairingState = new Repairing();

        myStateChangeListeners = new LinkedList<ComponentStateChangeListenerIfc>();

        myState = myCreatedState;
        myState.enter(0.0);
        myRepairTime = Double.NaN;
        myOperationTime = Double.NaN;
        myRepairedSinceLastOperatedFlag = true;
        myFailureEvent = null;
        myEndOperationEvent = null;
        myEndRepairEvent = null;
    }

    //  ===========================================
    //      Public Methods
    //  ===========================================
    /** Attaches a component state change listener to the component
     *
     * @param listener The listener to be attached, must be non-null and not already attached
     */
    public final void attachStateChangeListener(ComponentStateChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null.");
        }

        if (myStateChangeListeners.contains(listener)) {
            throw new IllegalArgumentException("The listener is already attached to the component");
        }

        myStateChangeListeners.add(listener);

    }

    /** Removes an attached component state change listener from the component
     *
     * @param listener, Must not be null
     * @return True if the remove was successful
     */
    public final boolean removeStateChangeListener(ComponentStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null.");
        }

        return (myStateChangeListeners.remove(listener));
    }

    /** Checks if the listener has already been attached to the component
     *
     * @param listener, must not be null
     * @return True if it has already been attached
     */
    public final boolean containsStateChangeListener(ComponentStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null.");
        }

        return (myStateChangeListeners.contains(listener));
    }

    /** Returns true if the component is in the created state
     * @return
     */
    public final boolean isCreated() {
        return (myState == myCreatedState);
    }

    /** Returns true if the component is in the activated state
     * @return
     */
    public final boolean isAvailable() {
        return (myState == myAvailableState);
    }

    /** Returns true if the component is in the deactivated state
     *
     * @return
     */
    public final boolean isUnavailable() {
        return (myState == myUnavailableState);
    }

    /** Returns true if the component is in the failed state
     * @return
     */
    public final boolean isFailed() {
        return (myState == myFailedState);
    }

    /** Returns true if the component is in the operating state
     * @return
     */
    public final boolean isOperating() {
        return (myState == myOperatingState);
    }

    /** Returns true if the component is in the being repaired state
     * @return
     */
    public final boolean isInRepair() {
        return (myState == myRepairingState);
    }

    /** Returns true if the component has a failure event scheduled
     * @return
     */
    public final boolean isFailurePending() {
        return (myFailureEvent != null);
    }

    /** Returns true if the component has an end of operation event scheduled
     * @return
     */
    public final boolean isEndOfOperationPending() {
        return (myEndOperationEvent != null);
    }

    /** Returns true if the component has an end of repair event scheduled
     * @return
     */
    public final boolean isEndOfRepairPending() {
        return (myEndRepairEvent != null);
    }

    /** Returns the current state of the component
     * @return
     */
    public final StateAccessorIfc getComponentState() {
        return (myState);
    }

    /** Returns the previous state of the component
     * @return
     */
    public final StateAccessorIfc getPreviousComponentState() {
        return (myPreviousState);
    }

    /** Returns true if the component is in the supplied state
     *
     * @param state
     * @return
     */
    public final boolean isState(StateAccessorIfc state) {
        return (state == myState);
    }

    /** Returns true if the component is in the supplied state
     *
     * @param state
     * @return
     */
    public final boolean isPreviousState(StateAccessorIfc state) {
        return (state == myPreviousState);
    }

    /**
     * @return Returns the activatedState.
     */
    public final StateAccessorIfc getActivatedState() {
        return myAvailableState;
    }

    /**
     * @return Returns the createdState.
     */
    public final StateAccessorIfc getCreatedState() {
        return myCreatedState;
    }

    /**
     * @return Returns the deactivatedState.
     */
    public final StateAccessorIfc getDeactivatedState() {
        return myUnavailableState;
    }

    /**
     * @return Returns the failedState.
     */
    public final StateAccessorIfc getFailedState() {
        return myFailedState;
    }

    /**
     * @return Returns the operatingState.
     */
    public final StateAccessorIfc getOperatingState() {
        return myOperatingState;
    }

    /**
     * @return Returns the repairingState.
     */
    public final StateAccessorIfc getRepairingState() {
        return myRepairingState;
    }

    /** Sets the time to failure distribution for the component
     *
     * @param distribution, must not be null
     */
    public final void setTimeToFailureCDFInitialRandomSource(RVariableIfc distribution) {
        if (distribution == null) {
            throw new IllegalArgumentException("The time to failure distribution was null!");
        }

        myTimeToFailureRV.setInitialRandomSource(distribution);
    }

    /** Sets the operation time distribution for the component
     *
     * @param distribution, must not be null
     */
    public final void setOperationTimeCDFInitialRandomSource(RVariableIfc distribution) {
        if (distribution == null) {
            throw new IllegalArgumentException("The operation time distribution was null!");
        }

        myOperationTimeRV.setInitialRandomSource(distribution);
    }

    /** Sets the repair time distribution for the component
     *
     * @param distribution, must not be null
     */
    public final void setRepairTimeCDFInitialRandomSource(RVariableIfc distribution) {
        if (distribution == null) {
            throw new IllegalArgumentException("The repair time distribution was null!");
        }

        myRepairTimeRV.setInitialRandomSource(distribution);
    }

    /** True indicates that the component will automatically be scheduled to start operating
     *  when it is initialized
     *
     * @return Returns the operateAtInitializationFlag.
     */
    public final boolean isOperateAtInitializationFlagOn() {
        return myOperateAtInitializationFlag;
    }

    /** True indicates that the component will automatically be scheduled to start operating
     *  when it is initialized.  Changing the flag, only has an effect at initialization.
     *
     * @param flag The operateAtInitializationFlag to set.
     */
    public final void setOperateAtInitializationFlag(boolean flag) {
        myOperateAtInitializationFlag = flag;
    }

    /** Indicates whether or not the component will automatically restart operating
     *  after completing an operation
     *
     * @return Returns the automaticRestartAfterOperatingFlag.
     */
    public final boolean isAutomaticRestartAfterOperatingFlagOn() {
        return myAutomaticRestartAfterOperatingFlag;
    }

    /** Indicates whether or not the component will automatically restart operating
     *  after completing an operation.  The default is false. This behavior can
     *  also be overridden by overriding endOperationAction()
     *
     * @param flag The automaticRestartAfterOperatingFlag to set.
     */
    public final void setAutomaticRestartAfterOperatingFlag(boolean flag) {
        myAutomaticRestartAfterOperatingFlag = flag;
    }

    /** Indicates whether or not the component will automatically start operating
     *  after completing repair.  The default is false
     *
     * @return Returns the automaticRestartAfterRepairFlag.
     */
    public final boolean isAutomaticRestartAfterRepairFlagOn() {
        return myAutomaticRestartAfterRepairFlag;
    }

    /** Indicates whether or not the component will automatically start operating
     *  after completing repair.  The default is false. This behavior can
     *  also be overridden by overriding endRepairAction()
     *
     * @param flag The automaticRestartAfterRepairFlag to set.
     */
    public final void setAutomaticRestartAfterRepairFlag(boolean flag) {
        myAutomaticRestartAfterRepairFlag = flag;
    }

    /** Indicates whether or not the component will automatically start repair
     *  after failing.  The default is false
     * @return Returns the automaticStartRepairAfterFailureFlag.
     */
    public final boolean isAutomaticStartRepairAfterFailureFlagOn() {
        return myAutomaticStartRepairAfterFailureFlag;
    }

    /** Indicates whether or not the component will automatically start repair
     *  after failing.  The default is false. This behavior can
     *  also be overridden by overriding failureAction()
     *
     * @param flag The automaticStartRepairAfterFailureFlag to set.
     */
    public final void setAutomaticStartRepairAfterFailureFlag(boolean flag) {
        myAutomaticStartRepairAfterFailureFlag = flag;
    }

    /** If the component belongs to a composite (assembly) then this
     *  method will return the assembly.  This may be null.
     * @return Returns the assembly, may be null
     */
    public final ComponentAssembly getAssembly() {
        return myAssembly;
    }

    /** This method is used by ComponentAssembly to set the component's assembly.
     *  The assembly may be null if the component is removed from a ComponentAssembly
     *  as is thus not part of an assembly.
     *
     * @param assembly The assembly to set.
     */
    protected final void setAssembly(ComponentAssembly assembly) {
        myAssembly = assembly;
    }

    /** Tells the component to immediately start operating. The component
     *  cannot operate unless it has been activated.
     */
    public final void startOperation() {
        myState.startOperation();
    }

    /** Tells the component to immediately start operating. The component
     *  cannot operate unless it has been activated.  The supplied operation
     *  time is used to determine whether or not a failure will occur during
     *  the operation time. No end operation is scheduled.  The client is still
     *  responsible for ending the operation; however, the component may experience
     *  a failure before the specified operation time is completed.
     *
     * @param operationTime
     */
    public final void startOperation(double operationTime) {
        myState.startOperation(operationTime);
    }

    /** Schedules the component to operate using the operating time distribution
     *
     */
    public final void scheduleOperation() {
        scheduleOperation(getNextOperationTime());
    }

    /** Schedules the component to operate for the supplied time period
     *
     * @param operationTime, must be &gt; 0.0
     */
    public final void scheduleOperation(double operationTime) {
        if (operationTime <= 0.0) {
            throw new IllegalArgumentException("The operating time must be > 0.0");
        }

        myState.scheduleOperation(operationTime);
    }

    /** Gets the current operation time for the component. If the component is in
     *  the operating state and an operation has been scheduled, this method gets
     *  the time interval length of the operation.  If no operation has been scheduled or if
     *  the component is not in the operating state this method returns Double.NaN
     *
     * @return Returns the operationTime.
     */
    public final double getOperationTime() {
        return myOperationTime;
    }

    /** Can be used to directly end an on-going operation.  This method
     *  is only valid when the component is in the operating state
     */
    public final void endOperation() {
        myState.endOperation();
    }

    /** Tells the component to immediately start repair.  The component
     *  must be in the failed state.
     */
    public final void startRepair() {
        myState.startRepair();
    }

    /** Schedules the component to undergo repair for the time specified
     *  by its repair time distribution
     *
     */
    public final void scheduleRepair() {
        scheduleRepair(getNextRepairTime());
    }

    /** Schedules the component to undergo repair for the supplied time period
     *
     * @param repairTime, must be &gt; 0.0
     */
    public final void scheduleRepair(double repairTime) {
        if (repairTime <= 0.0) {
            throw new IllegalArgumentException("The repair time must be > 0.0");
        }
        myState.scheduleRepair(repairTime);
    }

    /** Can be used to directly end an on-going repair.  This method
     *  is only valid when the component is in the repairing state
     */
    public final void endRepair() {
        myState.endRepair();
    }

    /** Tells the component to activate
     */
    public final void activate() {
        myState.activate();
    }

    /** Tells the component to deactivate.  It cannot deactivate unless
     *  it is in the activate state.
     *
     */
    public final void deactivate() {
        myState.deactivate();
    }

    /** Tells the component to immediately fail.  The component must be in the
     *  operating state for it to fail.
     *
     */
    public final void fail() {
        myState.fail();
    }

    //  ===========================================
    //      Protected Methods
    //  ===========================================
    /** Returns the time remaining until the next failure.  This
     *  method is called whenever an operation is scheduled to
     *  determine if a failure should be scheduled
     *
     * @return
     */
    protected double getTimeRemainingUntilNextFailure() {
        if (myRepairedSinceLastOperatedFlag) {
            myTimeToFailure = getNextTimeToFailure();
            myRepairedSinceLastOperatedFlag = false;
        }
        return (myTimeToFailure);
    }

    /** After an operation ends this method is called to allow
     *  the time to failure to be updated.  The default is to decrement
     *  the time to failure by the amount of time that the component
     *  just operated.
     *
     * @param operationTime
     */
    protected void updateTimeRemainingUntilNextFailure(double operationTime) {
        myTimeToFailure = myTimeToFailure - operationTime;
    }

    /** Gets the next operation time for the component.
     *  By default this is determined by the operation time
     *  random variable, but can be overridden
     *
     * @return the next operation time
     */
    protected double getNextOperationTime() {
        return (myOperationTimeRV.getValue());
    }

    /** Gets the next time to failure for the component.
     *  By default this is determined by the time to failure
     *  random variable, but can be overridden
     *
     * @return the next time to failure
     */
    protected double getNextTimeToFailure() {
        return (myTimeToFailureRV.getValue());
    }

    /** Gets the next time to complete repair for the component.
     *  By default this is determined by the repair time
     *  random variable, but can be overridden
     *
     * @return the next repair time
     */
    protected double getNextRepairTime() {
        return (myRepairTimeRV.getValue());
    }

    /** This method is called right after the component
     *  enters the activated state, i.e. transitioning from
     *  operating to activated.   The component should be in the
     *  activated state within this method. If the automatic restart
     *  after operating flag is true, the component will be scheduled
     *  to operate again automatically. Overriders of this method are
     *  responsible for automatic restart after operating if that
     *  behavior is still required
     *
     */
    protected void endOperationAction() {

        if (myAutomaticRestartAfterOperatingFlag == true) {
            scheduleOperation();
        }

    }

    /** This method is called right after the component enters
     *  the failed state, i.e. transitioning from operating to failed. The
     *  component will be in the failed state within this method.
     *  If the automatic start
     *  start repair after failure flag is true, the component will be scheduled
     *  into repair automatically. Overriders of this method are responsible for
     *  handling automatic start of repair if that behavior is needed.
     */
    protected void failureAction() {

        if (myAutomaticStartRepairAfterFailureFlag == true) {
            scheduleRepair();
        }

    }

    /** This method is called right after the component enters
     *  the activated state after being repaired, i.e. transitioning from repairing to activated
     *  The component will be in the activated state within this method.
     *  If the automatic restart
     *  after repair flag is true, the component will be scheduled
     *  to operate again automatically. Overriders of this method are responsible for
     *  handling automatic restart after repair if that behavior is needed.
     */
    protected void endRepairAction() {

        if (myAutomaticRestartAfterRepairFlag == true) {
            scheduleOperation();
        }

    }

    /** Required initialization actions occur in this method.  The state
     *  of the component is automatically set to the available state and
     *  the component is considered repaired since last operated.
     */
    protected void beforeReplication() {
        myRepairedSinceLastOperatedFlag = true;
        myRepairTime = Double.NaN;
        myOperationTime = Double.NaN;
        myFailureEvent = null;
        myEndOperationEvent = null;
        myEndRepairEvent = null;
        setState(myAvailableState);
    }

    /** Can be used to initialize the component after beforeReplication()
     *  is called.  If the operate at initialization flag is true then
     *  the component is scheduled to operate according to the operation time model
     *
     */
    protected void initialize() {
        if (myOperateAtInitializationFlag == true) {
            scheduleOperation();
        }
    }

    /** Sets the state of the component, updates previous and current states,
     *  notifies state change listeners of the state change, and notifies
     *  any observers of the state change.
     *
     * @param state
     */
    protected final void setState(ComponentState state) {
        myState.exit();
        myPreviousState = myState;
        myState = state;
        myState.enter();
        System.out.println();
        System.out.println("Component: t = " + getTime() + " > Transitioning from state " + myPreviousState + " to state " + myState);
        System.out.println();
        notifyStateChangeListeners();
        notifyObservers(STATE_CHANGE);
    }

    //  ===========================================
    //      Private Methods
    //  ===========================================
    /** Used internally to notify any state change listeners of a state change
     */
    private final void notifyStateChangeListeners() {

        if (myAssembly != null) {
            myAssembly.stateChange(this);
        }

        for (ComponentStateChangeListenerIfc listener : myStateChangeListeners) {
            listener.stateChange(this);
        }
    }

    //  ===========================================
    //      Inner Classes
    //  ===========================================
    protected class ComponentState extends ModelElementState {

        public ComponentState() {
            this(null, false);
        }

        public ComponentState(String name) {
            this(name, false);
        }

        public ComponentState(boolean useStatistic) {
            this(null, useStatistic);
        }

        public ComponentState(String name, boolean useStatistic) {
            super(Component.this, name, useStatistic);
        }

        protected void activate() {
            throw new IllegalStateException("Tried to turn on the component from an illegal state: " + getName());
        }

        protected void deactivate() {
            throw new IllegalStateException("Tried to turn off the component from an illegal state: " + getName());
        }

        protected void startOperation() {
            throw new IllegalStateException("Tried to start the component from an illegal state: " + getName());
        }

        protected void startOperation(double operationTime) {
            throw new IllegalStateException("Tried to start the component from an illegal state: " + getName());
        }

        protected void scheduleOperation(double time) {
            throw new IllegalStateException("Tried to start the component from an illegal state: " + getName());
        }

        protected void endOperation() {
            throw new IllegalStateException("Tried to end the operation from an illegal state: " + getName());
        }

        protected void startRepair() {
            throw new IllegalStateException("Tried to start the component from an illegal state: " + getName());
        }

        protected void scheduleRepair(double time) {
            throw new IllegalStateException("Tried to start repair on the component from an illegal state: " + getName());
        }

        protected void endRepair() {
            throw new IllegalStateException("Tried to end the repair from an illegal state: " + getName());
        }

        protected void fail() {
            throw new IllegalStateException("Tried to fail the component from an illegal state: " + getName());
        }

        protected void scheduleFailure(double time) {
            throw new IllegalStateException("Tried to fail the component from an illegal state: " + getName());
        }
    }

    protected class Created extends ComponentState {

        public Created() {
            this(null, false);
        }

        public Created(String name) {
            this(name, false);
        }

        public Created(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void activate() {
            setState(myAvailableState);
        }
    }

    protected class Unavailable extends ComponentState {

        public Unavailable() {
            this(null, false);
        }

        public Unavailable(String name) {
            this(name, false);
        }

        public Unavailable(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void activate() {
            setState(myAvailableState);
        }
    }

    protected class Available extends ComponentState {

        public Available() {
            this(null, false);
        }

        public Available(String name) {
            this(name, false);
        }

        public Available(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void deactivate() {
            setState(myUnavailableState);
        }

        protected void startOperation() {
            // immediately start operation, end of operation is responsibility of client
            // failing is responsibility of client
            myOperationTime = Double.NaN;
            setState(myOperatingState);
        }

        protected void startOperation(double operationTime) {

            double ttf = getTimeRemainingUntilNextFailure();
            if (ttf <= operationTime) { // failure will occur first, schedule it
                myOperationTime = ttf;
                myFailureEvent = scheduleEvent(myFailureListener, ttf);
                myEndOperationEvent = null;
            } else { // client controls end of operation
                myOperationTime = operationTime;
                myEndOperationEvent = null;
                myFailureEvent = null;
            }
            setState(myOperatingState);
        }

        protected void scheduleOperation(double operationTime) {

            double ttf = getTimeRemainingUntilNextFailure();
            if (ttf <= operationTime) { // failure will occur first, schedule it
                myOperationTime = ttf;
                myFailureEvent = scheduleEvent(myFailureListener, ttf);
                myEndOperationEvent = null;
            } else { // failure will occur after current operation
                myOperationTime = operationTime;
                myEndOperationEvent = scheduleEvent(myEndOperationListener, operationTime);
                myFailureEvent = null;
            }
            setState(myOperatingState);
        }
    }

    protected class Operating extends ComponentState {

        public Operating() {
            this(null, false);
        }

        public Operating(String name) {
            this(name, false);
        }

        public Operating(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void endOperation() {
            // get the time spent operating
            double operationTime = getTime() - getTimeStateEntered();
            updateTimeRemainingUntilNextFailure(operationTime);
            if ((myEndOperationEvent != null) || (myFailureEvent != null)) {
                // end operation event or failure event had been scheduled
                if (myEndOperationEvent != null) {// end operation event
                    if (myEndOperationEvent.isScheduled()) {
                        // user is telling component to directly end the operation
                        // even though an end operation event is pending
                        // cancel the event
                        cancelEvent(myEndOperationEvent);
                    }
                }

                if (myFailureEvent != null) {// failure event
                    if (myFailureEvent.isScheduled()) {
                        // user is telling component to directly end the operation
                        // even though a failure event is pending
                        // cancel the event
                        cancelEvent(myFailureEvent);
                    }
                }
            }
            myEndOperationEvent = null;
            myFailureEvent = null;
            myOperationTime = Double.NaN;
            setState(myAvailableState);
            endOperationAction();
        }

        protected void fail() {
            if ((myEndOperationEvent != null) || (myFailureEvent != null)) {
                // end operation event or failure event had been scheduled
                if (myEndOperationEvent != null) {// end operation event
                    if (myEndOperationEvent.isScheduled()) {
                        // user is telling component to directly fail
                        // even though an end operation event is pending
                        // cancel the event
                        cancelEvent(myEndOperationEvent);
                    }
                }

                if (myFailureEvent != null) {// failure event
                    if (myFailureEvent.isScheduled()) {
                        // user is telling component to directly fail
                        // even though a failure event is pending
                        // cancel the event
                        cancelEvent(myFailureEvent);
                    }
                }
            }
            myEndOperationEvent = null;
            myFailureEvent = null;
            myOperationTime = Double.NaN;
            myTimeToFailure = 0.0;
            setState(myFailedState);
            failureAction();
        }
    }

    protected class Repairing extends ComponentState {

        public Repairing() {
            this(null, false);
        }

        public Repairing(String name) {
            this(name, false);
        }

        public Repairing(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void endRepair() {
            if (myEndRepairEvent != null) {
                // repair event had been scheduled
                // check if it is still in the event calendar
                if (myEndRepairEvent.isScheduled()) {
                    // user is telling component to be repaired
                    // even though an end repair event is pending
                    // cancel the event
                    cancelEvent(myEndRepairEvent);
                }
            }
            myEndRepairEvent = null;
            myRepairTime = Double.NaN;
            myRepairedSinceLastOperatedFlag = true;
            setState(myAvailableState);
            endRepairAction();
        }
    }

    protected class Failed extends ComponentState {

        public Failed() {
            this(null, false);
        }

        public Failed(String name) {
            this(name, false);
        }

        public Failed(String name, boolean useStatistic) {
            super(name, useStatistic);
        }

        protected void startRepair() {
            setState(myRepairingState);
        }

        protected void scheduleRepair(double time) {
            myRepairTime = time;
            myEndRepairEvent = scheduleEvent(myEndRepairListener, time);
            setState(myRepairingState);
        }
    }

    private class EndOperationListener extends EventAction {

        public void action(JSLEvent evt) {
            myState.endOperation();
        }
    }

    private class FailureListener extends EventAction {

        public void action(JSLEvent evt) {
            myState.fail();
        }
    }

    private class EndRepairListener extends EventAction {

        public void action(JSLEvent evt) {
            myState.endRepair();
        }
    }
}
