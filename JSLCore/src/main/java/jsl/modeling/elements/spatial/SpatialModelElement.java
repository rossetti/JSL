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
import jsl.simulation.SchedulingElement;
import jsl.observers.ObserverIfc;

/**
 * SpatialModelElement represents a ModelElement within a SpatialModel. A
 * SpatialModelElement does not have the ability to move within the
 * SpatialModel. Sub-classes may implement this behavior. A SpatialModelElement
 * has a fixed location within the SpatialModel
 *
 */
public class SpatialModelElement extends SchedulingElement implements SpatialElementIfc {

    private SpatialElement mySpatialElement;

    /**
     * Creates a Location2D with (0.0, 0.0) position. The SpatialModel2D of the
     * parent is used as the SpatialModel2D. If the parent does not have a
     * SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent
     */
    public SpatialModelElement(ModelElement parent) {
        this(parent, null, null, null);
    }

    /**
     * Creates a Location2D with (0.0, 0.0) position. The SpatialModel2D of the
     * parent is used as the SpatialModel2D. If the parent does not have a
     * SpatialModel2D, then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     */
    public SpatialModelElement(ModelElement parent, String name) {
        this(parent, name, null, null);
    }

    /**
     * Creates a Location2D with at the coordinates of the supplied position.
     * The SpatialModel2D of the parent is used as the SpatialModel2D. If the
     * parent does not have a SpatialModel2D, then an IllegalArgumentException
     * is thrown
     *
     * @param parent
     * @param position
     */
    public SpatialModelElement(ModelElement parent, CoordinateIfc position) {
        this(parent, null, null, position);
    }

    /**
     * Creates a Location2D with at the coordinates of the supplied position.
     * The SpatialModel2D of the parent is used as the SpatialModel2D. If the
     * parent does not have a SpatialModel2D, then an IllegalArgumentException
     * is thrown
     *
     * @param parent
     * @param position
     */
    public SpatialModelElement(ModelElement parent, SpatialElementIfc position) {
        this(parent, null, null, position.getPosition());
    }

    /**
     * Creates a Location2D with the given parent and SpatialModel2D. The
     * default position is (0.0, 0.0). If the SpatialModel2D is null, the
     * SpatialModel2D of the parent is used as the SpatialModel2D. If the parent
     * does not have a SpatialModel2D, then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     */
    public SpatialModelElement(ModelElement parent, String name, SpatialModel spatialModel) {
        this(parent, name, spatialModel, null);
    }

    /**
     * Creates a Location2D with the given parent and SpatialModel2D. The
     * default position is (0.0, 0.0). If the SpatialModel2D is null, the
     * SpatialModel2D of the parent is used as the SpatialModel2D. If the parent
     * does not have a SpatialModel2D, then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param spatialModel
     */
    public SpatialModelElement(ModelElement parent, SpatialModel spatialModel) {
        this(parent, null, spatialModel, null);
    }

    /**
     * Creates a Location2D with the given parent and SpatialModel2D. The
     * default position is (0.0, 0.0). If the SpatialModel2D is null, the
     * SpatialModel2D of the parent is used as the SpatialModel2D. If the parent
     * does not have a SpatialModel2D, then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param spatialModel
     * @param element
     */
    public SpatialModelElement(ModelElement parent, SpatialModel spatialModel, SpatialElementIfc element) {
        this(parent, null, spatialModel, element.getPosition());
    }

    /**
     * Creates a Location2D with the given parent and SpatialModel2D. If the
     * SpatialModel2D is null, the SpatialModel2D of the parent is used as the
     * SpatialModel2D. If the parent does not have a SpatialModel2D, then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     * @param coordinate
     */
    public SpatialModelElement(ModelElement parent, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, name);
        if (spatialModel == null) {
            spatialModel = parent.getSpatialModel();
            if (spatialModel == null) {
                throw new IllegalArgumentException("No spatial model is available!");
            }
        }
//        System.out.println();
//        System.out.println("In SpatialModelElement Constructor:");
//        System.out.println(" >Setting the spatial model for the model element.");
        setSpatialModel(spatialModel); // set the model element's spatial model
//        System.out.println("In SpatialModelElement Constructor:");
//        System.out.println(" >Creating its spatial element.");
        String s = getName() + ":SpatialElement";
        SpatialElement se = new SpatialElement(spatialModel, coordinate, s);
//        System.out.println("In SpatialModelElement Constructor:");
//        System.out.println(" >Setting its spatial element.");
        setSpatialElement(se);
    }

    @Override
    public final CoordinateIfc getPosition() {
        return mySpatialElement.getPosition();
    }

    @Override
    public final CoordinateIfc getInitialPosition() {
        return mySpatialElement.getInitialPosition();
    }

    @Override
    public final CoordinateIfc getPreviousPosition() {
        return mySpatialElement.getPreviousPosition();
    }

    @Override
    public final double distanceTo(CoordinateIfc coordinate) {
        return mySpatialElement.distanceTo(coordinate);
    }

    @Override
    public final boolean isPositionEqualTo(CoordinateIfc coordinate) {
        return mySpatialElement.isPositionEqualTo(coordinate);
    }

    @Override
    public final double distanceTo(SpatialElementIfc element) {
        return mySpatialElement.distanceTo(element);
    }

    @Override
    public final boolean isPositionEqualTo(SpatialElementIfc element) {
        return mySpatialElement.isPositionEqualTo(element);
    }

    @Override
    public final SpatialModel getInitialSpatialModel() {
        return mySpatialElement.getInitialSpatialModel();
    }

    @Override
    public final void changeSpatialModel(SpatialModel spatialModel, CoordinateIfc coordinate) {
        mySpatialElement.changeSpatialModel(spatialModel, coordinate);
    }

    @Override
    public final ModelElement getModelElement() {
        return mySpatialElement.getModelElement();
    }

    @Override
    public void attachPositionObserver(ObserverIfc observer) {
        mySpatialElement.attachPositionObserver(observer);
    }

    @Override
    public void removePositionObserver(ObserverIfc observer) {
        mySpatialElement.removePositionObserver(observer);
    }

    @Override
    public void setInitialPosition(CoordinateIfc coordinate) {
        mySpatialElement.setInitialPosition(coordinate);
    }

    @Override
    public void initializeSpatialElement() {
        mySpatialElement.initializeSpatialElement();
    }

    @Override
    protected void initialize() {
        super.initialize();
        mySpatialElement.initializeSpatialElement();
    }

    /**
     * Returns the spatial element associated with this spatial model element
     *
     * @return
     */
    protected final SpatialElement getSpatialElement() {
        return (mySpatialElement);
    }

    /**
     * Sets the underlying SpatialElement
     *
     * @param spatialElement the element
     */
    protected final void setSpatialElement(SpatialElement spatialElement) {
//        System.out.println("In SpatialModelElement: setSpatialElement()");
        if (spatialElement == null) {
            throw new IllegalArgumentException("The supplied spatial element was null!");
        }
        mySpatialElement = spatialElement;
        mySpatialElement.setModelElement(this);
    }

    protected void setModelElement(ModelElement modelElement) {
        mySpatialElement.setModelElement(modelElement);
    }

    protected final void setCurrentPosition(CoordinateIfc currentPosition) {
        mySpatialElement.setCurrentPosition(currentPosition);
    }
}
