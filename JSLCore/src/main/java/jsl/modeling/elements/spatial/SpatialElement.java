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
import jsl.observers.ObservableComponent;
import jsl.observers.ObserverIfc;

/**
 * A SpatialElement represents an object that is within a spatial model. A
 * spatial element can be in one and only one spatial model at a time. A spatial
 * model may contain many spatial elements. Spatial elements can be added to and
 * removed from a spatial model. A spatial element has a position within a
 * spatial model. The position should allow the distance between spatial
 * elements to be computed within a spatial model. If the position of the
 * spatial element is changed then the spatial element should notify any
 * position observers and should indicate to its spatial model that the position
 * has changed.
 *
 * A spatial element should start in one spatial model. The starting spatial
 * model is the element's initial model. A spatial element may move to other
 * spatial models during a simulation and thus should always have a current
 * spatial model. A spatial element may be related to a ModelElement.
 *
 *
 */
public class SpatialElement implements SpatialElementIfc {

    /**
     * incremented to give a running total of the number of elements created
     */
    private static int myCounter_;

    /**
     * An enum to indicate that an element changed its position within a spatial
     * model
     */
    public static final int CHANGED_POSITION = ModelElement.getNextEnumConstant();

    /**
     * Holds the initial position in the spatial model
     */
    private Vector3D myInitialPosition;

    /**
     * Holds the previous position in the spatial model
     */
    private Vector3D myPreviousPosition;

    /**
     * Holds the current position in in the spatial model
     */
    private Vector3D myCurrentPosition;

    /**
     * A reference to a spatial model to allow location within the spatial
     * context
     */
    private SpatialModel mySpatialModel;

    /**
     * A reference to a spatial model to allow location within the spatial
     * context
     */
    private SpatialModel myInitialSpatialModel;

    /**
     * helper for observable pattern
     *
     */
    private final ObservableComponent myObservableComponent;

    /**
     * Is used by observers to check what state the element is in when notified
     * of a change
     */
    private int myObserverState;

    /**
     * A name for the element
     */
    private String myName;

    /**
     * A unique id given to the element
     */
    private int myId;

    /**
     * If the element is related to a JSL ModelElement then this refers to the
     * element
     */
    private ModelElement myModelElement;

    /**
     * Creates a spatial element with the given spatial model. The CoordinateIfc
     * will be the default within the supplied spatial model.
     *
     * @param spatialModel
     */
    public SpatialElement(SpatialModel spatialModel) {
        this(spatialModel, null, null);
    }

    /**
     * Creates a spatial element with the given parent and spatial model. TThe
     * CoordinateIfc must be valid within the supplied spatial model.
     *
     * @param spatialModel
     * @param coordinate
     */
    public SpatialElement(SpatialModel spatialModel, CoordinateIfc coordinate) {
        this(spatialModel, coordinate, null);
    }

    /**
     * Creates a spatial element with the given parent and spatial model. TThe
     * coordinate will be the default within the supplied spatial model.
     *
     * @param spatialModel
     * @param name
     */
    public SpatialElement(SpatialModel spatialModel, String name) {
        this(spatialModel, null, name);
    }

    /**
     * Creates a spatial element at the given (x, y, 0.0) coordinates
     *
     * @param spatialModel
     * @param x
     * @param y
     */
    public SpatialElement(SpatialModel spatialModel, double x, double y) {
        this(spatialModel, new Vector3D(x, y, 0.0), null);
    }

    /**
     * Creates a spatial element at the given (x, y, 0.0) coordinates
     *
     * @param spatialModel
     * @param x
     * @param y
     * @param name
     */
    public SpatialElement(SpatialModel spatialModel, double x, double y, String name) {
        this(spatialModel, new Vector3D(x, y, 0.0), name);
    }

    /**
     * Creates a spatial element at the given (x,y,z) coordinates
     *
     * @param spatialModel
     * @param x
     * @param y
     * @param z
     */
    public SpatialElement(SpatialModel spatialModel, double x, double y, double z) {
        this(spatialModel, new Vector3D(x, y, z), null);
    }

    /**
     * Creates a spatial element at the given (x,y,z) coordinates
     *
     * @param spatialModel
     * @param x
     * @param y
     * @param z
     * @param name
     */
    public SpatialElement(SpatialModel spatialModel, double x, double y, double z, String name) {
        this(spatialModel, new Vector3D(x, y, z), name);
    }

    /**
     * Creates a spatial element with the given parent and spatial model. The
     * coordinate must be valid within the supplied spatial model.
     *
     * @param spatialModel
     * @param coordinate
     * @param name
     */
    public SpatialElement(SpatialModel spatialModel, CoordinateIfc coordinate, String name) {

        if (spatialModel == null) {
            throw new IllegalArgumentException("No spatial model was provided!");
        }

        if (coordinate == null) {
            coordinate = spatialModel.getDefaultCoordinate();
        }

        if (!spatialModel.isValid(coordinate)) {
            throw new IllegalArgumentException("The supplied coordinate is not valid for the given spatial model.");
        }

        // if we get here we know that the coordinate is valid for the spatial model
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        setName(name);

        myObservableComponent = new ObservableComponent();

        // set initial spatial model 
        setInitialSpatialModel(spatialModel);	// this just sets the initial model it doesn't do anything else

        // set spatial model and add the element to the spatial model
        setSpatialModel(spatialModel);  // this just sets the current spatial model

        // set up the positions
        myInitialPosition = Vector3D.newInstance(coordinate);
        myPreviousPosition = Vector3D.newInstance(coordinate);
        myCurrentPosition = Vector3D.newInstance(coordinate);
//        System.out.println("In SpatialElement constructor:");
//        System.out.println(" >Telling spatial model to add this spatial element.");
//        System.out.println(" >Name of element being added: " + getName());
        spatialModel.addSpatialElement(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("------------");
        sb.append(System.lineSeparator());
        sb.append("SpatialElement");
        sb.append(System.lineSeparator());
        sb.append("Class name: ").append(this.getClass().getSimpleName()).append(System.lineSeparator());
        sb.append("ID: ").append(getId()).append(System.lineSeparator());
        sb.append("Name: ").append(getName()).append(System.lineSeparator());
        sb.append("Initial Position: ").append(getInitialPosition()).append(System.lineSeparator());
        sb.append("Position: ").append(getPosition()).append(System.lineSeparator());
        sb.append("Previous Position: ").append(getPreviousPosition()).append(System.lineSeparator());
        if (getObserverState() == CHANGED_POSITION) {
            sb.append("Observer State: ").append("CHANGED_POSITION").append(System.lineSeparator());
        }
        sb.append("Contained in spatial model: ").append(getSpatialModel().getName()).append(System.lineSeparator());
        if (getModelElement() != null) {
            sb.append("Attached model element: ").append(getModelElement().getName()).append(System.lineSeparator());
        } else {
            sb.append("Attached model element: ").append("NONE").append(System.lineSeparator());
        }
        sb.append("------------");
        return sb.toString();
    }

    @Override
    public final String getName() {
        return myName;
    }

    @Override
    public final int getId() {
        return (myId);
    }

    @Override
    public final CoordinateIfc getPosition() {
        return myCurrentPosition;
    }

    @Override
    public final CoordinateIfc getInitialPosition() {
        return myInitialPosition;
    }

    @Override
    public final CoordinateIfc getPreviousPosition() {
        return myPreviousPosition;
    }

    @Override
    public final int getObserverState() {
        return myObserverState;
    }

    @Override
    public final double distanceTo(CoordinateIfc coordinate) {
        return (mySpatialModel.distance(getPosition(), coordinate));
    }

    @Override
    public final boolean isPositionEqualTo(CoordinateIfc coordinate) {
        return (mySpatialModel.comparePositions(getPosition(), coordinate));
    }

    @Override
    public final double distanceTo(SpatialElementIfc element) {
        return (mySpatialModel.distance(this, element));
    }

    @Override
    public final boolean isPositionEqualTo(SpatialElementIfc element) {
        return (mySpatialModel.comparePositions(this, element));
    }

    @Override
    public final SpatialModel getSpatialModel() {
        return mySpatialModel;
    }

    @Override
    public final SpatialModel getInitialSpatialModel() {
        return (myInitialSpatialModel);
    }

    @Override
    public final void changeSpatialModel(SpatialModel spatialModel, CoordinateIfc coordinate) {

        if (spatialModel == null) {
            throw new IllegalArgumentException("The suppled spatial model was equal to null.");
        }

        // check if the coordinate is valid within the supplied spatial model
        if (!spatialModel.isValid(coordinate)) {
            throw new IllegalArgumentException("The spatial model is not valid for the supplied coordinate.");
        }

        if (spatialModel == mySpatialModel)// no need to change spatial model
        {
            return;
        }

        // remove the element from its current spatial model
        mySpatialModel.removeSpatialElement(this);

        //set the spatial model to new spatial model
        setSpatialModel(spatialModel);

        // adds the element
        spatialModel.addSpatialElement(this);

        // directly updates the positions, coordinate should be valid
        myPreviousPosition.setCoordinates(coordinate);
        myCurrentPosition.setCoordinates(coordinate);

        // notify any position update listeners for the spatial model
        mySpatialModel.updatePosition_(this);

        // notify any position change listeners for this spatial element
        notifyObservers(CHANGED_POSITION);

    }

    @Override
    public ModelElement getModelElement() {
        return myModelElement;
    }

    @Override
    public void initializeSpatialElement() {

        if (myInitialSpatialModel != mySpatialModel) {
            // There has been a change from the initial spatial model
            // return the element to it's initial spatial model, at the initial position
            changeSpatialModel(myInitialSpatialModel, getInitialPosition());
        } else // no change in spatial model, but position might have changed
        // check if current position is not same as initial position
        if (!isPositionEqualTo(getInitialPosition())) {
            // there has been a change in position from the initial position
            // at initialization the current position must be the initial position
            // set current/previous to coordinates of initial position
            setPreviousPosition(getInitialPosition());
            setCurrentPosition(getInitialPosition());
        }

    }

    @Override
    public final void setInitialPosition(CoordinateIfc initialPosition) {
        if (!mySpatialModel.isValid(initialPosition)) {
            throw new IllegalArgumentException("The supplied coordinate is not valid for the give spatial model.");
        }

        myInitialPosition.setCoordinates(initialPosition);
    }

    /**
     * Sets the spatial model for the spatial element. This method is used by
     * addSpatialElement() within SpatialModel
     *
     * @param spatialModel
     */
    protected final void setSpatialModel(SpatialModel spatialModel) {
        mySpatialModel = spatialModel;
    }

    /**
     * Sets the model element associated with this spatial element, may be null
     *
     * @param modelElement
     */
    protected void setModelElement(ModelElement modelElement) {
//        System.out.println("In SpatialElement: setModelElement()");
        myModelElement = modelElement;
    }

    /**
     * @param currentPosition The currentPosition to set.
     */
    protected final void setCurrentPosition(CoordinateIfc currentPosition) {
        if (!mySpatialModel.isValid(currentPosition)) {
            throw new IllegalArgumentException("The supplied coordinate is not valid for the give spatial model.");
        }

        setPreviousPosition(getPosition());

        myCurrentPosition.setCoordinates(currentPosition);

        mySpatialModel.updatePosition_(this);

        notifyObservers(CHANGED_POSITION);
    }

    /**
     * @param previousPosition The previousPosition to set.
     */
    protected final void setPreviousPosition(CoordinateIfc previousPosition) {
        myPreviousPosition.setCoordinates(previousPosition);
    }

    /**
     * Sets the name of this element
     *
     * @param str The name as a string.
     */
    protected final void setName(String str) {

        if (str == null) { // no name is being passed, construct a default name
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            str = s + "-" + getId();
        }

        myName = str;

    }

    /**
     * Sets the initial spatial model for the position. Since a element can move
     * between spatial models during a simulation, there must be a way to
     * remember which spatial model the element started with prior to re-running
     * a simulation This method sets the initial spatial model. This only has an
     * effect when the element is initialized. Calling this method during a
     * simulation has no effect until the element is initialized. The current
     * spatial model is not changed by this method. When the element is
     * initialized, its current spatial model is set to the supplied spatial
     * model.
     *
     * @param spatialModel
     */
    protected final void setInitialSpatialModel(SpatialModel spatialModel) {
        if (spatialModel == null) {
            throw new IllegalArgumentException("The suppled spatial model was equal to null.");
        }
        myInitialSpatialModel = spatialModel;
    }

    /**
     * Can be called by sub-classes to set an indicator of the state of the
     * position prior to notifying observers
     *
     * @param observerState The observerState to set.
     */
    protected final void setObserverState(int observerState) {
        myObserverState = observerState;
    }

    /**
     * @param observerState The observerState to set.
     */
    protected final void notifyObservers(int observerState) {
        setObserverState(observerState);
        notifyObservers(this, null);
    }

    @Override
    public void attachPositionObserver(ObserverIfc observer) {
        myObservableComponent.addObserver(observer);
    }

    @Override
    public void removePositionObserver(ObserverIfc observer) {
        myObservableComponent.deleteObserver(observer);
    }

    protected final void notifyObservers(Object theObserved, Object arg) {
        myObservableComponent.notifyObservers(theObserved, arg);
    }
}
