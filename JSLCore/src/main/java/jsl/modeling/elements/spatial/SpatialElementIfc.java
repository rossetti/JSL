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
import jsl.observers.ObserverIfc;
import jsl.utilities.IdentityIfc;

public interface SpatialElementIfc extends IdentityIfc, PositionIfc {

    @Override
    public abstract CoordinateIfc getPosition();

    /**
     * @return Returns the initialPosition.
     */
    public abstract CoordinateIfc getInitialPosition();

    /** Sets the initial position of the element.  This position should
     *  be used when initializeSpatialElement() is called, typically at the
     *  beginning of a replication.
     *
     * @param coordinate the coordinate
     */
    public abstract void setInitialPosition(CoordinateIfc coordinate);

    /**
     * @return Returns the previousPosition.
     */
    public abstract CoordinateIfc getPreviousPosition();

    /** Observers can call this to get an integer representing the
     *  state of the element after the observers have been notified
     *
     * @return the observer state
     */
    public abstract int getObserverState();

    /** This is a "convenience" method for getting the distance
     *  from this element to the supplied coordinate within
     *  the underlying spatial model
     *
     * @param coordinate the coordinate to check
     * @return the distance
     */
    public abstract double distanceTo(CoordinateIfc coordinate);

    /** Returns true if the position of this element is the
     *  same as supplied coordinate within the underlying spatial
     *  model.  This is not necessarily object reference equality, but rather
     *  whether or not the positions within the underlying spatial model
     *  can be considered the same (equivalent).
     *
     *
     * @param coordinate the coordinate to check
     * @return true if position is equal
     */
    public abstract boolean isPositionEqualTo(CoordinateIfc coordinate);

    /** This is a "convenience" method for getting the distance
     *  from this element to the supplied element within
     *  the underlying spatial model
     *
     *  Requirement: The elements must be in the same spatial model.
     *  The distance should be calculated by the spatial model.
     *  If they are not in the same spatial model this method will
     *  throw and IllegalArgumentException
     *
     * @param element the element to check
     * @return the distance
     */
    public abstract double distanceTo(SpatialElementIfc element);

    /** Returns true if the position of this element is the
     *  same as the position of the supplied element within the underlying spatial
     *  model.  This is not necessarily object reference equality, but rather
     *  whether or not the positions within the underlying spatial model
     *  can be considered the same (equivalent).
     *
     *  Requirement: The elements must be in the same spatial model. If
     *  they are not in the same spatial model, then this method should
     *  return false.
     *
     * @param element the element to check
     * @return the distance
     */
    public abstract boolean isPositionEqualTo(SpatialElementIfc element);

    /** Returns the current spatial model that contains this
     *  element
     *
     * @return the spatial model
     */
    public abstract SpatialModel getSpatialModel();

    /** Returns the spatial model that should hold this element
     *  at the beginning of each replication of a simulation
     *
     * @return the spatial model
     */
    public abstract SpatialModel getInitialSpatialModel();

    /** Changes the spatial model for this element and places the element at the supplied
     *  coordinate within the new spatial model.
     *
     *  Throws IllegalArgumentException if the coordinate is not valid for the
     *  supplied spatial model.
     *
     *  This spatial element becomes a child element of the new spatial model.
     *
     * @param spatialModel the spatial model
     * @param coordinate the coordinate
     */
    public abstract void changeSpatialModel(SpatialModel spatialModel,
            CoordinateIfc coordinate);

    /** Gets the ModelElement associated with this spatial element
     *  May be null
     *
     * @return the model element
     */
    public abstract ModelElement getModelElement();

//    /** Sets the model element associated with this spatial element if available
//     *
//     * @param modelElement
//     */
////	public abstract void setModelElement(ModelElement modelElement);
    /** This method should be called to initialize the spatial element prior
     *  to running a simulation
     *
     *
     */
    public abstract void initializeSpatialElement();

    /** Implementor of this interface should allow Observers to
     *  be attached.  For example, the observers should be notified
     *  when the position changes.  It is the responsibility of implementers
     *  to properly notify the observers.
     *
     * @param observer the observer
     */
    public abstract void attachPositionObserver(ObserverIfc observer);

    /** Remove the observer from this PositionIfc
     *
     * @param observer the observer
     */
    public abstract void removePositionObserver(ObserverIfc observer);
}
