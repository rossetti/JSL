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
package jsl.modeling.elements.spatial.transporter;

import java.util.HashSet;

import java.util.Set;

import jsl.simulation.IllegalStateException;
import jsl.simulation.ModelElement;
import jsl.simulation.State;
import jsl.modeling.elements.spatial.AbstractMover;
import jsl.modeling.elements.spatial.CoordinateIfc;
import jsl.modeling.elements.spatial.Path;
import jsl.modeling.elements.spatial.PathFinder;
import jsl.modeling.elements.spatial.SpatialModel;
import jsl.modeling.elements.spatial.Vector3D;

/**
 *
 */
public class Transporter extends AbstractMover {

    /**
     * Indicates that the transporter has changed state to its observers
     */
    public static final int STATE_CHANGE = ModelElement.getNextEnumConstant();

    /** The list of transporter sets currently holding this transporter
     */
    protected Set<TransporterSet> myTransporterSets;

    /** A variable that can be used to hold the path once found
     */
    protected Path myCurrentPath;

    /** An object that knows how to find the path for this transporter
     */
    protected PathFinder myPathFinder;

    /** A reference to the current state of the iterative process
     */
    protected TransporterState myState;

    /** The previous state for the transporter
     */
    protected TransporterState myPreviousState;

    /** The created state is used when the transporter is first created.
     *  Can transition to inactive state or idle state
     */
    protected Created myCreatedState = new Created();

    /** Represents the state of being active but not busy
     *  Can become inactive, moving idle, or allocated
     */
    protected Idle myIdleState = new Idle();

    /** Inactive means that it is unavailable.  Can only transition
     *  to active state
     */
    protected Inactive myInactiveState = new Inactive();

    /** Represents the state of being allocated to do a transport
     *  Busy, but not yet moving
     */
    protected Allocated myAllocatedState = new Allocated();

    /** Not-busy but moving
     */
    protected MovingIdle myMovingIdleState = new MovingIdle();

    /** Busy (allocated) but moving empty
     */
    protected AllocatedMovingEmpty myAllocatedMovingEmptyState = new AllocatedMovingEmpty();

    /** Allocated (busy) and moving loaded
     */
    protected AllocatedMovingLoaded myAllocatedMovingLoadedState = new AllocatedMovingLoaded();

    /** Can be supplied to give logic for the transporter when
     *  an idle move is completed
     */
    protected IdleMoveCompletionIfc myIdleMoveCompletionListener;

    /** Can be supplied to give logic for the transporter for when
     *  an empty move is completed.
     */
    protected EmptyMoveCompletionIfc myEmptyMoveCompletionListener;

    /** Can be supplied to provide logic for the transporter for when
     *  a loaded move is completed
     */
    protected TransportCompletionIfc myLoadedMoveCompletionListener;

    /** Can be used by clients to collect the time spent loading
     */
    protected State myLoadingState;

    /** Can be used by clients to collect the time spent unloading
     */
    protected State myUnloadingState;

    /** Creates a Transporter2D with (0.0, 0.0) position.
     *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     */
    public Transporter(ModelElement parent) {
        this(parent, null, null, 0.0, 0.0);
    }

    /** Creates a Transporter2D with (0.0, 0.0) position.
     *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param name the name of the transporter
     */
    public Transporter(ModelElement parent, String name) {
        this(parent, name, null, 0.0, 0.0);
    }

    /** Creates a Transporter2D with the given  (x,y) position.
     *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param x the initial x position
     * @param y the initial y position
     */
    public Transporter(ModelElement parent, double x, double y) {
        this(parent, null, null, x, y);
    }

    /** Creates a Transporter2D with at the coordinates of the supplied position.
     *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param position the initial position
     */
    public Transporter(ModelElement parent, CoordinateIfc position) {
        this(parent, null, null, position);
    }

    /** Creates a Transporter2D with the given parent and SpatialModel2D.  The default position
     *  is (0.0, 0.0).  If the SpatialModel2D
     *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param name the name of the transporter
     * @param spatialModel the spatial model
     */
    public Transporter(ModelElement parent, String name, SpatialModel spatialModel) {
        this(parent, name, spatialModel, 0.0, 0.0);
    }

    /** Creates a Transporter2D with the given parent and SpatialModel2D.  The default position
     *  is (0.0, 0.0).  If the SpatialModel2D
     *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent 
     * @param spatialModel the spatial model
     */
    public Transporter(ModelElement parent, SpatialModel spatialModel) {
        this(parent, null, spatialModel, 0.0, 0.0, 0.0);
    }

    /** Creates a Transporter2D with the given parent and SpatialModel2D.  If the SpatialModel2D
     *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param name the name of the transporter
     * @param spatialModel the spatial model
     * @param x the initial x position
     * @param y the initial y position
     */
    public Transporter(ModelElement parent, String name, SpatialModel spatialModel, double x, double y) {
        this(parent, null, null, new Vector3D(x, y, 0.0));
    }

    /** Creates a Transporter2D with the given parent and SpatialModel2D.  If the SpatialModel2D
     *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param name the name of the transporter
     * @param spatialModel the spatial model
     * @param x the initial x position
     * @param y the initial y position
     * @param z the initial z position
     */
    public Transporter(ModelElement parent, String name, SpatialModel spatialModel, double x, double y, double z) {
        this(parent, null, null, new Vector3D(x, y, z));
    }

    /** Creates a Transporter2D with the given parent and SpatialModel2D.  If the SpatialModel2D
     *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
     *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent the parent
     * @param name the name of the transporter
     * @param spatialModel the spatial model
     * @param coordinate the initial coordinate
     */
    public Transporter(ModelElement parent, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, name, spatialModel, coordinate);

        myTransporterSets = new HashSet<TransporterSet>();
        myState = myCreatedState;
        myState.enter(0.0);
    }

    public final boolean isCreated() {
        return (myState == myCreatedState);
    }

    public final boolean isIdle() {
        return (myState == myIdleState);
    }

    public final boolean isBusy() {

        if (isAllocated()) {
            return (true);
        }

        if (isAllocatedMovingEmpty()) {
            return (true);
        }

        if (isAllocatedMovingLoaded()) {
            return (true);
        }

        return (false);

    }

    public final boolean isInactive() {
        return (myState == myInactiveState);
    }

    public final boolean isAllocated() {
        return (myState == myAllocatedState);
    }

    public final boolean isAllocatedMovingEmpty() {
        return (myState == myAllocatedMovingEmptyState);
    }

    public final boolean isAllocatedMovingLoaded() {
        return (myState == myAllocatedMovingLoadedState);
    }

    public final boolean isMovingIdle() {
        return (myState == myMovingIdleState);
    }

    public void activate() {
        myState.activate();
    }

    public void inactivate() {
        myState.inactivate();
    }

    public void allocate() {
        myState.allocate();
    }

    public void moveIdle(CoordinateIfc destination, IdleMoveCompletionIfc idleMoveCompletionListener) {
        myState.moveIdle(destination, idleMoveCompletionListener);
    }

    public void moveEmpty(CoordinateIfc destination, EmptyMoveCompletionIfc emptyMoveCompletionListener) {
        myState.moveEmpty(destination, emptyMoveCompletionListener);
    }

    public void transport(CoordinateIfc destination, TransportCompletionIfc loadedMoveCompletionListener) {
        myState.transport(destination, loadedMoveCompletionListener);
    }

    public void free() {
        myState.free();
    }

    /** Allows the client to indicate that the transporter is loading
     *  It is up to the client to also indicate that loading has ended
     *
     *
     */
    public final void beginLoading() {
        if (myLoadingState == null) {
            myLoadingState = new State("Loading");
        }
        myLoadingState.enter(getTime());
    }

    /** Allows the client to indicate that the transporter is done loading.
     *  This has no tie to the underlying state transition for the transporter
     */
    public final void endLoading() {
        if (myLoadingState == null) {
            return;
        }

        myLoadingState.exit(getTime());
    }

    /** Checks if a client has indicated that the transporter is loading
     *
     * @return true if loading
     */
    public final boolean isLoading() {
        return myLoadingState.isEntered();
    }

    /** Allows the client to indicate that the transporter is unloading
     *  It is up to the client to also indicate that unloading has ended
     *  This has no tie to the underlying state transition for the transporter
     *
     */
    public final void beginUnloading() {
        if (myUnloadingState == null) {
            myUnloadingState = new State("UnLoading");
        }
        myUnloadingState.enter(getTime());
    }

    /** Allows the client to indicate that the transporter is done unloading.
     *  This has no tie to the underlying state transition for the transporter
     */
    public final void endUnloading() {
        if (myUnloadingState == null) {
            return;
        }

        myUnloadingState.exit(getTime());
    }

    /** Checks if a client has indicated that the transporter is unloading
     *
     * @return true if unloading
     */
    public final boolean isUnloading() {
        return myUnloadingState.isEntered();
    }

    @Override
    protected void initialize() {
        super.initialize();
        myCreatedState.initialize();
        myIdleState.initialize();
        myInactiveState.initialize();
        myMovingIdleState.initialize();
        myAllocatedState.initialize();
        myAllocatedMovingEmptyState.initialize();
        myAllocatedMovingLoadedState.initialize();

        // the transporter could be in any state
        // the default initial state should be idle
        // need to put it back in the idle state

        setState(myCreatedState);
        myState.activate();
    }

    @Override
    protected void warmUp() {
        super.warmUp();
        myCreatedState.initialize();
        myIdleState.initialize();
        myInactiveState.initialize();
        myMovingIdleState.initialize();
        myAllocatedState.initialize();
        myAllocatedMovingEmptyState.initialize();
        myAllocatedMovingLoadedState.initialize();
        setState(myState);
    }

    protected final void setState(TransporterState state) {
        myState.exit(getTime());
        myPreviousState = myState;
        myState = state;
        myState.enter(getTime());
        notifyObservers(STATE_CHANGE);
    }

    @Override
    protected void afterTripEnds() {
        // a trip has ended either from a move or a transport
        // need to set the state accordingly
        if (isMovingIdle()) {
            myState.idleMoveComplete();
        } else if (isAllocatedMovingEmpty()) {
            myState.emptyMoveComplete();
        } else if (isAllocatedMovingLoaded()) {
            myState.loadedMoveComplete();
        }
    }

    protected final boolean addTransporterSet(TransporterSet set) {
        return (myTransporterSets.add(set));
    }

    protected final boolean removeTransporterSet(TransporterSet set) {
        return (myTransporterSets.remove(set));
    }

    protected final void notifyTransporterSetsOfIdleness() {

        for (TransporterSet ts : myTransporterSets) {
            ts.addIdleTransporter(this);
        }
    }

    protected final void notifyTransporterSetsOfNonIdleness() {

        for (TransporterSet ts : myTransporterSets) {
            ts.removeIdleTransporter(this);
        }
    }

    protected class TransporterState extends State {

        protected TransporterState(String name) {
            super(name);
        }

        protected void activate() {
            throw new IllegalStateException("Tried to activate from an illegal state: " + myName);
        }

        protected void inactivate() {
            throw new IllegalStateException("Tried to inactivate from an illegal state: " + myName);
        }

        protected void allocate() {
            throw new IllegalStateException("Tried to allocate from an illegal state: " + myName);
        }

        protected void moveIdle(CoordinateIfc destination, IdleMoveCompletionIfc idleMoveCompletionListener) {
            throw new IllegalStateException("Tried to move from an illegal state: " + myName);
        }

        protected void moveEmpty(CoordinateIfc destination, EmptyMoveCompletionIfc emptyMoveCompletionListener) {
            throw new IllegalStateException("Tried to move from an illegal state: " + myName);
        }

        protected void transport(CoordinateIfc destination, TransportCompletionIfc loadedMoveCompletionListener) {
            throw new IllegalStateException("Tried to transport from an illegal state: " + myName);
        }

        protected void free() {
            throw new IllegalStateException("Tried to free from an illegal state: " + myName);
        }

        protected void idleMoveComplete() {
            throw new IllegalStateException("Tried to idleMoveComplete from an illegal state: " + myName);
        }

        protected void emptyMoveComplete() {
            throw new IllegalStateException("Tried to emptyMoveComplete from an illegal state: " + myName);
        }

        protected void loadedMoveComplete() {
            throw new IllegalStateException("Tried to loadedMoveComplete from an illegal state: " + myName);
        }

        @Override
        public final String toString() {
            return (myName);
        }
    }

    protected class Created extends TransporterState {

        protected Created() {
            super("Created");
        }

        @Override
        protected final void inactivate() {
            notifyTransporterSetsOfNonIdleness();
            setState(myInactiveState);
        }

        @Override
        protected final void activate() {
            setState(myIdleState);
            notifyTransporterSetsOfIdleness();
        }
    }

    protected class Inactive extends TransporterState {

        protected Inactive() {
            super("Inactive");
        }

        @Override
        protected final void activate() {
            setState(myIdleState);
            notifyTransporterSetsOfIdleness();
        }
    }

    protected class Idle extends TransporterState {

        protected Idle() {
            super("Idle");
        }

        @Override
        protected final void inactivate() {
            notifyTransporterSetsOfNonIdleness();
            setState(myInactiveState);
        }

        @Override
        protected final void moveIdle(CoordinateIfc destination, IdleMoveCompletionIfc idleMoveCompletionListener) {
            myIdleMoveCompletionListener = idleMoveCompletionListener;
            notifyTransporterSetsOfNonIdleness();
            setState(myMovingIdleState);
            moveTo(destination);
        }

        @Override
        protected final void allocate() {
            notifyTransporterSetsOfNonIdleness();
            setState(myAllocatedState);
        }
    }

    protected class MovingIdle extends TransporterState {

        protected MovingIdle() {
            super("MovingIdle");
        }

        @Override
        protected void idleMoveComplete() {
            setState(myIdleState);
            notifyTransporterSetsOfIdleness();
            if (myIdleMoveCompletionListener != null) {
                myIdleMoveCompletionListener.idleMoveComplete(Transporter.this);
            }
        }
    }

    protected class Allocated extends TransporterState {

        public Allocated() {
            super("Allocated");
        }

        @Override
        protected final void free() {
            setState(myIdleState);
            notifyTransporterSetsOfIdleness();
        }

        @Override
        protected final void moveEmpty(CoordinateIfc destination, EmptyMoveCompletionIfc emptyMoveCompletionListener) {
            myEmptyMoveCompletionListener = emptyMoveCompletionListener;
            setState(myAllocatedMovingEmptyState);
            moveTo(destination);
        }

        @Override
        protected final void transport(CoordinateIfc destination, TransportCompletionIfc loadedMoveCompletionListener) {
            myLoadedMoveCompletionListener = loadedMoveCompletionListener;
            setState(myAllocatedMovingLoadedState);
            moveTo(destination);
        }
    }

    protected class AllocatedMovingEmpty extends TransporterState {

        protected AllocatedMovingEmpty() {
            super("AllocatedMovingEmpty");
        }

        @Override
        protected void emptyMoveComplete() {
            setState(myAllocatedState);
            if (myEmptyMoveCompletionListener != null) {
                myEmptyMoveCompletionListener.emptyMoveComplete(Transporter.this);
            }
        }
    }

    protected class AllocatedMovingLoaded extends TransporterState {

        public AllocatedMovingLoaded() {
            super("AllocatedMovingLoaded");
        }

        @Override
        protected void loadedMoveComplete() {
            setState(myAllocatedState);
            if (myLoadedMoveCompletionListener != null) {
                myLoadedMoveCompletionListener.transportComplete(Transporter.this);
            }
        }
    }
}
