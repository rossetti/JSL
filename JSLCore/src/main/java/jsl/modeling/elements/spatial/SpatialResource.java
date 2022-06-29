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
import jsl.observers.ObserverIfc;

/**
 * A SpatialResource is a resource that can be placed/positioned within a
 * SpatialModel
 *
 * It is not "self-moving" but can be positioned by clients at various locations
 * (coordinates, etc) within the spatial model. A SpatialResource can be
 * assigned a "home location" via the ResourceLocation class
 *
 * In all other respects a SpatialResource acts like a Resource.
 *
 */
public class SpatialResource extends Resource implements SpatialElementIfc {

    /**
     * Indicates that the transporter has changed state to its observers
     */
    public static final int MOVED = ModelElement.getNextEnumConstant();

    /**
     * Used to respresent the resources in a SpatialModel2D
     *
     */
    private SpatialElement mySpatialElement;

    /**
     * If a SpatialResource is assigned to a ResourceLocation then this field is
     * used to refer to the location. The default is null (not assigned to a
     * location).
     */
    private ResourceLocation myResourceLocation;

    /**
     * If a SpatialResource is assigned to a ResourceLocation then this field is
     * used to remember the location for when the SpatialResource is initialized
     * prior to a replication. The default is null (not assigned to a location).
     */
    private ResourceLocation myInitialResourceLocation;

    /**
     * Creates a SpatialResource with the default position within its spatial
     * model. The spatial model of the parent is used as the spatial model of
     * this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     */
    public SpatialResource(ModelElement parent) {
        this(parent, 1, null, null, null);
    }

    /**
     * Creates a SpatialResource with the given capacity at the default position
     * within its spatial model. The spatial model of the parent is used as the
     * spatial model of this object. If the parent does not have a spatial model
     * (i.e. getSpatialModel() == null), then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param capacity
     */
    public SpatialResource(ModelElement parent, int capacity) {
        this(parent, capacity, null, null, null);
    }

    /**
     * Creates a SpatialResource with the given capacity at the default position
     * within its spatial model. The spatial model of the parent is used as the
     * spatial model of this object. If the parent does not have a spatial model
     * (i.e. getSpatialModel() == null), then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param capacity
     * @param name
     */
    public SpatialResource(ModelElement parent, int capacity, String name) {
        this(parent, capacity, name, null, null);
    }

    /**
     * Creates a SpatialResource with the given capacity at (x,y) within its
     * spatial model. The spatial model of the parent is used as the spatial
     * model of this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param x
     * @param y
     */
    public SpatialResource(ModelElement parent, String name, double x, double y) {
        this(parent, 1, name, null, x, y, 0.0);
    }

    /**
     * Creates a SpatialResource with the given capacity at (x,y) within its
     * spatial model. The spatial model of the parent is used as the spatial
     * model of this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param x
     * @param y
     */
    public SpatialResource(ModelElement parent, int capacity, String name, double x, double y) {
        this(parent, capacity, name, null, x, y, 0.0);
    }

    /**
     * Creates a SpatialResource with capacity 1 at the given coordinate within
     * its spatial model. The spatial model of the parent is used as the spatial
     * model of this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param position
     */
    public SpatialResource(ModelElement parent, String name, CoordinateIfc position) {
        this(parent, 1, name, null, position);
    }

    /**
     * Creates a SpatialResource with capacity 1 at the coordinates of the
     * supplied spatial element within its spatial model. The spatial model of
     * the parent is used as the spatial model of this object. If the parent
     * does not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param position
     */
    public SpatialResource(ModelElement parent, String name, SpatialElementIfc position) {
        this(parent, 1, name, null, position.getPosition());
    }

    /**
     * Creates a SpatialResource with the given capacity at the given coordinate
     * within its spatial model. The spatial model of the parent is used as the
     * spatial model of this object. If the parent does not have a spatial model
     * (i.e. getSpatialModel() == null), then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param position
     */
    public SpatialResource(ModelElement parent, int capacity, String name, CoordinateIfc position) {
        this(parent, capacity, name, null, position);
    }

    /**
     * Creates a SpatialResource with capacity 1 at (x,y) within its spatial
     * model. The spatial model of the parent is used as the spatial model of
     * this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param x
     * @param y
     */
    public SpatialResource(ModelElement parent, double x, double y) {
        this(parent, 1, null, null, x, y, 0.0);
    }

    /**
     * Creates a SpatialResource with capacity 1 at the given coordinate within
     * its spatial model. The spatial model of the parent is used as the spatial
     * model of this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param position
     */
    public SpatialResource(ModelElement parent, CoordinateIfc position) {
        this(parent, 1, null, null, position);
    }

    /**
     * Creates a SpatialResource with the given capacity at the given coordinate
     * within its spatial model. The spatial model of the parent is used as the
     * spatial model of this object. If the parent does not have a spatial model
     * (i.e. getSpatialModel() == null), then an IllegalArgumentException is
     * thrown
     *
     * @param parent
     * @param capacity
     * @param position
     */
    public SpatialResource(ModelElement parent, int capacity, CoordinateIfc position) {
        this(parent, capacity, null, null, position);
    }

    /**
     * Creates a SpatialResource with the given capacity at (x,y) within its
     * spatial model. The spatial model of the parent is used as the spatial
     * model of this object. If the parent does not have a spatial model (i.e.
     * getSpatialModel() == null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param x
     * @param y
     */
    public SpatialResource(ModelElement parent, int capacity, double x, double y) {
        this(parent, capacity, null, null, x, y, 0.0);
    }

    /**
     * Creates a SpatialResource with the given capacity at the default position
     * within the spatial model. If the spatial model is null then the spatial
     * model of the parent is used as the spatial model of this object. If the
     * parent does not have a spatial model (i.e. getSpatialModel() == null),
     * then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param spatialModel
     */
    public SpatialResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel) {
        this(parent, capacity, name, spatialModel, null);
    }

    /**
     * Creates a SpatialResource with the given capacity at the default position
     * within the spatial model. If the spatial model is null then the spatial
     * model of the parent is used as the spatial model of this object. If the
     * parent does not have a spatial model (i.e. getSpatialModel() == null),
     * then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param spatialModel
     */
    public SpatialResource(ModelElement parent, int capacity, SpatialModel spatialModel) {
        this(parent, capacity, null, spatialModel, null);
    }

    /**
     * Creates a SpatialResource with the given capacity at (x,y,z) within the
     * spatial model. If the spatial model is null then the spatial model of the
     * parent is used as the spatial model of this object. If the parent does
     * not have a spatial model (i.e. getSpatialModel() == null), then an
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
    public SpatialResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel, double x, double y, double z) {
        this(parent, capacity, name, spatialModel, new Vector3D(x, y, z));
    }

    /**
     * Creates a SpatialResource with the given capacity at the default position
     * within the spatial model. If the spatial model is null then the spatial
     * model of the parent is used as the spatial model of this object. If the
     * parent does not have a spatial model (i.e. getSpatialModel() == null),
     * then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param capacity
     * @param name
     * @param spatialModel
     * @param coordinate
     */
    public SpatialResource(ModelElement parent, int capacity, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, capacity, name);

        if (spatialModel == null) {
            spatialModel = parent.getSpatialModel();
            if (spatialModel == null) {
                throw new IllegalArgumentException("No spatial model is available!");
            }
        }

        setSpatialModel(spatialModel); // set the model element's spatial model

        setSpatialElement(new SpatialElement(spatialModel, coordinate, getName()));
        myInitialResourceLocation = null;

    }

    @Override
    public final void attachPositionObserver(ObserverIfc observer) {
        mySpatialElement.attachPositionObserver(observer);
    }

    @Override
    public final void changeSpatialModel(SpatialModel spatialModel, CoordinateIfc coordinate) {
        mySpatialElement.changeSpatialModel(spatialModel, coordinate);
    }

    @Override
    public final double distanceTo(CoordinateIfc coordinate) {
        return mySpatialElement.distanceTo(coordinate);
    }

    @Override
    public final double distanceTo(SpatialElementIfc element) {
        return mySpatialElement.distanceTo(element);
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
    public final SpatialModel getInitialSpatialModel() {
        return mySpatialElement.getInitialSpatialModel();
    }

    @Override
    public final ModelElement getModelElement() {
        return mySpatialElement.getModelElement();
    }

    @Override
    public final CoordinateIfc getPreviousPosition() {
        return mySpatialElement.getPreviousPosition();
    }

    @Override
    public final void initializeSpatialElement() {
        mySpatialElement.initializeSpatialElement();
    }

    @Override
    public final boolean isPositionEqualTo(CoordinateIfc coordinate) {
        return mySpatialElement.isPositionEqualTo(coordinate);
    }

    @Override
    public final boolean isPositionEqualTo(SpatialElementIfc element) {
        return mySpatialElement.isPositionEqualTo(element);
    }

    @Override
    public final void removePositionObserver(ObserverIfc observer) {
        mySpatialElement.removePositionObserver(observer);
    }

    @Override
    public final void setInitialPosition(CoordinateIfc c) {
        mySpatialElement.setInitialPosition(c);
    }

    /**
     * Sets the position to the coordinates of the supplied location
     *
     * @param location
     */
    public final void setPosition(CoordinateIfc location) {
        mySpatialElement.setCurrentPosition(location);
        notifyObservers(MOVED);
    }

    /**
     * Sets the position to the coordinates of the supplied location
     *
     * @param element
     */
    public final void setPosition(SpatialElementIfc element) {
        setPosition(element.getPosition());
    }

    /**
     * Gets the initial resource location. This location is used when the
     * element is initialized, prior to a replication.
     *
     * @return Returns the myInitialResourceLocation.
     */
    public final ResourceLocation getInitialResourceLocation() {
        return myInitialResourceLocation;
    }

    /**
     * Returns the associated ResourceLocation if one exists. May be null
     *
     * @return Returns the ResourceLocation.
     */
    public final ResourceLocation getResourceLocation() {
        return myResourceLocation;
    }

    protected void initialize() {
        super.initialize();
        mySpatialElement.initializeSpatialElement();
        setResourceLocation(getInitialResourceLocation());
    }

    protected final SpatialElement getSpatialElement() {
        return mySpatialElement;
    }

    /**
     * Sets the initial resource location. This location is used when the
     * element is initialized prior to a replication. This may be null.
     *
     * @param location The initial resource locaiton to set for the element
     */
    protected final void setInitialResourceLocation(ResourceLocation location) {
        myInitialResourceLocation = location;
    }

    /**
     * Sets the ResourceLocation for this SpatialResource. It can be null
     *
     * @param resourceLocation The resourceLocation to set.
     */
    protected final void setResourceLocation(ResourceLocation resourceLocation) {
        myResourceLocation = resourceLocation;
    }

    /**
     * Sets the underlying SpatialElement
     *
     * @param spatialElement
     */
    protected final void setSpatialElement(SpatialElement spatialElement) {
        if (spatialElement == null) {
            throw new IllegalArgumentException("The supplied spatial element was null!");
        }
        mySpatialElement = spatialElement;
        mySpatialElement.setModelElement(this);
    }
}
