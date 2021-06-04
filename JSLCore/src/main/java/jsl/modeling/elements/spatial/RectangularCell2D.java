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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jsl.simulation.ModelElement;
import jsl.utilities.JSLArrayUtil;

/**
 * Represents a basic unit of a RectangularGridSpatialModel
 *
 * @author rossetti
 */
public class RectangularCell2D {

    private final int myRow;

    private final int myCol;

    private final double myWidth;

    private final double myHeight;

    private final Rectangle2D myRectangle;

    private boolean myAvailableFlag;

    /**
     * A cell must be inside a grid, this provides a reference to the cell's
     * containing grid
     */
    private final RectangularGridSpatialModel2D myParentRectangularGrid2D;

    private final List<SpatialElementIfc> mySpatialElements;

    RectangularCell2D(RectangularGridSpatialModel2D grid, int row, int col) {
        if (grid == null) {
            throw new IllegalArgumentException("The grid must not be null");
        }
        myParentRectangularGrid2D = grid;
        myRow = row;
        myCol = col;
        myWidth = grid.getCellWidth();
        myHeight = grid.getCellHeight();
        // create and set the rectangle
        Point2D[][] points = grid.getPoints();
        myRectangle = new Rectangle2D.Double(points[row][col].getX(), points[row][col].getY(), myWidth, myHeight);
        mySpatialElements = new LinkedList<>();
        myAvailableFlag = false;
    }

    /** The row major index for this cell, based on getRowMajorIndex() of
     *  RectangularGridSpatialModel2D
     * 
     * @return the index
     */
    public final int getRowMajorIndex(){
        return myParentRectangularGrid2D.getRowMajorIndex(myRow, myCol);
    }
    
    /**
     * Can be used to check if the cell is available or not. For example, this
     * can be used to see if the cell is available for traversal.
     *
     * @return true means available
     */
    public final boolean isAvailable() {
        return myAvailableFlag;
    }

    /**
     * Can be used to check if the cell is available or not. For example, this
     * can be used to see if the cell is available for traversal.
     *
     * @param flag true means available
     */
    public final void setAvailability(boolean flag) {
        myAvailableFlag = flag;
    }

    public String getRowColName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cell(");
        sb.append(getRowIndex());
        sb.append(",");
        sb.append(getColumnIndex());
        sb.append(")");
        return sb.toString();
    }

    /**
     *
     * @return the number of spatial elements in the cell
     */
    public final int getNumSpatialElements() {
        return mySpatialElements.size();
    }

    /**
     * Gets a list of elements of the target class that are in the cell
     *
     * @param <T> the type
     * @param targetClass the class type
     * @return the list
     */
    public final <T> List<T> getElements(Class<T> targetClass) {
//        List objects = (List) mySpatialElements;
//        return JSLMath.getElements(objects, targetClass);
        return JSLArrayUtil.getElements(mySpatialElements, targetClass);
    }

    /** Gets a list of model elements of the target class that are in the cell
     *  This uses getModelElements() as the basis for the search
     * @param <T> the type
     * @param targetClass the class type
     * @return the list
     */
    public final <T> List<T> getModelElementsOfType(Class<T> targetClass) {
        return JSLArrayUtil.getElements(getModelElements(), targetClass);
    }

    /**
     * Counts the number of ModelElements of the provided class type that are in
     * the cell. Use X.class for the search, where X is a valid class name.
     *
     * @param targetClass
     * @return the count
     */
    public final int countModelElements(Class targetClass) {
        return JSLArrayUtil.countElements(getModelElements(), targetClass);
    }

    /**
     * Counts the number of SpatialElements of the provided class type that are
     * in the cell. Use X.class for the search, where X is a valid class name.
     *
     * @param targetClass
     * @return the count
     */
    public final int countSpatialElements(Class targetClass) {
        return JSLArrayUtil.countElements(mySpatialElements, targetClass);
    }

    /**
     * Returns a list of the ModelElements attached to any spatial elements
     * within the cell.
     *
     * @return a list of the ModelElements attached to any spatial elements
     * within the cell
     */
    public final List<ModelElement> getModelElements() {
        List<ModelElement> list = new ArrayList<>();
        for (SpatialElementIfc se : mySpatialElements) {
            if (se.getModelElement() != null) {
                list.add(se.getModelElement());
            }
        }
        return list;
    }

    /**
     * Returns an unmodifiable view of the spatial elements in this cell The
     * list is unmodifiable, i.e. you can't change the list, but you can still
     * change the elements in the list. WARNING: Don't change the elements
     * unless you really know what you are doing.
     *
     * @return
     */
    public final List<SpatialElementIfc> getUnmodifiableSpatialElements() {
        return (Collections.unmodifiableList(mySpatialElements));
    }

    /**
     * This is a copy. The underlying list of spatial elements might change
     *
     * @return a copy of the current list of spatial elements
     */
    public final List<SpatialElementIfc> getListOfSpatialElements() {
        List<SpatialElementIfc> list = new ArrayList<>(mySpatialElements);
        return list;
    }

    /**
     * Returns an array containing the cells associated with the 1st Moore
     * neighborhood for the cell.
     *
     * @param neighborhood
     */
    public final void getMooreNeighborhood(RectangularCell2D[][] neighborhood) {
        myParentRectangularGrid2D.getMooreNeighborhood(this, neighborhood);
    }

    /**
     * Converts the cell to a string
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Cell\n");
        s.append("row = ").append(myRow).append(" : ");
        s.append("column = ").append(myCol).append(System.lineSeparator());
        s.append("width = ").append(myWidth).append(" : ");
        s.append("height = ").append(myHeight).append(System.lineSeparator());
        s.append("minimum x = ").append(getMinX()).append(" : ");
        s.append("maximum x = ").append(getMaxX()).append(System.lineSeparator());
        s.append("center x = ").append(getCenterX()).append(" : ");
        s.append("center y = ").append(getCenterY()).append(System.lineSeparator());
        s.append("minimum y = ").append(getMinY()).append(" : ");
        s.append("maximum y = ").append(getMaxY()).append(System.lineSeparator());
        s.append("UL coordinate : ").append(getUpperLeftCoordinate()).append(System.lineSeparator());
        s.append("UR coordinate : ").append(getUpperRightCoordinate()).append(System.lineSeparator());
        s.append("Center coordinate : ").append(getCenterCoordinate()).append(System.lineSeparator());
        s.append("LL coordinate : ").append(getLowerLeftCoordinate()).append(System.lineSeparator());
        s.append("LR coordinate : ").append(getLowerRightCoordinate()).append(System.lineSeparator());
        s.append("Availability : ").append(isAvailable()).append(System.lineSeparator());
        s.append("Spatial elements in the cell: ");
        if (mySpatialElements.isEmpty()) {
            s.append("NONE");
        }
        s.append(System.lineSeparator());
        for (SpatialElementIfc se : mySpatialElements) {
            s.append(se);
            s.append(System.lineSeparator());
        }
        s.append(System.lineSeparator());
        return (s.toString());
    }

    /**
     * Returns the RectangularGridSpatialModel2D that contains this cell
     *
     * @return
     */
    public final RectangularGridSpatialModel2D getParentRectangularGrid2D() {
        return myParentRectangularGrid2D;
    }

    /**
     * @return Returns the column index
     */
    public final int getColumnIndex() {
        return myCol;
    }

    /**
     * @return Returns the height of the cell
     */
    public final double getHeight() {
        return myHeight;
    }

    /**
     * @return Returns the cell's Row Index.
     */
    public final int getRowIndex() {
        return myRow;
    }

    /**
     * @return Returns the cell's Width.
     */
    public final double getWidth() {
        return myWidth;
    }

    /**
     * Checks if x and y are in this cell
     *
     * @param x
     * @param y
     * @return
     */
    public final boolean contains(double x, double y) {
        return myRectangle.contains(x, y);
    }

    public final CoordinateIfc getUpperLeftCoordinate() {
        return myParentRectangularGrid2D.getCoordinate(getX(), getY());
    }

    public final CoordinateIfc getCenterCoordinate() {
        return myParentRectangularGrid2D.getCoordinate(getCenterX(), getCenterY());
    }

    public final CoordinateIfc getUpperRightCoordinate() {
        double x = getX() + getWidth();
        double y = getY();
        return myParentRectangularGrid2D.getCoordinate(x, y);
    }

    public final CoordinateIfc getLowerLeftCoordinate() {
        double x = getX();
        double y = getY() + getHeight();
        return myParentRectangularGrid2D.getCoordinate(x, y);
    }

    public final CoordinateIfc getLowerRightCoordinate() {
        double x = getX() + getWidth();
        double y = getY() + getHeight();
        return myParentRectangularGrid2D.getCoordinate(x, y);
    }

    /**
     * The x-coordinate of the upper left corner of the rectangle for the cell
     *
     * @return
     */
    public final double getX() {
        return (myRectangle.getX());
    }

    /**
     * The y-coordinate of the upper left corner of the rectangle for the cell
     *
     * @return
     */
    public final double getY() {
        return (myRectangle.getY());
    }

    /**
     * The x-coordinate of the center of the cell
     *
     * @return
     */
    public final double getCenterX() {
        return myRectangle.getCenterX();
    }

    /**
     * The y-coordinate of the center of the cell
     *
     * @return
     */
    public final double getCenterY() {
        return myRectangle.getCenterY();
    }

    /**
     * The x-coordinate of the maximum x still within the cell
     *
     * @return
     */
    public final double getMaxX() {
        return myRectangle.getMaxX();
    }

    /**
     * The y-coordinate of the maximum y still within the cell
     *
     * @return
     */
    public final double getMaxY() {
        return myRectangle.getMaxY();
    }

    /**
     * The x-coordinate of the minimum x still within the cell
     *
     * @return
     */
    public final double getMinX() {
        return myRectangle.getMinX();
    }

    /**
     * The y-coordinate of the minimum y still within the cell
     *
     * @return
     */
    public final double getMinY() {
        return myRectangle.getMinY();
    }

    /**
     * Add the spatial element to the cell
     *
     * @param element
     */
    protected void addSpatialElement(SpatialElementIfc element) {
//        System.out.println("In RectangularCell2D: addSpatialElement()");
//        System.out.println("Adding Element: " + element);
        mySpatialElements.add(element);
    }

    /**
     * Removes the spatial element from the cell
     *
     * @param element
     * @return
     */
    protected boolean removeSpatialElement(SpatialElementIfc element) {
//        System.out.println("In RectangularCell2D removeSpatialElement");
//        System.out.println("Element: " + element);
        return (mySpatialElements.remove(element));
    }
}
