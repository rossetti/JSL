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

import java.util.ArrayList;
import java.util.List;

import jsl.simulation.Model;
import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.simulation.ModelElement;
import jsl.utilities.IdentityIfc;
import jsl.utilities.math.JSLMath;

/**
 *
 */
public abstract class SpatialModel implements ObservableIfc, IdentityIfc {

    /**
     * An "enum" to indicate that a element has been added when notifying
     * observers
     */
    public static final int ADDED_ELEMENT = ModelElement.getNextEnumConstant();

    /**
     * An "enum" to indicate that a element has been reomved when notifying
     * observers
     */
    public static final int REMOVED_ELEMENT = ModelElement.getNextEnumConstant();

    /**
     * An enum to indicate that the spatial model as just updated the position
     * of a element
     */
    public static final int UPDATED_POSITION = ModelElement.getNextEnumConstant();

    /**
     * incremented to give a running total of the number of objects created
     */
    private static int myCounter_;

    /**
     * helper for observable pattern
     *
     */
    private final ObservableComponent myObservableComponent;

    /**
     * Represents the state of the spatial model Can be checked by observers
     */
    private int myObserverState;

    /**
     * The name of the spatial model
     */
    private String myName;

    /**
     * An id for the spatial model
     */
    private int myId;

    /**
     * Allows the spatial model to access the JSL Model
     */
    private Model myModel;

    /**
     * The element that notified the model of a change
     */
    private SpatialElementIfc myUpdatingElement;

    /**
     * Holds the elements defined within the model
     */
    protected List<SpatialElement> myElements;

    /**
     * The default precision when comparing spatial element positions for
     * equality
     */
    protected double myDefaultPositionPrecision = JSLMath.getDefaultNumericalPrecision();

    /**
     * Constructs a new spatial model
     *
     */
    public SpatialModel() {
        this(null);
    }

    /**
     * Constructs a new spatial model
     *
     * @param name
     */
    public SpatialModel(String name) {
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        setName(name);
        myObservableComponent = new ObservableComponent();
        myElements = new ArrayList<>();
    }

    /**
     * Returns a default set of coordinates to be used to initialize the
     * location of spatial elements if necessary.
     *
     * @return
     */
    abstract public CoordinateIfc getDefaultCoordinate();

    /**
     * Creates a valid coordinate for this spatial model This method should
     * check whether the coordinate can be represented spatially within this
     * spatial model. If not, an IllegalArgumentException should be thrown
     *
     * @param x1
     * @param x2
     * @param x3
     * @return
     */
    abstract public CoordinateIfc getCoordinate(double x1, double x2, double x3);

    /**
     * Checks to see if the supplied coordinate is physically valid within the
     * spatial model. This depends on the underlying spatial representation.
     * This method should check whether the coordinate can be represented
     * spatially within this spatial model. If so, this method should return
     * true.
     *
     * @param coordinate
     * @return
     */
    public abstract boolean isValid(CoordinateIfc coordinate);

    /**
     * Computes the distance between the two supplied coordinates
     *
     * @param fromCoordinate
     * @param toCoordinate
     * @return
     */
    public abstract double distance(CoordinateIfc fromCoordinate, CoordinateIfc toCoordinate);

    /**
     * Returns true if the first coordinate is the same as second coordinate
     * within the underlying spatial model. This is not object reference
     * equality, but rather whether or not the positions within the underlying
     * spatial model can be considered spatially (equivalent).
     *
     * Requirement: The coordinates must be valid within the spatial model. If
     * they are not valid within same spatial model, then this method should
     * return false.
     *
     * @param coordinate1
     * @param coordinate2
     * @return
     */
    public abstract boolean comparePositions(CoordinateIfc coordinate1, CoordinateIfc coordinate2);

    /**
     * Creates a valid coordinate for this spatial model, with 3rd coordinate
     * 0.0
     *
     * @param x1
     * @param x2
     * @return
     */
    public final CoordinateIfc getCoordinate(double x1, double x2) {
        return (getCoordinate(x1, x2, 0.0));
    }

    /**
     * Checks to see if the supplied element can be physically valid within the
     * spatial model. This depends on the underlying spatial representation. The
     * supplied element may or may not be contained by the spatial model. If it
     * is contained in the spatial model, it must be valid for that spatial
     * model and thus this method should return true. If the supplied element is
     * in a different spatial model, then this method should check whether the
     * element can be represented spatially within this spatial model. If so,
     * this method should return true.
     *
     * @param element
     * @return
     */
    public final boolean isValid(SpatialElementIfc element) {
        return (isValid(element.getPosition()));
    }

    /**
     * Computes the distance between the two supplied elements The elements must
     * be valid within the (same) spatial model or an IllegalArgumentException
     * will be thrown.
     *
     * @param fromElement
     * @param toElement
     * @return
     */
    public final double distance(SpatialElementIfc fromElement, SpatialElementIfc toElement) {
        return (distance(fromElement.getPosition(), toElement.getPosition()));
    }

    /**
     * Returns true if the first element's position is the same as second
     * element's position within the underlying spatial model. This is not
     * object reference equality, but rather whether or not the positions within
     * the underlying spatial model can be considered spatially (equivalent).
     *
     * Requirement: The elements must be in the same spatial model. If they are
     * not in the same spatial model, then this method should return false.
     *
     * @param element1
     * @param element2
     * @return
     */
    public final boolean comparePositions(SpatialElementIfc element1, SpatialElementIfc element2) {
        return (comparePositions(element1.getPosition(), element2.getPosition()));
    }

    /**
     * Gets the default position precision for checking if elements have the
     * same position
     *
     * @return Returns the precision.
     */
    public final double getDefaultPositionPrecision() {
        return myDefaultPositionPrecision;
    }

    /**
     * Sets the default position precision.
     *
     * @param precision The precision to set.
     */
    public final void setDefaultPositionPrecision(double precision) {
        if (precision <= 0) {
            throw new IllegalArgumentException("The precision was <= 0. Precision is typically a small positive number, e.g. 0.000001");
        }

        myDefaultPositionPrecision = precision;
    }

    /**
     * Gets the JSL Model related to this spatial model
     *
     * @return
     */
    public final Model getModel() {
        return myModel;
    }

    /**
     * Sets the JSL Model related to this spatial model. The supplied model
     * cannot be null or an IllegalArgumentException is thrown
     *
     * @param model
     */
    public final void setModel(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("The Model was null");
        }
        myModel = model;
    }

    /**
     * Gets this spatial model's name.
     *
     * @return The name of the spatial model.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /**
     * Gets a uniquely assigned integer identifier for this model. This
     * identifier is assigned when the model is created. It may vary if the
     * order of creation changes.
     *
     * @return The identifier for the model.
     */
    @Override
    public final int getId() {
        return (myId);
    }

    /**
     * Checks if the spatial model contains the supplied element. True indicates
     * that the element is within the spatial model. If the element has already
     * been added to this spatial model then this method should return true
     *
     * @param element
     * @return
     */
    public final boolean contains(SpatialElement element) {
        return myElements.contains(element);
    }

    /**
     * Returns a reference to the most recent spatial element that notified the
     * spatial model of an update change. Null if no such element. Subclasses
     * are responsible for setting this within the updatePosition() method This
     * method can be used by observers to ask the SpatialModel for the element
     * that updated its position.
     *
     * @return
     */
    public final SpatialElementIfc getUpdatingSpatialElement() {
        return (myUpdatingElement);
    }

    /**
     * Returns the observer state indicator
     *
     * @return Returns the observerState.
     */
    public final int getObserverState() {
        return myObserverState;
    }

    /**
     * Checks to see if the model is in the REMOVED_ELEMENT state. That is, has
     * removeSpatialElement() been invoked. This method can be used by observers
     * that are interested in reacting to the removeSpatialElement() for the
     * model.
     *
     * @return True means that this model element is in the REMOVED_ELEMENT
     * state
     */
    public final boolean checkForSpatialElementRemoved() {
        if (getObserverState() == REMOVED_ELEMENT) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Checks to see if the spatial model is in the ADDED_ELEMENT state. That
     * is, has addSpatialElement() been invoked. This method can be used by
     * observers that are interested in reacting to the addSpatialElement() for
     * the model.
     *
     * @return True means that this model element is in the ADDED_ELEMENT state
     */
    public final boolean checkSpatialElementAdded() {
        if (getObserverState() == ADDED_ELEMENT) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Checks to see if the model is in the UPDATED_POSITION state. That is, has
     * updatePosition() been invoked. This method can be used by observers that
     * are interested in reacting to the updatePosition() for the model.
     *
     * @return True means that this model element is in the UPDATED_POSITION
     * state
     */
    public final boolean checkSpatialElementPositionChanged() {
        if (getObserverState() == UPDATED_POSITION) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Adds the supplied element to the spatial model
     *
     * @param element
     */
    protected void addSpatialElement(SpatialElement element) {
//        System.out.println("In SpatialModel: addSpatialElement()");
        myElements.add(element);
        setObserverState(ADDED_ELEMENT);
        notifyObservers(this, null);
    }

    /**
     * Removes the supplied element from the spatial model.
     *
     * Requirements:
     *
     * 1) If the supplied element is null, then an IllegalArgumentException
     * should be thrown 2) If the supplied element is not in the spatial model
     * then an IllegalArgumentException should be thrown
     *
     * Implementors should ensure that element.getSpatialModel() returns null
     * after this method is called
     *
     * @param element
     * @return
     */
    protected final boolean removeSpatialElement(SpatialElementIfc element) {

        if (element == null) {
            throw new IllegalArgumentException("The element was null");
        }

        boolean found = myElements.remove(element);

        if (found == true) {
            setObserverState(REMOVED_ELEMENT);
            notifyObservers(this, null);
        }

        return (found);
    }

    /**
     * Should be implemented by subclasses to provide behavior when a spatial
     * element notifies that it has changed its position within the spatial
     * model
     */
    protected void updatePosition() {

    }

    /**
     * Called by spatial elements when they update their current position
     *
     * @param element
     */
    protected final void updatePosition_(SpatialElementIfc element) {

        setUpdatingElement(element);

        updatePosition();

        setObserverState(UPDATED_POSITION);
        notifyObservers(this, null);

    }

    /**
     * Sets the most recent spatial element that notified the spatial model of
     * an update change. Must not be null. Subclasses are responsible for
     * setting this within the updatePosition() method
     *
     * @param updatingElement The updatingElement to set.
     */
    protected void setUpdatingElement(SpatialElementIfc updatingElement) {
        if (updatingElement == null) {
            throw new IllegalArgumentException("The SpatialElement element was null");
        }
        myUpdatingElement = updatingElement;
    }

    /**
     * Sets the observer state indicator
     *
     * @param observerState The observer state to set.
     */
    protected final void setObserverState(int observerState) {
        myObserverState = observerState;
    }

    /**
     * Sets the name of this model
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

    @Override
    public final void addObserver(ObserverIfc observer) {
        myObservableComponent.addObserver(observer);
    }

    @Override
    public final void deleteObserver(ObserverIfc observer) {
        myObservableComponent.deleteObserver(observer);
    }

    @Override
    public final void deleteObservers() {
        myObservableComponent.deleteObservers();
    }

    @Override
    public final boolean contains(ObserverIfc observer) {
        return myObservableComponent.contains(observer);
    }

    @Override
    public final int countObservers() {
        return myObservableComponent.countObservers();
    }

    protected final void notifyObservers(Object theObserved, Object arg) {
        myObservableComponent.notifyObservers(theObserved, arg);
    }
}
