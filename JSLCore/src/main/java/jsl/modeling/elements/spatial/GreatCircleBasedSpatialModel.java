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

import jsl.utilities.math.JSLMath;

/**
 * This class represents the distance between two coordinates using the Great
 * Circle Distance. (http://en.wikipedia.org/wiki/Great_circle_distance)
 *
 * Distance is assumed to be in kilometers
 *
 * Coordinates are assumed to be given as: X1 - latitude, in degrees, [-90,90]
 * X2 - longitude, in degrees [-180,180] X3 - not used
 *
 * By convention North is considered positive latitude By convention West is
 * considered negative longitude
 *
 */
public class GreatCircleBasedSpatialModel extends SpatialModel {

    /**
     * Defines the directions on a compass
     *
     */
    public static enum Direction {
        NORTH, SOUTH, EAST, WEST
    };

    /**
     * The ciruity addFactor for road networks, see pg 559 of Ballou
     *
     */
    public static final double ROADS = 1.17;

    /**
     * The ciruity addFactor for rail networks, see pg 559 of Ballou
     *
     */
    public static final double RAIL = 1.20;

    /**
     * The default radius for the earth, @see
     * (http://en.wikipedia.org/wiki/Great_circle_distance)
     *
     */
    protected static final double DEFAULT_EARTH_RADIUS = 6372.795;

    /**
     * Holds teh radius of the earth for calculating great circle distance
     *
     */
    protected double myEarthRadius = DEFAULT_EARTH_RADIUS;

    /**
     * Can be set to adjust computed great circle distance to account for the
     * circuity of the road/rail network, by default 1.0
     *
     */
    protected double myCircuityFactor = 1.0;

    /**
     * Creates a spatial model that uses the great circle distance as the basis
     * for computing distance between two coordinates.
     */
    public GreatCircleBasedSpatialModel() {
        this(null, 1.0);
    }

    /**
     * Creates a spatial model that uses the great circle distance as the basis
     * for computing distance between two coordinates.
     *
     * @param name, An optional name for the spatial model
     */
    public GreatCircleBasedSpatialModel(String name) {
        this(name, 1.0);
    }

    /**
     * Creates a spatial model that uses the great circle distance as the basis
     * for computing distance between two coordinates.
     *
     * @param name, An optional name for the spatial model
     * @param circuityFactor Can be used to adjust distance for network effects
     */
    public GreatCircleBasedSpatialModel(String name, double circuityFactor) {
        super(name);
        setCircuityFactor(circuityFactor);
    }

    /**
     * Returns a valid coordinate
     *
     * @param lat must be between -90 and 90 degrees
     * @param lon must be between -180 and 180 degrees
     * @param x3 not used
     * @return
     */
    public CoordinateIfc getCoordinate(double lat, double lon, double x3) {
        if (Math.abs(lat) > 90.0) {
            throw new IllegalArgumentException("The latitude must be in range [-90, 90] degrees");
        }
        if (Math.abs(lon) > 180.0) {
            throw new IllegalArgumentException("The latitude must be in range [-180, 180] degrees");
        }

        Vector3D v = new Vector3D(lat, lon);
        return (v);
    }

    /* (non-Javadoc)
	 * @see jsl.modeling.elements.spatial.SpatialModel#getDefaultCoordinate()
     */
    @Override
    public CoordinateIfc getDefaultCoordinate() {
        return new Vector3D();
    }

    /**
     * Returns a valid coordinate
     *
     * @param latD direction associated with the latitude
     * @param latd degrees latitude
     * @param latm minutes latitude
     * @param lonD longitude direction
     * @param lond longitude degrees
     * @param lonm longitude minutes
     * @return
     */
    public static final CoordinateIfc getCoordinate(Direction latD, double latd, double latm,
            Direction lonD, double lond, double lonm) {
        return (getCoordinate(latD, latd, latm, 0.0, lonD, lond, lonm, 0.0));
    }

    /**
     * Returns a valid coordinate
     *
     * @param latD latitude direction
     * @param latd latitude degrees
     * @param latm latitude minutes
     * @param lats latitude seconds
     * @param lonD longitude direction
     * @param lond longitude degrees
     * @param lonm longitude minutes
     * @param lons longitude seconds
     * @return
     */
    public static CoordinateIfc getCoordinate(Direction latD, double latd, double latm, double lats,
            Direction lonD, double lond, double lonm, double lons) {
        double lat = getLatitude(latD, latd, latm, lats);
        double lon = getLongitude(lonD, lond, lonm, lons);
        Vector3D v = new Vector3D(lat, lon);
        return (v);
    }

    /**
     * Returns a latitude in degrees
     *
     * @param direction
     * @param degrees
     * @param minutes
     * @return
     */
    public static final double getLatitude(Direction direction, double degrees, double minutes) {
        return (getLatitude(direction, degrees, minutes, 0.0));
    }

    /**
     * Returns a latitude in degrees
     *
     * @param direction
     * @param degrees
     * @param minutes
     * @param seconds
     * @return
     */
    public static double getLatitude(Direction direction, double degrees, double minutes, double seconds) {
        if (direction == Direction.EAST) {
            throw new IllegalArgumentException("The direction supplied was EAST, not valid for latitude");
        }

        if (direction == Direction.WEST) {
            throw new IllegalArgumentException("The direction supplied was WEST, not valid for latitude");
        }

        if (degrees < 0.0) {
            throw new IllegalArgumentException("The degrees must be >= 0.");
        }

        if (degrees > 90.0) {
            throw new IllegalArgumentException("The degrees must be <= 90.");
        }

        if (minutes < 0.0) {
            throw new IllegalArgumentException("The minutes must be >= 0.");
        }

        if (minutes > 60.0) {
            throw new IllegalArgumentException("The minutes must be <= 60.0.");
        }

        if (seconds < 0.0) {
            throw new IllegalArgumentException("The seconds must be >=0.");
        }

        if (seconds > 60.0) {
            throw new IllegalArgumentException("The seconds must be <= 60");
        }

        double sign = 1.0;
        if (direction == Direction.SOUTH) {
            sign = -1.0;
        }
        double lat = degrees + (minutes / 60.0) + (seconds / 3600.0);
        return (sign * lat);
    }

    /**
     * Returns a longitude in degrees
     *
     * @param direction
     * @param degrees
     * @param minutes
     * @return
     */
    public static final double getLongitude(Direction direction, double degrees, double minutes) {
        return (getLongitude(direction, degrees, minutes, 0.0));
    }

    /**
     * Returns a longitude in degrees
     *
     * @param direction
     * @param degrees
     * @param minutes
     * @param seconds
     * @return
     */
    public static double getLongitude(Direction direction, double degrees, double minutes, double seconds) {
        if (direction == Direction.NORTH) {
            throw new IllegalArgumentException("The direction supplied was NORTH, not valid for longitude");
        }

        if (direction == Direction.SOUTH) {
            throw new IllegalArgumentException("The direction supplied was SOUTH, not valid for longitude");
        }

        if (degrees < 0.0) {
            throw new IllegalArgumentException("The degrees must be >= 0.");
        }

        if (degrees > 180.0) {
            throw new IllegalArgumentException("The degrees must be <= 180.");
        }

        if (minutes < 0.0) {
            throw new IllegalArgumentException("The minutes must be >= 0.");
        }

        if (minutes > 60.0) {
            throw new IllegalArgumentException("The minutes must be <= 60.0.");
        }

        if (seconds < 0.0) {
            throw new IllegalArgumentException("The seconds must be >=0.");
        }

        if (seconds > 60.0) {
            throw new IllegalArgumentException("The seconds must be <= 60");
        }

        double sign = 1.0;
        if (direction == Direction.WEST) {
            sign = -1.0;
        }
        double lon = degrees + (minutes / 60.0) + (seconds / 3600.0);
        return (sign * lon);
    }

    /**
     * Gets the sign associated with the supplied direction
     *
     * @param d
     * @return
     */
    public static final double getDirectionSign(Direction d) {

        switch (d) {
            case NORTH:
                return (1.0);
            case SOUTH:
                return (-1.0);
            case EAST:
                return (1.0);
            case WEST:
                return (-1.0);
            default:
                return (1.0);
        }

    }

    @Override
    public boolean isValid(CoordinateIfc coordinate) {
        double lat = coordinate.getX1();
        double lon = coordinate.getX2();

        if (Math.abs(lat) > 90.0) {
            return false;
        }
        if (Math.abs(lon) > 180.0) {
            return false;
        }

        return true;
    }

    @Override
    public double distance(CoordinateIfc fromCoordinate, CoordinateIfc toCoordinate) {
        if (!isValid(fromCoordinate)) {
            throw new IllegalArgumentException("The from coordinate is not valid for this spatial model!");
        }

        if (!isValid(toCoordinate)) {
            throw new IllegalArgumentException("The to coordinate is not valid for this spatial model!");
        }

        double lat1 = Math.toRadians(fromCoordinate.getX1());
        double lon1 = Math.toRadians(fromCoordinate.getX2());
        double lat2 = Math.toRadians(toCoordinate.getX1());
        double lon2 = Math.toRadians(toCoordinate.getX2());

        double lonDiff = lon1 - lon2;
        double cosLat2 = Math.cos(lat2);
        double sinLonDiff = Math.sin(lonDiff);
        double cosLat1 = Math.cos(lat1);
        double sinLat2 = Math.sin(lat2);
        double sinLat1 = Math.sin(lat1);
        double cosLonDiff = Math.cos(lonDiff);

        double n1 = (cosLat2 * sinLonDiff);
        double n2 = cosLat1 * sinLat2 - sinLat1 * cosLat2 * cosLonDiff;
        double n = Math.sqrt(n1 * n1 + n2 * n2);
        double d = sinLat1 * sinLat2 + cosLat1 * cosLat2 * cosLonDiff;

        double angDiff = Math.atan2(n, d);

        return (myCircuityFactor * getEarthRadius() * angDiff);

    }

    @Override
    public boolean comparePositions(CoordinateIfc coordinate1, CoordinateIfc coordinate2) {
        if (!isValid(coordinate1)) {
            throw new IllegalArgumentException("The coordinate 1 is not valid for this spatial model!");
        }

        if (!isValid(coordinate2)) {
            throw new IllegalArgumentException("The coordinate 2 is not valid for this spatial model!");
        }

        double x1 = coordinate1.getX1();
        double y1 = coordinate1.getX2();
        double x2 = coordinate2.getX1();
        double y2 = coordinate2.getX2();
        boolean b1 = JSLMath.equal(x1, x2, myDefaultPositionPrecision);
        boolean b2 = JSLMath.equal(y1, y2, myDefaultPositionPrecision);
        return (b1 && b2);
    }

    /**
     * The earth's radius for great circle calculation
     *
     * @return Returns the earth's Radius.
     */
    public final double getEarthRadius() {
        return myEarthRadius;
    }

    /**
     * The earth's radius for great circle calculation
     *
     * @param radius The radius for the earth to set.
     */
    public final void setEarthRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("The earth radius must be > 0!");
        }
        myEarthRadius = radius;
    }

    /**
     * The network circuity addFactor for adjusting the distance.
     *
     * @return Returns the circuityFactor.
     */
    public final double getCircuityFactor() {
        return myCircuityFactor;
    }

    /**
     * @param circuityFactor The circuityFactor to set.
     */
    protected final void setCircuityFactor(double circuityFactor) {
        if (circuityFactor <= 0) {
            throw new IllegalArgumentException("The circuity addFactor must be > 0!");
        }

        myCircuityFactor = circuityFactor;
    }

    public static void main(String[] args) {

        GreatCircleBasedSpatialModel m = new GreatCircleBasedSpatialModel();

        CoordinateIfc bna = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 36.0, 7.2, 0.0, Direction.WEST, 86.0, 40.2, 0.0);

        CoordinateIfc lax = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 33.0, 56.4, 0.0, Direction.WEST, 118.0, 24.0, 0.0);

        double d = m.distance(bna, lax);
        System.out.println("Distance from bna to lax is " + d);

        CoordinateIfc london = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 51.0, 30.0, 0.0, Direction.WEST, 0.0, 7.0, 0.0);

        CoordinateIfc boston = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 42.0, 21.0, 0.0, Direction.WEST, 71.0, 4.0, 0.0);

        double d2 = m.distance(boston, london);
        System.out.println("Distance from Boston to London is " + d2);

        CoordinateIfc madrid = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 40.24, 0.0, 0.0, Direction.WEST, 3.41, 0.0, 0.0);

        CoordinateIfc milan = GreatCircleBasedSpatialModel.getCoordinate(Direction.NORTH, 45.28, 0.0, 0.0, Direction.EAST, 9.12, 0.0, 0.0);

        double d3 = m.distance(madrid, milan);
        System.out.println("Distance from Madrid to Milanis " + d3);

    }
}
