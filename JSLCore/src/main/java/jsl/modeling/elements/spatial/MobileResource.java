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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.entity.Resource;
import jsl.modeling.elements.spatial.transporter.EmptyMoveCompletionIfc;
import jsl.modeling.elements.spatial.transporter.TransportCompletionIfc;
import jsl.modeling.elements.spatial.transporter.Transporter;
import jsl.observers.ObserverIfc;
import jsl.utilities.random.RandomIfc;

/**
 *
 */
abstract public class MobileResource extends Resource implements MoverIfc {

    /**
     * Used to model the movements
     */
    private Transporter myTransporter;

    /**
     * Listens for empty move completions
     */
    private final EmptyMoveCompletedListener myEmptyMoveCompletedListener = new EmptyMoveCompletedListener();

    /**
     * Listens for transport completed events
     */
    private final TransportCompletedListener myTransportCompletedListener = new TransportCompletedListener();

    /**
     * Creates a MobileResource with capacity 1 at the default position in the
     * spatial model. The spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     */
    public MobileResource(ModelElement parent) {
        this(parent, 1, null, null, null);
    }

    /**
     * Creates a MobileResource with capacity 1 at the default position in the
     * spatial model. The spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     */
    public MobileResource(ModelElement parent, String name) {
        this(parent, 1, name, null, null);
    }

    /**
     * Creates a MobileResource with the given capacity at the default position
     * in the spatial model. The spatial model of the parent is used as the
     * spatial model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     */
    public MobileResource(ModelElement parent, int capacity) {
        this(parent, capacity, null, null, null);
    }

    /**
     * Creates a MobileResource with the given capacity at the default position
     * in the spatial model. The spatial model of the parent is used as the
     * spatial model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     */
    public MobileResource(ModelElement parent, int capacity, String name) {
        this(parent, capacity, name, null, null);
    }

    /**
     * Creates a MobileResource with capacity 1 at the given coordinates. The
     * spatial model of the parent is used as the spatial model. If the parent
     * does not have a spatial model, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param position
     */
    public MobileResource(ModelElement parent, String name, CoordinateIfc position) {
        this(parent, 1, name, null, position);
    }

    /**
     * Creates a MobileResource with capacity 1 at the given coordinates. The
     * spatial model of the parent is used as the spatial model. If the parent
     * does not have a spatial model, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param position
     */
    public MobileResource(ModelElement parent, int capacity, String name, CoordinateIfc position) {
        this(parent, capacity, name, null, position);
    }

    /**
     * Creates a MobileResource with capacity 1 and (x,y) position. The spatial
     * model of the parent is used as the spatial model. If the parent does not
     * have a spatial model, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param x
     * @param y
     */
    public MobileResource(ModelElement parent, String name, double x, double y) {
        this(parent, 1, name, null, x, y, 0.0);
    }

    /**
     * Creates a MobileResource with the given capacity and (x,y) position. The
     * spatial model of the parent is used as the spatial model. If the parent
     * does not have a spatial model, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param x
     * @param y
     */
    public MobileResource(ModelElement parent, int capacity, String name, double x, double y) {
        this(parent, capacity, name, null, x, y, 0.0);
    }

    /**
     * Creates a MobileResource with the given capacity at the coordinates of
     * the given spatial element The spatial model of the parent is used as the
     * spatial model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param position
     */
    public MobileResource(ModelElement parent, SpatialElementIfc position) {
        this(parent, 1, null, null, position.getPosition());
    }

    /**
     * Creates a MobileResource with the given capacity at the coordinates of
     * the given spatial element The spatial model of the parent is used as the
     * spatial model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param position
     * @param name
     */
    public MobileResource(ModelElement parent, SpatialElementIfc position, String name) {
        this(parent, 1, name, null, position.getPosition());
    }

    /**
     * Creates a MobileResource with the given capacity at the coordinates of
     * the given spatial element The spatial model of the parent is used as the
     * spatial model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param position
     */
    public MobileResource(ModelElement parent, int capacity, SpatialElementIfc position) {
        this(parent, capacity, null, null, position.getPosition());
    }

    /**
     * Creates a MobileResource with the given capacity at the given
     * coordinates. The spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param position
     */
    public MobileResource(ModelElement parent, int capacity, CoordinateIfc position) {
        this(parent, capacity, null, null, position);
    }

    /**
     * Creates a MobileResource with the given capacity and (x,y) position. The
     * spatial model of the parent is used as the spatial model. If the parent
     * does not have a spatial model, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param x
     * @param y
     */
    public MobileResource(ModelElement parent, int capacity, double x, double y) {
        this(parent, capacity, null, null, x, y, 0.0);
    }

    /**
     * Creates a MobileResource with the given capacity at the default
     * coordinates in the supplied spatial model. The supplied is used unless it
     * is null, then the spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param spatialModel
     */
    public MobileResource(ModelElement parent, int capacity, SpatialModel spatialModel) {
        this(parent, capacity, null, spatialModel, null);
    }

    /**
     * Creates a MobileResource with the given capacity at the default
     * coordinates in the supplied spatial model. The supplied is used unless it
     * is null, then the spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param spatialModel
     */
    public MobileResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel) {
        this(parent, capacity, name, spatialModel, null);
    }

    /**
     * Creates a MobileResource with the given capacity at the (x,y,z)
     * coordinates in the supplied spatial model. The supplied is used unless it
     * is null, then the spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param spatialModel
     * @param x
     * @param y
     * @param z
     */
    public MobileResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel, double x, double y, double z) {
        this(parent, capacity, name, spatialModel, new Vector3D(x, y, z));
    }

    /**
     * Creates a MobileResource with the given capacity at the supplied
     * coordinates in the supplied spatial model. The supplied is used unless it
     * is null, then the spatial model of the parent is used as the spatial
     * model. If the parent does not have a spatial model, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param spatialModel
     * @param coordinate
     */
    public MobileResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, capacity, name);

        if (spatialModel == null) {
            spatialModel = parent.getSpatialModel();
            if (spatialModel == null) {
                throw new IllegalArgumentException("No spatial model is available!");
            }
        }

        setSpatialModel(spatialModel); // set the model element's spatial model

        myTransporter = new Transporter(this, getName() + "Mover", spatialModel, coordinate);
        // allow the Moveable Resource to observe state changes in underlying transporter
        // so that they can be passed along to observers of the MobileResource
        myTransporter.addObserver(new TransporterObserver());

    }

    @Override
    public final CollisionDetectorIfc getCollisionDetector() {
        return myTransporter.getCollisionDetector();
    }

    @Override
    public final CollisionHandlerIfc getCollisionHandler() {
        return myTransporter.getCollisionHandler();
    }

    @Override
    public final CoordinateIfc getDestination() {
        return myTransporter.getDestination();
    }

    @Override
    public final CoordinateIfc getFuturePosition(double time) {
        return myTransporter.getFuturePosition(time);
    }

    @Override
    public final MovementControllerIfc getMovementController() {
        return myTransporter.getMovementController();
    }

    @Override
    public final double getMovementDistance() {
        return myTransporter.getMovementDistance();
    }

    @Override
    public final double getMovementStartTime() {
        return myTransporter.getMovementStartTime();
    }

    @Override
    public final double getMovementTime() {
        return myTransporter.getMovementTime();
    }

    @Override
    public final double getMovementVelocity() {
        return myTransporter.getMovementVelocity();
    }

    @Override
    public final OutsideSpatialModelHandlerIfc getOSMHandler() {
        return myTransporter.getOSMHandler();
    }

    @Override
    public final boolean isMoving() {
        return myTransporter.isMoving();
    }

    @Override
    public final boolean isOnTrip() {
        return myTransporter.isOnTrip();
    }

    @Override
    public final void setCollisionDetector(CollisionDetectorIfc collisionDetector) {
        myTransporter.setCollisionDetector(collisionDetector);
    }

    @Override
    public final void setCollisionHandler(CollisionHandlerIfc collisionHandler) {
        myTransporter.setCollisionHandler(collisionHandler);
    }

    @Override
    public final void setMovement(double velocity, CoordinateIfc position) {
        myTransporter.setMovement(velocity, position);
    }

    @Override
    public final void setMovementController(MovementControllerIfc movementController) {
        myTransporter.setMovementController(movementController);
    }

    @Override
    public final void setOSMHandler(OutsideSpatialModelHandlerIfc OSMHandler) {
        myTransporter.setOSMHandler(OSMHandler);
    }

    @Override
    public final void setVelocityInitialRandomSource(RandomIfc source) {
        myTransporter.setVelocityInitialRandomSource(source);
    }

    @Override
    public void setVelocityRandomSource(RandomIfc source) {
        myTransporter.setVelocityRandomSource(source);
    }

    @Override
    public final void attachPositionObserver(ObserverIfc observer) {
        myTransporter.attachPositionObserver(observer);
    }

    @Override
    public final void changeSpatialModel(SpatialModel spatialModel, CoordinateIfc coordinate) {
        myTransporter.changeSpatialModel(spatialModel, coordinate);
    }

    @Override
    public final double distanceTo(CoordinateIfc coordinate) {
        return myTransporter.distanceTo(coordinate);
    }

    @Override
    public final double distanceTo(SpatialElementIfc element) {
        return myTransporter.distanceTo(element);
    }

    @Override
    public final CoordinateIfc getPosition() {
        return myTransporter.getPosition();
    }

    @Override
    public final CoordinateIfc getInitialPosition() {
        return myTransporter.getInitialPosition();
    }

    @Override
    public void setInitialPosition(CoordinateIfc coordinate) {
        myTransporter.setInitialPosition(coordinate);
    }

    @Override
    public final SpatialModel getInitialSpatialModel() {
        return myTransporter.getInitialSpatialModel();
    }

    @Override
    public final ModelElement getModelElement() {
        return myTransporter.getModelElement();
    }

    @Override
    public final CoordinateIfc getPreviousPosition() {
        return myTransporter.getPreviousPosition();
    }

    @Override
    public final void initializeSpatialElement() {
        myTransporter.initializeSpatialElement();
    }

    @Override
    public final boolean isPositionEqualTo(CoordinateIfc coordinate) {
        return myTransporter.isPositionEqualTo(coordinate);
    }

    @Override
    public final boolean isPositionEqualTo(SpatialElementIfc element) {
        return myTransporter.isPositionEqualTo(element);
    }

    @Override
    public final void removePositionObserver(ObserverIfc observer) {
        myTransporter.removePositionObserver(observer);
    }

    @Override
    protected void initialize() {
        super.initialize();
        // need to put the transporter back in the allocated state
        // transporter can only be freed, moveEmpty(), or transport()
        myTransporter.allocate();
    }

    /**
     * Subclasses can use this to access the underlying Transporter
     *
     * @return Returns the Transporter.
     */
    protected final Transporter getTransporter() {
        return myTransporter;
    }

    /**
     * Tells the resource to move empty to the supplied coordinates
     *
     * @param coordinate
     */
    protected void moveEmpty(CoordinateIfc coordinate) {
        myTransporter.moveEmpty(coordinate, myEmptyMoveCompletedListener);
    }

    /**
     * Tells the resource to move empty to the coordinates of the supplied
     * spatial element
     *
     * @param element
     */
    protected void moveEmpty(SpatialElementIfc element) {
        myTransporter.moveEmpty(element.getPosition(), myEmptyMoveCompletedListener);
    }

    /**
     * Tells the resource to transport to the supplied coordinates
     *
     * @param coordinate
     */
    protected void transport(CoordinateIfc coordinate) {
        myTransporter.transport(coordinate, myTransportCompletedListener);
    }

    /**
     * Tells the resource to transport to the coordinates of the supplied
     * spatial element
     *
     * @param element
     */
    protected void transport(SpatialElementIfc element) {
        myTransporter.transport(element.getPosition(), myTransportCompletedListener);
    }

    /**
     * Subclasses must override this method to react to the end of an empty move
     *
     */
    abstract protected void emptyMoveCompleted();

    /**
     * Subclasses must override this method to react to the end of a transport
     */
    abstract protected void transportCompleted();

    private class EmptyMoveCompletedListener implements EmptyMoveCompletionIfc {

        @Override
        public void emptyMoveComplete(Transporter transporter) {
            emptyMoveCompleted();
        }
    }

    private class TransportCompletedListener implements TransportCompletionIfc {

        @Override
        public void transportComplete(Transporter transporter) {
            transportCompleted();
        }
    }

    private class TransporterObserver implements ObserverIfc {

        @Override
        public void update(Object arg0, Object arg1) {

            // get the state of the transporter
            int state = myTransporter.getObserverState();
            // pass the notification along to observers of the MobileResource
            //if (state != ModelElement.INITIALIZED)
            if (state >= AbstractMover.TRIP_STARTED) {
                MobileResource.this.notifyObservers(state);
            }

        }

    }

}
