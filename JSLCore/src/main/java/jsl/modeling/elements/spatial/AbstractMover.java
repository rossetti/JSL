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
package jsl.modeling.elements.spatial;

import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.statistic.WeightedStatisticIfc;

/**
 * An AbstractMover implements the MoverIfc and is a model element
 * (SpatialModelElement). It represents an object that can move through a
 * spatial model. It can begin a trip and make movements towards the destination
 * associated with the trip. Each trip can be broken up into a series of
 * movements. Each movement can have a different length and velocity as long as
 * the object eventually completes the trip.
 *
 * It has default behaviors that can be modified by some helper classes.
 *
 * A MovementControllerIfc can be supplied to control the movement within a
 * trip. If a movement controller is supplied, it is responsible for setting the
 * velocity associated with movements. A CollisionDetectorIfc can be supplied to
 * detect collisions while moving. A CollisionHandlerIfc can be supplied to
 * handle (react) to collisions detected while moving A
 * OutsideSpatialModelHandlerIfc can be supplied to handle the situation if the
 * the object attempts to go outside the valid frame of reference associated
 * with its spatial model.
 *
 */
abstract public class AbstractMover extends SpatialModelElement implements MoverIfc, VelocityIfc {

    /**
     * Indicates that a AbstractMover started a trip
     */
    public static final int TRIP_STARTED = Model.getNextEnumConstant();

    /**
     * Indicates that a AbstractMover ended a trip
     */
    public static final int TRIP_ENDED = Model.getNextEnumConstant();

    /**
     * Indicates that a AbstractMover had its trip canceled
     */
    public static final int TRIP_CANCELED = Model.getNextEnumConstant();

    /**
     * Indicates that a AbstractMover started a movement
     */
    public static final int MOVE_STARTED = Model.getNextEnumConstant();

    /**
     * Indicates that a AbstractMover ended a movement
     */
    public static final int MOVE_ENDED = Model.getNextEnumConstant();

    /**
     * Controls the movement of this element
     */
    protected MovementControllerIfc myMovementController;

    /**
     * Handles the occurs of collisions
     */
    protected CollisionHandlerIfc myCollisionHandler;

    /**
     * Detects the occurrence of collisions
     */
    protected CollisionDetectorIfc myCollisionDetector;

    /**
     * Detects the occurrence of collisions
     */
    protected OutsideSpatialModelHandlerIfc myOSMHandler;

    /**
     * Holds the spatial element of the destination for the current trip
     * (movement) if given
     */
    protected SpatialElementIfc mySEDestination;

    /**
     * A temporary vector to hold the position.
     */
    private Vector3D myPosition;

    /**
     * Holds the x,y coordinates of the destination for the current trip
     * (movement)
     */
    private Vector3D myDestination;

    /**
     * Holds the x,y coordinates of the end position associated with the current
     * movement
     */
    private Vector3D myNextPosition;

    /**
     * This Vector2D is used to hold the x and y components of the current
     * direction of travel
     */
    private Vector3D myDirection;

    /**
     * Used to indicate the position of the moving element at a projected time t
     */
    private Vector3D myFuturePosition;

    /**
     * Represents the inherent velocity of the mover. Used to set the movement
     * velocity if no movement controller is present
     */
    private RandomVariable myVelocity;

    /**
     * Represents the velocity (speed) in the current direction of travel for
     * the current movement
     */
    private double myMovementVelocity = 1.0;

    /**
     * Represents the distance in the current direction of travel for the
     * current movement
     */
    private double myMovementDistance;

    /**
     * The time that the current movement started
     */
    private double myMovementStartTime;

    /**
     * The length of time expected for the current movement
     */
    private double myMovementTime;

    /**
     * The event that represents the currently scheduled movement
     */
    private JSLEvent<Object> myCurrentMovementEvent;

    /**
     * ActionListener that handles the end of movement event
     */
    private EndMovementAction myEndMovementAction;

    /**
     * A flag that indicates that the object is moving
     */
    private boolean myMovingFlag = false;

    /**
     * A flag that indicates that the object is on a trip
     */
    private boolean myTripFlag = false;

    /**
     * A flag to indicate to automatically call startNextTrip() Default to false
     */
    private boolean myDefaultNextTripFlag = false;

    /**
     * Used and reused to hold information about the next collision
     */
    private Collision myCollision;

    /**
     * Used to count the number of trips completed by the mover
     */
    protected Counter myTripCounter;

    /**
     * Used to record the distance of each trip made
     */
    protected ResponseVariable myTripDist;

    /**
     * Used to record the total distance of all trips
     */
    protected ResponseVariable myTotalTripDist;

    /**
     * Controls whether or not trip statistics are automatically collected By
     * default the option is false
     */
    private boolean myTripStatOption;

    /**
     * Used to count the number of trips
     */
    private int myNumTrips;

    /**
     * Used to stop replication when this number of trips is reached
     */
    private int myTripLimit;

    /**
     * The velocity addFactor, default is 1. If this is changed, it is changed for
     * all replications
     */
    private double myVelFactor;

    /**
     * Creates a AbstractMover with the default position within its spatial
     * model. The spatial model of the parent is used as the spatial model of
     * this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     */
    public AbstractMover(ModelElement parent) {
        this(parent, null, null, null);
    }

    /**
     * Creates a AbstractMover with the default position within its spatial
     * model. The spatial model of the parent is used as the spatial model of
     * this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     */
    public AbstractMover(ModelElement parent, String name) {
        this(parent, name, null, null);
    }

    /**
     * Creates a AbstractMover at the supplied coordinate within its spatial
     * model. The spatial model of the parent is used as the spatial model of
     * this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param coordinate
     */
    public AbstractMover(ModelElement parent, CoordinateIfc coordinate) {
        this(parent, null, null, coordinate);
    }

    /**
     * Creates a AbstractMover at the same coordinates as the supplied
     * SpatialElementIfc within its spatial model. The spatial model of the
     * parent is used as the spatial model of this object. If the parent does
     * not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param element
     */
    public AbstractMover(ModelElement parent, SpatialElementIfc element) {
        this(parent, null, null, element.getPosition());
    }

    /**
     * Creates a AbstractMover at the same coordinates as the supplied
     * SpatialElementIfc within its spatial model. The spatial model of the
     * parent is used as the spatial model of this object. If the parent does
     * not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param element
     * @param name
     */
    public AbstractMover(ModelElement parent, SpatialElementIfc element, String name) {
        this(parent, name, null, element.getPosition());
    }

    /**
     * Creates a AbstractMover with the default position within the given
     * spatial model. If the supplied spatial model is null the spatial model of
     * the parent is used as the spatial model of this object. If the parent
     * does not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     */
    public AbstractMover(ModelElement parent, String name, SpatialModel spatialModel) {
        this(parent, name, spatialModel, null);
    }

    /**
     * Creates a AbstractMover with the default position within the given
     * spatial model. If the supplied spatial model is null the spatial model of
     * the parent is used as the spatial model of this object. If the parent
     * does not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param spatialModel
     */
    public AbstractMover(ModelElement parent, SpatialModel spatialModel) {
        this(parent, null, spatialModel, null);
    }

    /**
     * Creates a AbstractMover at the supplied coordinates within the given
     * spatial model. If the supplied spatial model is null the spatial model of
     * the parent is used as the spatial model of this object. If the parent
     * does not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     * @param coordinate Must be valid for the spatial model
     */
    public AbstractMover(ModelElement parent, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, name, spatialModel, coordinate);
        myTripStatOption = false;
        myVelFactor = 1.0;
        myNumTrips = 0;
        myTripLimit = Integer.MAX_VALUE;
        myVelocity = new RandomVariable(this, ConstantRV.ONE);
        myPosition = new Vector3D();
        myDirection = new Vector3D();
        myDestination = new Vector3D();
        myNextPosition = new Vector3D();
        myFuturePosition = new Vector3D();
        myCollision = new Collision(this);
        myEndMovementAction = new EndMovementAction();
    }

    /**
     * Turns on trip statistical collection. Once it is on. It is on.
     */
    public void turnOnTripStatistics() {
        if (myTripStatOption == true) {
            return;
        }
        myTripStatOption = true;
        myTripCounter = new Counter(this, getName() + ":TripCount");
        myTripDist = new ResponseVariable(this, getName() + ":TripDist");
        myTotalTripDist = new ResponseVariable(this, getName() + ":TotTripDist");
    }

    /**
     *
     * @return true means on, false means off
     */
    public final boolean isTripStatOptionOn() {
        return myTripStatOption;
    }

    @Override
    protected void initialize() {
        super.initialize();
        myNumTrips = 0;
        myTripFlag = false;
        myMovingFlag = false;
        myDirection.setCoordinates(0.0, 0.0, 0.0);
        myDestination.setCoordinates(0.0, 0.0, 0.0);
        myFuturePosition.setCoordinates(0.0, 0.0, 0.0);
        myCollision.clear();
    }

    @Override
    protected void replicationEnded() {
        super.replicationEnded();
        if (isTripStatOptionOn()) {
            WeightedStatisticIfc stat = myTripDist.getWithinReplicationStatistic();
            myTotalTripDist.setValue(stat.getWeightedSum());
        }
    }

    /**
     * Sets the number of trips that will trigger the end of the replication
     *
     * @param maxTrips the desired limit
     */
    public final void setMaxNumTrips(int maxTrips) {
        if (maxTrips <= 0) {
            throw new IllegalArgumentException("Limit of trips must be > 0");
        }
        myTripLimit = maxTrips;
    }

    /**
     * Gets the number of trips that will trigger the end of the replication
     *
     * @return the limit
     */
    public long getMaxNumTrips() {
        return myTripLimit;
    }

    /**
     *
     * @return the number of trips completed by the mover
     */
    public double getNumTripsCompleted() {
        return myNumTrips;
    }

    /**
     * Controls whether or not to automatically start a new trip via
     * startNextTrip()
     *
     * @return true if the option is on
     */
    public final boolean isDefaultNextTripOptionOn() {
        return myDefaultNextTripFlag;
    }

    /**
     * Controls whether or not to automatically start a new trip via
     * startNextTrip()
     *
     * @param defaultNextTripFlag true means on
     */
    public final void setDefaultNextTripOption(boolean defaultNextTripFlag) {
        myDefaultNextTripFlag = defaultNextTripFlag;
    }

    /**
     * If the mover is on a trip then the movement along the trip is canceled
     * and the mover stays at the position where it is when this method is
     * called. This method cancels movement. If the mover is not moving or on a
     * trip nothing happens.
     */
    @Override
    public final void cancelTrip() {
        if (!isMoving() || !isOnTrip()) {
            return;
        }
        // moving or on a trip
        myCurrentMovementEvent.setCanceledFlag(true);
        myMovingFlag = false;
        myTripFlag = false;
        tripCanceled();
        notifyObservers(TRIP_CANCELED);
    }

    @Override
    public final CoordinateIfc getFuturePosition(double time) {
        if (time < 0.0) {
            throw new IllegalArgumentException("The time was less than zero.");
        }
        // clear the future position
        myFuturePosition.setCoordinates(0.0, 0.0, 0.0);
        // set the direction of travel, direction is a unit vector
        myFuturePosition.setCoordinates(myDirection);
        // set the distance, v*t, in the direction of travel
        myFuturePosition.multiply(getMovementVelocity() * time);
        // add the current position to get the future position
        CoordinateIfc c = getPosition();
        myFuturePosition.add(c);
        return (myFuturePosition);
    }

    @Override
    public final boolean isMoving() {
        return (myMovingFlag);
    }

    @Override
    public final boolean isOnTrip() {
        return (myTripFlag);
    }

    @Override
    public final double getMovementDistance() {
        return myMovementDistance;
    }

    @Override
    public final double getMovementTime() {
        return myMovementTime;
    }

    @Override
    public final double getMovementStartTime() {
        return myMovementStartTime;
    }

    @Override
    public final double getMovementVelocity() {
        return myMovementVelocity;
    }

    @Override
    public final CollisionDetectorIfc getCollisionDetector() {
        return myCollisionDetector;
    }

    @Override
    public final void setCollisionDetector(CollisionDetectorIfc collisionDetector) {
        myCollisionDetector = collisionDetector;
    }

    @Override
    public final CollisionHandlerIfc getCollisionHandler() {
        return myCollisionHandler;
    }

    @Override
    public final void setCollisionHandler(CollisionHandlerIfc collisionHandler) {
        myCollisionHandler = collisionHandler;
    }

    @Override
    public final MovementControllerIfc getMovementController() {
        return myMovementController;
    }

    @Override
    public final void setMovementController(
            MovementControllerIfc movementController) {
        myMovementController = movementController;
    }

    @Override
    public final void setVelocityChangeFactor(double factor) {
        if (factor <= 0.0) {
            throw new IllegalArgumentException("The velocity addFactor must be > 0.");
        }
        myVelFactor = factor;
    }

    @Override
    public final double getVelocityChangeFactor() {
        return myVelFactor;
    }

    @Override
    public double getVelocity() {
        return myVelocity.getValue() * getVelocityChangeFactor();
    }

    /**
     * @return Returns the velocity.
     */
    @Override
    public final RandomIfc getVelocityInitialRandomSource() {
        return myVelocity.getInitialRandomSource();
    }

    @Override
    public final void setVelocityInitialRandomSource(RandomIfc source) {
        myVelocity.setInitialRandomSource(source);
    }

    /**
     * @return Returns the velocity.
     */
    @Override
    public final RandomIfc getVelocityRandomSource() {
        return myVelocity.getRandomSource();
    }

    @Override
    public void setVelocityRandomSource(RandomIfc source) {
        myVelocity.setRandomSource(source);
    }

    @Override
    public final OutsideSpatialModelHandlerIfc getOSMHandler() {
        return myOSMHandler;
    }

    @Override
    public final void setOSMHandler(OutsideSpatialModelHandlerIfc OSMHandler) {
        myOSMHandler = OSMHandler;
    }

    @Override
    public final void setMovement(double velocity, CoordinateIfc position) {
        if (velocity <= 0.0) {
            throw new IllegalArgumentException("The velocity was <= 0");
        }

        // remember the next position
        myNextPosition.setCoordinates(position);

        // set the velocity of the move
        myMovementVelocity = velocity;

        // get the current position as a vector
        CoordinateIfc c = getPosition();
        myPosition.setCoordinates(c);

        // determine the distance associated with the move
        myMovementDistance = distanceTo(myNextPosition);

        // the vector from A to B can be obtained by
        // substracting the position vector a of point A from
        // the position vector b of point B
        // set direction vector equal to next position's coordinates
        myDirection.setCoordinates(myNextPosition);

        // subtract the current position, resulting in a vector from A to B
        myDirection.subtract(myPosition);

        // normalize the vector for direction
        myDirection.normalize();

        // record the time that the movement will start
        myMovementStartTime = getTime();

        // compute the total time associated with the movement
        myMovementTime = myMovementDistance / myMovementVelocity;
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append(toString());
        sb.append("Trip Destination Position: ").append(myDestination);
        sb.append(System.lineSeparator());
        sb.append("Element started trip: ").append(myTripFlag);
        sb.append(System.lineSeparator());
        sb.append("Element started moving: ").append(myMovingFlag);
        sb.append(System.lineSeparator());
        sb.append("Movement Direction: ").append(myDirection);
        sb.append(System.lineSeparator());
        sb.append("Movement Speed: ").append(myMovementVelocity);
        sb.append(System.lineSeparator());
        sb.append("Movement Distance: ").append(myMovementDistance);
        sb.append(System.lineSeparator());
        sb.append("Movement Start Time: ").append(myMovementStartTime);
        sb.append(System.lineSeparator());
        sb.append("Movement Time: ").append(myMovementTime);
        sb.append(System.lineSeparator());
        sb.append("End Movement Position: ").append(myNextPosition);
        sb.append(System.lineSeparator());
        return (sb.toString());
    }

    @Override
    public CoordinateIfc getDestination() {
        return myDestination;
    }

    /**
     * Causes the element to start a trip from its current position to the
     * coordinates specified. This starts a trip. A trip is a series of
     * movements to move from the current position to the specified coordinates.
     * At the beginning of a trip, trip start observers are notified. If the
     * trip is broken down into movements, then each movement can have its own
     * velocity, distance, and direction (as long as the final movement ends at
     * the destination). If the destination is the same as the current position
     * of the element then no trip is started and no movement occurs, i.e.
     * nothing occurs
     *
     * If the specified coordinates are not in the element's associated spatial
     * model then outsideSpatialModelHandler() is called. The default action is
     * to throw an exception but this can be overridden in
     * outsideSpatialModelHandler().
     *
     * @param destination the coordinate of the destination
     */
    protected final void moveTo(CoordinateIfc destination) {
        if (isOnTrip()) {
            throw new IllegalArgumentException(
                    "The element is already on a trip.");
        }

        if (!getSpatialModel().isValid(destination)) {
            outsideSpatialModelHandler(destination);
        } else // check to see if the destination is different than the current position
         if (!isPositionEqualTo(destination)) {
                myDestination.setCoordinates(destination);
                if (!isTripCompleted()) {
                    startTrip_();
                }
            } else {// told to travel to same location, start and end the trip 
                myTripFlag = true;
                beforeTripStarts();
                notifyObservers(TRIP_STARTED);
                myTripFlag = false;
                afterTripEnds();
                notifyObservers(TRIP_ENDED);
                if (hasNextTrip()) {
                    startNextTrip();
                }
            }
    }

    /**
     * Causes the element to start a trip to the coordinates of the specified
     * SpatialElementIfc
     *
     * @param destination the coordinate of the destination
     */
    protected final void moveTo(SpatialElementIfc destination) {
        moveTo(destination.getPosition());
    }

    /**
     * Relative to the current position this method causes the mover to start a
     * trip delta in the x1 direction
     *
     * @param dx the amount of the move
     */
    protected final void moveDeltaX1(double dx) {
        CoordinateIfc cp = this.getPosition();
        double x = cp.getX1() + dx;
        CoordinateIfc coordinate = getSpatialModel().getCoordinate(x, cp.getX2());
        moveTo(coordinate);
    }

    /**
     * Relative to the current position this method causes the mover to start a
     * trip delta in the x2 direction
     *
     * @param dy the amount of the move
     */
    protected final void moveDeltaX2(double dy) {
        CoordinateIfc cp = this.getPosition();
        double y = cp.getX2() + dy;
        CoordinateIfc coordinate = getSpatialModel().getCoordinate(cp.getX1(), y);
        moveTo(coordinate);
    }

    /**
     * Relative to the current position this method causes the mover to start a
     * trip to a position that is delta in the x1 direction and delta in the x2
     * direction.
     *
     * @param dx amount of change in the X1 direction
     * @param dy amount of change in the X2 direction
     */
    protected final void moveDeltaX1X2(double dx, double dy) {
        CoordinateIfc cp = this.getPosition();
        double x = cp.getX1() + dx;
        double y = cp.getX2() + dy;
        CoordinateIfc coordinate = getSpatialModel().getCoordinate(x, y);
        moveTo(coordinate);
    }

    /**
     * Can be used by subclasses to invoke logic prior to the start of a trip
     */
    protected void beforeTripStarts() {
    }

    /**
     * Can be used by subclasses to invoke logic after a trip ends
     */
    protected void afterTripEnds() {
    }

    /**
     * A method to determine if another trip should be started This result
     * controls whether or not startNextTrip() is called. By default, it returns
     * false. Implementors can override to provide trips via startNextTrip()
     *
     * @return true if another trip should be started, false otherwise
     */
    public boolean hasNextTrip() {
        return isDefaultNextTripOptionOn();
    }

    /**
     * Should be used by subclasses to initiate (schedule) the next trip after
     * ending the current trip. This is called immediately after afterTripEnds()
     * if and only if hasNextTrip() returns true
     */
    protected void startNextTrip() {
    }

    /**
     * Can be used by subclasses to invoke logic prior to the start of a
     * movement
     */
    protected void beforeMovementStarts() {
    }

    /**
     * Can be used by subclasses to invoke logic after a movement ends
     */
    protected void afterMovementEnds() {
    }

    /**
     * Can be used by subclasses to invoke logic when a trip is canceled
     */
    protected void tripCanceled() {
    }

    /**
     * A method that can be overridden to collect statistics after a trip is
     * completed, called right before afterTripEnds(). The statistical
     * collection must be turned on via turnOnTripStatistics() for this method
     * to be called.
     */
    protected void collectTripStats() {
        myTripCounter.increment();
        double distance = getSpatialModel().distance(getPosition(), getPreviousPosition());
        myTripDist.setValue(distance);
    }

    /**
     * This method is called if the destination is outside of the element's
     * current spatial model. By default it throws an IllegalArgumentException
     * unless the user supplies an object that implements the
     * OutsideSpatialModelHandlerIfc. Alternatively subclasses can override this
     * method and provide the behavior directly
     *
     * @param coordinate  the coordinate
     */
    protected void outsideSpatialModelHandler(CoordinateIfc coordinate) {
        if (myOSMHandler != null) {
            myOSMHandler.handleOutsideSpatialModel(this, coordinate);
        } else {
            throw new IllegalArgumentException("The coordinate (x1= " + coordinate.getX1()
                    + ",x2= " + coordinate.getX2() + ",x3= " + coordinate.getX3() + ") is not contained in the spatial model.");
        }
    }

    /**
     * This method is called at the end of a movement to update the position.
     * The setCurrentPosition() method is used to set the current position to
     * the next position attribute Overriding methods can specify other ways to
     * set the position as long as they are consistent with the process
     * implemented for the movements.
     */
    protected void updatePosition() {
        setCurrentPosition(myNextPosition);
    }

    /**
     * This method can be overridden by subclasses to compute the
     * characteristics of a movement. Alternatively the user can supply an
     * object that implements the MovementControllerIfc By default the movement
     * goes directly to the destination at the default mover velocity. It is
     * important that subclasses utilize the setMovement() method to properly
     * set the characteristics of the movement.
     */
    protected void computeMovement() {
        setMovement(getVelocity(), getDestination());
    }

    /**
     * This method can be overridden by subclasses to provide collision
     * detection. Alternatively the user can supply an object that implements
     * the CollisionDetectorIfc By default there is no collision detection. When
     * there is no collision detector this method always returns false.
     *
     * @return False if no collision, true if collided
     */
    protected boolean checkForCollision() {
        // clear the collision for reuse
        myCollision.clear();

        if (myCollisionDetector != null) {
            return (myCollisionDetector.checkForCollision(myCollision));
        } else {
            return (false);
        }
    }

    /**
     * This method can be overridden by subclasses to provide collision
     * handling. Alternatively the user can supply an object that implements the
     * CollisionHandlerIfc By default there is no collision handling.
     */
    protected void handleCollision() {
        CollisionHandlerIfc handler = myCollision.getCollisionHandler();

        if (handler != null) // first check the collision for a handler
        {
            handler.handleCollision(myCollision);
        } else // no handler on the collision, see if moving element has a
        // handler
         if (myCollisionHandler != null) {
                myCollisionHandler.handleCollision(myCollision);
            } // no handler, do nothing
    }

    /**
     * Calls the beforeTripStarts() method and notifies trip started observers
     */
    private void startTrip_() {
        myTripFlag = true;
        beforeTripStarts();
        notifyObservers(TRIP_STARTED);
        startMovement_();
    }

    /**
     * Calls the afterTripEnds() method and notifies trip end observers
     */
    private void endTrip_() {
        myNumTrips = myNumTrips + 1;
        myTripFlag = false;
        if (isTripStatOptionOn()) {
            collectTripStats();
        };
        afterTripEnds();
        notifyObservers(TRIP_ENDED);
        if (getNumTripsCompleted() >= getMaxNumTrips()) {
            this.stopExecutive("Max number of trips reaching in " + getName());
        }
        if (hasNextTrip()) {
            startNextTrip();
        }
    }

    /**
     * Used to start a movement towards a destination computes the
     * characteristics of the movement (distance, velocity, direction, end
     * position, etc) check for collisions and handles if necessary, then
     * schedules the movement. The beforeMovementStarts observers are called
     * after scheduling the movement.
     */
    private void startMovement_() {

        computeMovement_();

        // check for collision using collision detector
        // if collision detected call collision handler
        if (checkForCollision()) {
            handleCollision();
        } else {
            // if no collision, then just schedule the movement
            myCurrentMovementEvent = scheduleEvent(myEndMovementAction,
                    myMovementTime);
            myMovingFlag = true;
            beforeMovementStarts();
            notifyObservers(MOVE_STARTED);
        }
    }

    /**
     * Used internally to compute the movement either by using a supplied
     * MovementController or by calling computeMovement()
     */
    private void computeMovement_() {
        if (myMovementController != null) {
            myMovementController.controlMovement(this);
        } else {
            computeMovement();
        }
    }

    /**
     * After a movement is completed this is used to continue movement towards
     * the destination if the trip is not completed.
     */
    private void continueMovement_() {
        computeMovement_();

        // check for collision using collision detector
        // if collision detected call collision handler
        if (checkForCollision()) {
            handleCollision();
        } else {
            // if no collision, then just schedule the movement
            rescheduleEvent(myCurrentMovementEvent, myMovementTime);
            myMovingFlag = true;
            beforeMovementStarts();
            notifyObservers(MOVE_STARTED);
        }
    }

    /**
     * Represents the end of a scheduled movement. This method 1) updates the
     * position of the element 2) notifies movement ended observers 4) checks if
     * the trip is not completed continues the movements, notifying movement
     * started observers else notifies trip ended observers
     */
    private void endMovement_() {
        updatePosition();
        myMovingFlag = false;
        afterMovementEnds();
        notifyObservers(MOVE_ENDED);

        if (!isTripCompleted()) {
            continueMovement_();
        } else {
            endTrip_();
        }
    }

    /**
     * Checks to if the current position is the same as trip's destination
     *
     * @return true if completed
     */
    private boolean isTripCompleted() {
        return (isPositionEqualTo(myDestination));
    }

    private class EndMovementAction extends EventAction {

        @Override
        public void action(JSLEvent<Object> event) {
            endMovement_();
        }
    }

}
