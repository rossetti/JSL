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

/**
 *
 */
public class Mover extends AbstractMover {

    /**
     * Creates a Mover with the default position within its spatial model. The
     * spatial model of the parent is used as the spatial model of this object.
     * If the parent does not have a spatial model (i.e. getSpatialModel() ==
     * null), then an IllegalArgumentException is thrown
     *
     * @param parent
     */
    public Mover(ModelElement parent) {
        this(parent, null, null, null);
    }

    /**
     * Creates a Mover with the default position within its spatial model. The
     * spatial model of the parent is used as the spatial model of this object.
     * If the parent does not have a spatial model (i.e. getSpatialModel() ==
     * null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     */
    public Mover(ModelElement parent, String name) {
        this(parent, name, null, null);
    }

    /**
     * Creates a Mover at the given coordinate within its spatial model. The
     * spatial model of the parent is used as the spatial model of this object.
     * If the parent does not have a spatial model (i.e. getSpatialModel() ==
     * null), then an IllegalArgumentException is thrown
     *
     * @param parent
     * @param coordinate
     */
    public Mover(ModelElement parent, CoordinateIfc coordinate) {
        this(parent, null, null, coordinate);
    }

    /**
     * Creates a Mover with the default position within the given spatial model.
     * If the supplied spatial model is null the spatial model of the parent is
     * used as the spatial model of this object. If the parent does not have a
     * spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     */
    public Mover(ModelElement parent, String name, SpatialModel spatialModel) {
        this(parent, name, spatialModel, null);
    }

    /**
     * Creates a Mover with the default position within the given spatial model.
     * If the supplied spatial model is null the spatial model of the parent is
     * used as the spatial model of this object. If the parent does not have a
     * spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param spatialModel
     */
    public Mover(ModelElement parent, SpatialModel spatialModel) {
        this(parent, null, spatialModel, null);
    }

    /**
     * Creates a Mover with the given coordinates within the given spatial
     * model. If the supplied spatial model is null the spatial model of the
     * parent is used as the spatial model of this object. If the parent does
     * not have a spatial model (i.e. getSpatialModel() == null), then an
     * IllegalArgumentException is thrown
     *
     * @param parent
     * @param name
     * @param spatialModel
     * @param coordinate
     */
    public Mover(ModelElement parent, String name, SpatialModel spatialModel, CoordinateIfc coordinate) {
        super(parent, name, spatialModel, coordinate);
    }

    /**
     * Causes the element to travel from its current position to the coordinates
     * specified. This starts a trip. A trip is a series of movements to move
     * from the current position to the specified coordinates. At the beginning
     * of a trip, trip start observers are notified. If the trip is broken down
     * into movements, then each movement can have its own velocity, distance,
     * and direction (as long as the final movement ends at the destination). If
     * the destination is the same as the current position of the element then
     * no trip is started and no movement occurs, i.e. nothing occurs
     *
     * If the specified coordinates are not in the element's associated spatial
     * model then outsideSpatialModelHandler() is called. The default action is
     * to throw an exception but this can be overridden in
     * outsideSpatialModelHandler().
     *
     * @param destination Must not be null
     */
    public final void travelTo(CoordinateIfc destination) {
        moveTo(destination);
    }

}
