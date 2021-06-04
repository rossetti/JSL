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

import java.util.*;

/**
 *
 */
public class Path implements Iterable<CoordinateIfc> {

    private List<CoordinateIfc> myPoints;

    private CoordinateIfc myOrigin;

    private double myTotalDistance;

    private SpatialModel mySpatialModel;

    public Path(SpatialModel model) {
        if (model == null) {
            throw new IllegalArgumentException("The spatial model was null");
        }
        mySpatialModel = model;
        myPoints = new ArrayList<CoordinateIfc>();
        myTotalDistance = 0.0;
    }

    /**
     * The coordinate of the origin of the path
     *
     * @return
     */
    public final CoordinateIfc getOrigin() {
        return (myOrigin);
    }

    /**
     * Sets the origin associated with the path
     *
     * @param origin
     */
    public final void setOrigin(CoordinateIfc origin) {
        if (origin == null) {
            throw new IllegalArgumentException("The Coordinate origin must not be null.");
        }
        myOrigin = origin;
    }

    /**
     * Gets the total distance associated with linear pt. to pt. travel on the
     * path
     *
     * @return Returns the totalDistance.
     */
    public final double getTotalDistance() {
        return myTotalDistance;
    }

    /**
     * Adds a point to the path. The origin is not on the path. The method
     * setOrigin() must be called prior to adding any points to the path;
     * otherwise an IllegalArgumentException will be thrown.
     *
     * @param point
     * @return
     */
    public boolean add(CoordinateIfc point) {
        if (myOrigin == null) {
            throw new IllegalArgumentException("The origin has not been set.");
        }

        if (point == null) {
            throw new IllegalArgumentException("The Coordinate point must not be null.");
        }

        if (myOrigin == point) {
            throw new IllegalArgumentException("Attempted to add the origin to the path, use setOrigin().");
        }

        if (myPoints.contains(point)) {
            throw new IllegalArgumentException("The Coordinate point is already on this path.");
        }

        double d = 0;
        if (myPoints.isEmpty()) {
            d = mySpatialModel.distance(myOrigin, point);
        } else {
            CoordinateIfc lastpt = myPoints.get(myPoints.size() - 1);
            d = mySpatialModel.distance(lastpt, point);
        }
        myTotalDistance = myTotalDistance + d;
        return myPoints.add(point);
    }

    /**
     * Clears the underlying list, sets the origin to null and resets the length
     * of the path to zero
     *
     */
    public final void clear() {
        myTotalDistance = 0.0;
        myOrigin = null;
        myPoints.clear();
    }

    /**
     * Checks if the given Vector2D is in the path
     *
     * @param arg0
     * @return
     */
    public boolean contains(CoordinateIfc arg0) {
        return myPoints.contains(arg0);
    }

    /**
     * Checks whether the path is empty
     *
     * @return
     */
    public final boolean isEmpty() {
        return myPoints.isEmpty();
    }

    /**
     * Returns an iterator over the path
     *
     * @return
     */
    public final Iterator<CoordinateIfc> iterator() {
        return myPoints.iterator();
    }

    /**
     * Returns a ListIterator over the path
     *
     * @return
     */
    public final ListIterator<CoordinateIfc> listIterator() {
        return myPoints.listIterator();
    }

    /**
     * The number of points in the path.
     *
     * @return
     */
    public final int size() {
        return myPoints.size();
    }

    /**
     * Converts the paths to an array
     *
     * @param arg0
     * @return
     */
    public final CoordinateIfc[] toArray(CoordinateIfc[] arg0) {
        return myPoints.toArray(arg0);
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
     */
    public String toString() {
        return myPoints.toString();
    }

}
