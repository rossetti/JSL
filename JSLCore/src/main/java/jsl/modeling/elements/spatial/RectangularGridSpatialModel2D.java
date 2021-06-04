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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import jsl.simulation.Model;
import jsl.utilities.math.JSLMath;

/**
 *
 */
public class RectangularGridSpatialModel2D extends SpatialModel {

    /**
     * An "enum" to indicate that a element has been added when notifying
     * observers
     */
    public static final int CELL_CHANGED = Model.getNextEnumConstant();
    /**
     * The upper left corner point for the grid
     */
    private Point2D myUpperLeftCornerPt;

    /**
     * The lower left corner point for the grid
     */
    private Point2D myLowerLeftCornerPt;

    /**
     * The upper right corner point for the grid
     */
    private Point2D myUpperRightCornerPt;

    /**
     * The lower right corner point for the grid
     */
    private Point2D myLowerRightCornerPt;

    /**
     * The line at the top of the grid
     */
    private Line2D myTopLine;

    /**
     * The line at the bottom of the grid
     */
    private Line2D myBottomLine;

    /**
     * The line at the right side of the grid
     */
    private Line2D myRightLine;

    /**
     * The line at the left side of the grid
     */
    private Line2D myLeftLine;

    /**
     * The width of the grid in user dimensions
     */
    private double myWidth;

    /**
     * The height of the grid in user dimensions
     */
    private double myHeight;

    /**
     * The number of rows in the grid, the grid is zero based (the first row is
     * the 0th row)
     */
    private int myNumRows;

    /**
     * The number of columns in the grid, the grid is zero based (the first
     * column is the 0th column)
     */
    private int myNumCols;

    /**
     * The width of the cells in the grid
     */
    private double myCellWidth;

    /**
     * The height of the cells in the grid
     */
    private double myCellHeight;

    /**
     * An 2-d array of points forming the grid, point[0][0] = left upper corner
     * point
     */
    private Point2D[][] myPoints;

    /**
     * An 2-d array of the horizontal line segments in the grid hline[0][0] =
     * point[0][0] -- point[0][1] and so forth
     */
    private Line2D[][] myHorzLines;

    /**
     * An 2-d array of the vertical line segments in the grid vline[0][0] =
     * point[0][0] -- point[1][0] and so forth
     */
    private Line2D[][] myVertLines;

    /**
     * An 2-d array of the cells forming the grid cell[0][0] = upper left most
     * cell with left corner point point[0][0]
     */
    private RectangularCell2D[][] myCells;

    /**
     * A List of the cells in the grid formed row by row
     */
    private List<RectangularCell2D> myCellList;

    /**
     * A reference to the rectangle that forms the outer edge of the grid
     */
    private Rectangle2D myOuterRectangle;

    /**
     * A reference to the path that forms the grid including all line segments
     */
    private GeneralPath myPath;

    /**
     * Creates a grid in the 2D plane. The grid is based on the standard user
     * coordinate system, with (x,y) = (0,0) being the upper left most corner
     * point, with the x-axis going from left to right and the y-axis going from
     * the top down Default values name = use default assigned name x = 0.0 The
     * x coordinate of the upper left most corner point y = 0.0 The y coordinate
     * of the upper left most corner point width = Double.MAX_VALUE The width
     * (along the x-axis) of the grid height = Double.MAX_VALUE The height
     * (along the y-axis) of the grid numRows = 1 The number of rows in the grid
     * (0-based) numCols = 1 The number of columns in the grid (0-based)
     *
     */
    public RectangularGridSpatialModel2D() {
        this(null, 0.0, 0.0, Double.MAX_VALUE, Double.MAX_VALUE, 1, 1);
    }

    /**
     * Creates a grid in the 2D plane. The grid is based on the standard user
     * coordinate system, with (x,y) = (0,0) being the upper left most corner
     * point, with the x-axis going from left to right and the y-axis going from
     * the top down Default values x = 0.0 The x coordinate of the upper left
     * most corner point y = 0.0 The y coordinate of the upper left most corner
     * point width = Double.MAX_VALUE The width (along the x-axis) of the grid
     * height = Double.MAX_VALUE The height (along the y-axis) of the grid
     * numRows = 1 The number of rows in the grid (0-based) numCols = 1 The
     * number of columns in the grid (0-based)
     *
     * @param name The name of the spatial model
     */
    public RectangularGridSpatialModel2D(String name) {
        this(name, 0.0, 0.0, Double.MAX_VALUE, Double.MAX_VALUE, 1, 1);
    }

    /**
     * Creates a grid in the 2D plane. The grid is based on the standard user
     * coordinate system, with (x,y) = (0,0) being the upper left most corner
     * point, with the x-axis going from left to right and the y-axis going from
     * the top down
     *
     * Default values name = use default assigned name x = 0.0 The x coordinate
     * of the upper left most corner point y = 0.0 The y coordinate of the upper
     * left most corner point
     *
     * @param width The width (along the x-axis) of the grid
     * @param height The height (along the y-axis) of the grid
     * @param numRows The number of rows in the grid (0-based)
     * @param numCols The number of columns in the grid (0-based)
     */
    public RectangularGridSpatialModel2D(double width, double height, int numRows, int numCols) {
        this(null, 0.0, 0.0, width, height, numRows, numCols);
    }

    /**
     * Creates a grid in the 2D plane. The grid is based on the standard user
     * coordinate system, with (x,y) = (0,0) being the upper left most corner
     * point, with the x-axis going from left to right and the y-axis going from
     * the top down
     *
     * @param x The x coordinate of the upper left most corner point
     * @param y The y coordinate of the upper left most corner point
     * @param width The width (along the x-axis) of the grid
     * @param height The height (along the y-axis) of the grid
     * @param numRows The number of rows in the grid (0-based)
     * @param numCols The number of columns in the grid (0-based)
     */
    public RectangularGridSpatialModel2D(double x, double y, double width, double height, int numRows, int numCols) {
        this(null, x, y, width, height, numRows, numCols);
    }

    /**
     * Creates a grid in the 2D plane. The grid is based on the standard user
     * coordinate system, with (x,y) = (0,0) being the upper left most corner
     * point, with the x-axis going from left to right and the y-axis going from
     * the top down
     *
     * @param name The name of the spatial model
     * @param x The x coordinate of the upper left most corner point
     * @param y The y coordinate of the upper left most corner point
     * @param width The width (along the x-axis) of the grid
     * @param height The height (along the y-axis) of the grid
     * @param numRows The number of rows in the grid (0-based)
     * @param numCols The number of columns in the grid (0-based)
     */
    public RectangularGridSpatialModel2D(String name, double x, double y, double width, double height, int numRows, int numCols) {
        super(name);
        setWidth(width);
        setHeight(height);
        setGrid(x, y, numRows, numCols);
        myOuterRectangle = new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Checks if the x and y values are in the grid
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if in the grid
     */
    public final boolean contains(double x, double y) {
        double x0 = myOuterRectangle.getX();
        double y0 = myOuterRectangle.getY();
        return (x >= x0
                && y >= y0
                && x <= x0 + getWidth()
                && y <= y0 + getHeight());
        //return myOuterRectangle.contains(x, y, getWidth(), getHeight());
        //return myOuterRectangle.contains(x, y);
    }

    /**
     * Returns the AWT shape representation
     *
     * @return
     */
    public final Shape getShape() {
        return myPath;
    }

    /**
     * @return Returns the cell width.
     */
    public final double getCellWidth() {
        return myCellWidth;
    }

    /**
     * @return Returns the cell height.
     */
    public final double getCellHeight() {
        return myCellHeight;
    }

    /**
     * @return Returns the number of columns.
     */
    public final int getNumColumns() {
        return myNumCols;
    }

    /**
     * @return Returns the number of rows
     */
    public final int getNumRows() {
        return myNumRows;
    }

    /**
     * @return Returns the height.
     */
    public final double getHeight() {
        return myHeight;
    }

    /**
     * @return Returns the width.
     */
    public final double getWidth() {
        return myWidth;
    }

    /**
     * Returns the cell that the element is in or null
     *
     * @param element the element
     * @return the cell or null
     */
    public final RectangularCell2D getCell(SpatialElement element) {
        if (contains(element)) {
            CoordinateIfc c = element.getPosition();
            double x = c.getX1();
            double y = c.getX2();
            return (getCell(x, y));
        } else {
            return (null);
        }
    }

    /**
     * Returns the number of spatial elements in the cell that the element is in
     *
     * @param element the element to check
     * @return zero if element is not in cell or the number
     */
    public final int getNumSpatialElementInCell(SpatialElement element) {
        RectangularCell2D cell = getCell(element);
        if (cell == null) {
            return 0;
        }
        return cell.getNumSpatialElements();
    }

    /**
     * Returns the number of spatial elements in the cell that the element is in
     *
     * @param coordinate the coordinate to check
     * @return zero if element is not in cell or the number
     */
    public final int getNumSpatialElementInCell(CoordinateIfc coordinate) {
        RectangularCell2D cell = getCell(coordinate);
        if (cell == null) {
            return 0;
        }
        return cell.getNumSpatialElements();
    }

    /**
     * Returns the number of spatial elements in the cell that the element is in
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return zero if element is not in cell or the number
     */
    public final int getNumSpatialElementInCell(double x, double y) {
        RectangularCell2D cell = getCell(x, y);
        if (cell == null) {
            return 0;
        }
        return cell.getNumSpatialElements();
    }

    /**
     * Gets a list of spatial elements that are in the cell with the supplied
     * element
     *
     * @param element the element
     * @return the list
     */
    public final List<SpatialElementIfc> getSpatialElementsInCell(SpatialElement element) {
        RectangularCell2D cell = getCell(element);
        if (cell == null) {
            return new ArrayList<>();
        }
        return cell.getListOfSpatialElements();
    }

    /**
     * Gets a list of spatial elements that are in the cell with the supplied
     * element
     *
     * @param coordinate the coordinate
     * @return the list
     */
    public final List<SpatialElementIfc> getSpatialElementsInCell(CoordinateIfc coordinate) {
        RectangularCell2D cell = getCell(coordinate);
        if (cell == null) {
            return new ArrayList<>();
        }
        return cell.getListOfSpatialElements();
    }

    /**
     * Gets a list of spatial elements that are in the cell with the supplied
     * element
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the list
     */
    public final List<SpatialElementIfc> getSpatialElementsInCell(double x, double y) {
        RectangularCell2D cell = getCell(x, y);
        if (cell == null) {
            return new ArrayList<>();
        }
        return cell.getListOfSpatialElements();
    }

    /**
     * Returns the cell that the coordinate is in or null
     *
     * @param coordinate
     * @return the cell or null
     */
    public final RectangularCell2D getCell(CoordinateIfc coordinate) {
        return getCell(coordinate.getX1(), coordinate.getX2());
    }

    /**
     * The cell that contains this x,y coordinate or null if no cell
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the cell or null
     */
    public final RectangularCell2D getCell(double x, double y) {
        if (!contains(x, y)) {
            return null;
        }
        int col = (int) (x / myCellWidth);
        int row = (int) (y / myCellHeight);
        return getCell(row, col);
    }

    /**
     * The cell at this row, col
     *
     * @param row the row
     * @param col the column
     * @return the cell or null
     */
    public final RectangularCell2D getCell(int row, int col) {

        if ((row < 0) || (row >= myNumRows)) {
            return null;
        }

        if ((col < 0) || (col >= myNumCols)) {
            return null;
        }

        return (myCells[row][col]);
    }

    /** The row major index is row(number of columns) + col + 1
     *  Labeling starts at 1 and goes by row (across columns). For example
     *  for a 3 by 3 grid<p>
     *  [1, 2, 3]<p>
     *  [4, 5, 6]<p>
     *  [7, 8, 9]<p>
     * 
     * @param row the row
     * @param col the column
     * @return the row major index of the cell
     */
    public final int getRowMajorIndex(int row, int col) {
        if ((row < 0) || (row >= myNumRows)) {
            throw new IllegalArgumentException("row was < 0 or >= #rows");
        }

        if ((col < 0) || (col >= myNumCols)) {
            throw new IllegalArgumentException("col was < 0 or >= #cols");
        }
        return row*myNumCols + col + 1;
    }

    /**
     * An iterator to the cells in the grid. The cells are accesses by rows
     * (row, col): (0,0), then (0,1), etc 0th row first,
     *
     * @return an iterator over the cells in the grid
     */
    public final Iterator<RectangularCell2D> getCellIterator() {
        return (myCellList.iterator());
    }

    /**
     *
     * @return an unmodifiable list of the cells
     */
    public final List<RectangularCell2D> getCellsAsList() {
        return (Collections.unmodifiableList(myCellList));
    }

    /**
     * Finds the cell that has the least number of spatial elements
     *
     * @param cells the cells to search
     * @return the minimum cell or null
     */
    public static final RectangularCell2D findCellWithMinimumElements(List<RectangularCell2D> cells) {
        int min = Integer.MAX_VALUE;
        RectangularCell2D minCell = null;
        for (RectangularCell2D cell : cells) {
            if (cell.getNumSpatialElements() < min) {
                min = cell.getNumSpatialElements();
                minCell = cell;
            }
        }
        return minCell;
    }

    /**
     * Across all the cells, what is the minimum number of elements in cells
     *
     * @param cells cells to search
     * @return the minimum
     */
    public static final int findMinimumNumberOfElements(List<RectangularCell2D> cells) {
        int min = Integer.MAX_VALUE;
        for (RectangularCell2D cell : cells) {
            if (cell.getNumSpatialElements() < min) {
                min = cell.getNumSpatialElements();
            }
        }
        return min;
    }

    /**
     * Across all the cells, what is the maximum number of elements in cells
     *
     * @param cells cells to search
     * @return the maximum
     */
    public static final int findMaximumNumberOfElements(List<RectangularCell2D> cells) {
        int max = Integer.MIN_VALUE;
        for (RectangularCell2D cell : cells) {
            if (cell.getNumSpatialElements() > max) {
                max = cell.getNumSpatialElements();
            }
        }
        return max;
    }

    /**
     * Across all the cells, which cells have the minimum number of elements in
     * cells
     *
     * @param cells the cells to search
     * @return a list of cells that have the minimum number of elements
     */
    public static final List<RectangularCell2D> findCellsWithMinimumElements(List<RectangularCell2D> cells) {
        List<RectangularCell2D> list = new ArrayList<>();
        int min = findMinimumNumberOfElements(cells);
        for (RectangularCell2D cell : cells) {
            if (cell.getNumSpatialElements() == min) {
                list.add(cell);
            }
        }
        return list;
    }

    /**
     * Across all the cells, which cells have the maximum number of elements in
     * cells
     *
     * @param cells the cells to search
     * @return a list of cells that have the maximum number of elements
     */
    public static final List<RectangularCell2D> findCellsWithMaximumElements(List<RectangularCell2D> cells) {
        List<RectangularCell2D> list = new ArrayList<>();
        int max = findMaximumNumberOfElements(cells);
        for (RectangularCell2D cell : cells) {
            if (cell.getNumSpatialElements() == max) {
                list.add(cell);
            }
        }
        return list;
    }

    /**
     * A comparator based on the number of elements in the cell
     *
     * @return A comparator based on the number of elements in the cell
     */
    public static NumElementsComparator getNumElementsComparator() {
        return new NumElementsComparator();
    }

    /**
     * Returns an array of the cells sorted from smallest to largest based on
     * the number of spacial elements in the cells
     *
     * @param cells the cells to sort
     * @return a new list of the sorted cells
     */
    public static final List<RectangularCell2D> sortCellsByNumElements(List<RectangularCell2D> cells) {
        ArrayList<RectangularCell2D> list = new ArrayList<>(cells);
        Collections.sort(list, getNumElementsComparator());
        return list;
    }

    /**
     * Finds the cell that has the least number of spatial elements
     *
     * @param coreCell the core cell
     * @param includeCore true includes the core in the list, false does not
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(
            RectangularCell2D coreCell, boolean includeCore) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(coreCell, includeCore));
    }

    /**
     * Finds the cell that has the least number of spatial elements. The core
     * cell is not included in the list
     *
     * @param coreCell the core cell
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(
            RectangularCell2D coreCell) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(coreCell, false));
    }

    /**
     * Finds the cell that has the least number of spatial elements
     *
     * @param coordinate the coordinate in a cell
     * @param includeCore true includes the core in the list, false does not
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(
            CoordinateIfc coordinate, boolean includeCore) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(coordinate, includeCore));
    }

    /**
     * Finds the cell that has the least number of spatial elements. The core
     * cell is not include in the search
     *
     * @param coordinate the coordinate in a cell
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(
            CoordinateIfc coordinate) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(coordinate, false));
    }

    /**
     * Finds the cell that has the least number of spatial elements
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param includeCore true includes the core in the list, false does not
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(double x,
            double y, boolean includeCore) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(x, y, includeCore));
    }

    /**
     * Finds the cell that has the least number of spatial elements. The core
     * cell is not included in the search
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the minimum cell or null
     */
    public final RectangularCell2D findCellWithMinimumElementsInNeighborhood(double x,
            double y) {
        return findCellWithMinimumElements(getMooreNeighborhoodAsList(x, y, false));
    }

    /**
     * Returns an array containing the 1st Moore neighborhood for the cell in
     * the grid
     *
     * @param coreCell the core cell in the neighborhood
     * @return the neighborhood array
     */
    public final RectangularCell2D[][] getMooreNeighborhood(RectangularCell2D coreCell) {
        RectangularCell2D[][] neighborhood = new RectangularCell2D[3][3];
        getMooreNeighborhood(coreCell, neighborhood);
        return (neighborhood);
    }

    /**
     * Returns an array containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid
     *
     * @param coordinate the coordinate
     * @return the neighborhood array
     */
    public final RectangularCell2D[][] getMooreNeighborhood(CoordinateIfc coordinate) {
        return (getMooreNeighborhood(coordinate.getX1(), coordinate.getX2()));
    }

    /**
     * Returns an array containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid
     *
     * @param element the element
     * @return the neighborhood array
     */
    public final RectangularCell2D[][] getMooreNeighborhood(SpatialElementIfc element) {
        return (getMooreNeighborhood(element.getPosition()));
    }

    /**
     * Returns an array containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the neighborhood array
     */
    public final RectangularCell2D[][] getMooreNeighborhood(double x, double y) {
        RectangularCell2D coreCell = getCell(x, y);
        RectangularCell2D[][] neighborhood = new RectangularCell2D[3][3];
        getMooreNeighborhood(coreCell, neighborhood);
        return (neighborhood);
    }

    /**
     * Returns an list containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid of the coordinate
     *
     * @param coordinate the coordinate
     * @param includeCore true includes the core in the list, false does not
     * @return the neighborhood array
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(CoordinateIfc coordinate,
            boolean includeCore) {
        return (getMooreNeighborhoodAsList(coordinate.getX1(), coordinate.getX2(), includeCore));
    }

    /**
     * Returns an list containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid of the coordinate. The core cell is not
     * included in the list
     *
     * @param coordinate the coordinate
     * @return the neighborhood array
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(CoordinateIfc coordinate) {
        return (getMooreNeighborhoodAsList(coordinate.getX1(), coordinate.getX2(), false));
    }

    /**
     * Returns an list containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid of the coordinate
     *
     * @param element the element
     * @param includeCore true includes the core in the list, false does not
     * @return the neighborhood array
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(SpatialElementIfc element,
            boolean includeCore) {
        return (getMooreNeighborhoodAsList(element.getPosition(), includeCore));
    }

    /**
     * Returns an list containing the 1st Moore neighborhood for the cell that
     * contains x and y in the grid of the coordinate. The core cell is not
     * included in the list
     *
     * @param element the element
     * @return the neighborhood array
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(SpatialElementIfc element) {
        return (getMooreNeighborhoodAsList(element.getPosition(), false));
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param includeCore true includes the core in the list, false does not
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(double x,
            double y, boolean includeCore) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(x, y), includeCore);
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid. The core cell is not included in the list.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(double x,
            double y) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(x, y), false);
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid
     *
     * @param coreCell the core cell
     * @param includeCore true includes the core in the list, false does not
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(RectangularCell2D coreCell,
            boolean includeCore) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(coreCell), includeCore);
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid. The core cell is not included in the list
     *
     * @param coreCell the core cell
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(RectangularCell2D coreCell) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(coreCell), false);
    }

    /**
     * Returns an array containing the 1st Moore neighborhood for the cell at
     * row, col in the grid
     *
     * @param row the row
     * @param col the column
     * @return the filled array
     */
    public final RectangularCell2D[][] getMooreNeighborhood(int row, int col) {
        RectangularCell2D[][] neighborhood = new RectangularCell2D[3][3];
        getMooreNeighborhood(row, col, neighborhood);
        return (neighborhood);
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid
     *
     * @param row the row
     * @param col the column
     * @param includeCore true includes the core in the list, false does not
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(int row,
            int col, boolean includeCore) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(row, col), includeCore);
    }

    /**
     * Returns a list containing the 1st Moore neighborhood for the cell at row,
     * col in the grid. The core cell is not included in the list.
     *
     * @param row the row
     * @param col the column
     * @return the list
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(int row, int col) {
        return getMooreNeighborhoodAsList(getMooreNeighborhood(row, col), false);
    }

    /**
     * Fills the supplied array with the 1st order Moore neighborhood for the
     * given cell at row, col of the grid
     *
     * @param row the row
     * @param col the column
     * @param neighborhood the array to fill
     */
    public final void getMooreNeighborhood(int row, int col,
            RectangularCell2D[][] neighborhood) {
        getMooreNeighborhood(getCell(row, col), neighborhood);
    }

    /**
     * Copies the non-null cells in the neighborhood into a List
     *
     * @param neighborhood the neighborhood to translate
     * @param includeCore true includes the core in the list, false does not
     * @return the list of cells in the neighborhood
     */
    public final List<RectangularCell2D> getMooreNeighborhoodAsList(
            RectangularCell2D[][] neighborhood, boolean includeCore) {
        if (neighborhood == null) {
            throw new IllegalArgumentException("The neighborhood array was null");
        }

        if (neighborhood.length < 3) {
            throw new IllegalArgumentException("Row size of the array must be 3.");
        }

        for (int i = 0; i < neighborhood.length; i++) {
            if (neighborhood[i].length < 3) {
                throw new IllegalArgumentException("Column size of the array must be 3.");
            }
        }

        List<RectangularCell2D> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (neighborhood[i][j] != null) {
                    if (includeCore == false) {
                        if ((i != 1) || (j != 1)) {
                            // not the core, so add it
                            list.add(neighborhood[i][j]);
                        }
                    } else {
                        list.add(neighborhood[i][j]);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns a default set of coordinates to be used to initialize the
     * location of spatial elements if necessary.
     *
     * @return
     */
    @Override
    public CoordinateIfc getDefaultCoordinate() {
        return (new Vector3D());
    }

    /* (non-Javadoc)
	 * @see jsl.modeling.elements.spatial.SpatialModel#getCoordinate(double, double, double)
     */
    @Override
    public CoordinateIfc getCoordinate(double x1, double x2, double x3) {
        if (!contains(x1, x2)) {
            throw new IllegalArgumentException("The coordinate is not valid for this spatial model!");
        }

        return new Vector3D(x1, x2, x3);
    }

    /**
     * This only checks whether 1st and 2nd (x,y) coordinates are valid, since
     * this is only rectangular 2D grid. The coordinate is valid if it is within
     * the boundary defined by the rectangle for the grid
     *
     * @return
     */
    @Override
    public boolean isValid(CoordinateIfc coordinate) {
        double x = coordinate.getX1();
        double y = coordinate.getX2();
        return contains(x, y);
    }

    @Override
    public double distance(CoordinateIfc fromCoordinate, CoordinateIfc toCoordinate) {
        if (!isValid(fromCoordinate)) {
            throw new IllegalArgumentException("The from coordinate is not valid for this spatial model!");
        }

        if (!isValid(toCoordinate)) {
            throw new IllegalArgumentException("The to coordinate is not valid for this spatial model!");
        }

        double x1 = fromCoordinate.getX1();
        double y1 = fromCoordinate.getX2();
        double x2 = toCoordinate.getX1();
        double y2 = toCoordinate.getX2();
        double dx = x1 - x2;
        double dy = y1 - y2;
        double d = Math.sqrt(dx * dx + dy * dy);
        return d;
    }

    /**
     * Only checks 1st and 2nd coordinates, because of 2D grid
     *
     * @return
     */
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
     * Fills the supplied array with the 1st order Moore neighborhood for the
     * given core cell.
     *
     * set the top row of the neighborhood neighborhood[0][0] = getCell(i-1,
     * j-1) neighborhood[0][1] = getCell(i-1, j) neighborhood[0][2] =
     * getCell(i-1, j+1)
     *
     * set the middle row of the neighborhood neighborhood[1][0] = getCell(i,
     * j-1) neighborhood[1][1] = getCell(i, j) neighborhood[1][2] = getCell(i,
     * j+1)
     *
     * set the bottom row of the neighborhood neighborhood[2][0] =
     * getCell(i+1,j-1) neighborhood[2][1] = getCell(i+1, j) neighborhood[2][2]
     * = getCell(i+1, j+1)
     *
     * @param coreCell
     * @param neighborhood
     */
    public final void getMooreNeighborhood(RectangularCell2D coreCell,
            RectangularCell2D[][] neighborhood) {
        if (neighborhood == null) {
            throw new IllegalArgumentException("The neighborhood array was null");
        }

        if (neighborhood.length < 3) {
            throw new IllegalArgumentException("Row size of the array must be 3.");
        }

        for (int i = 0; i < neighborhood.length; i++) {
            if (neighborhood[i].length < 3) {
                throw new IllegalArgumentException("Column size of the array must be 3.");
            }
        }

        if (coreCell == null) {
            throw new IllegalArgumentException("The core cell was null");
        }

        if (coreCell.getParentRectangularGrid2D() != this) {
            throw new IllegalArgumentException("The core cell is not part of this grid.");
        }

        // nullify the neighborhood
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                neighborhood[i][j] = null;
            }
        }

        // get the core cell's indices
        int i = coreCell.getRowIndex();
        int j = coreCell.getColumnIndex();

        // set the top row of the neighborhood
        neighborhood[0][0] = getCell(i - 1, j - 1);
        neighborhood[0][1] = getCell(i - 1, j);
        neighborhood[0][2] = getCell(i - 1, j + 1);

        // set the middle row of the neighborhood
        neighborhood[1][0] = getCell(i, j - 1);
        neighborhood[1][1] = getCell(i, j);
        neighborhood[1][2] = getCell(i, j + 1);

        // set the bottom row of the neighborhood
        neighborhood[2][0] = getCell(i + 1, j - 1);
        neighborhood[2][1] = getCell(i + 1, j);
        neighborhood[2][2] = getCell(i + 1, j + 1);

    }

    @Override
    protected void addSpatialElement(SpatialElement element) {
        super.addSpatialElement(element);
        CoordinateIfc c = element.getPosition();
        double x = c.getX1();
        double y = c.getX2();
        RectangularCell2D cell = getCell(x, y);
//        System.out.println("In RectangularGridSpatialModel2D: addSpatialElement()");
//        System.out.println("Adding spatial element: " + element.getName());
//        System.out.println("Putting spatial element in cell:  " + cell.getRowColName());
        cell.addSpatialElement(element);
    }

    /**
     * Removes the spatial element from the spatial model
     *
     * @param element
     * @return
     */
    protected boolean removeSpatialElement(SpatialElement element) {
        boolean found = super.removeSpatialElement(element);
        if (found) {
            CoordinateIfc c = element.getPosition();
            double x = c.getX1();
            double y = c.getX2();
            RectangularCell2D cell = getCell(x, y);
            cell.removeSpatialElement(element);
        }
        return (found);
    }

    /**
     * When the spatial element's position is changed then this method is called
     * to ensure that the spatial model properly tracks the elements position
     *
     */
    @Override
    protected final void updatePosition() {
        SpatialElementIfc element = this.getUpdatingSpatialElement();

        CoordinateIfc p = element.getPreviousPosition();
        double xp = p.getX1();
        double yp = p.getX2();

        CoordinateIfc c = element.getPosition();
        double xc = c.getX1();
        double yc = c.getX2();

        // The element's position has just changed
        // get the previous position's cell
        RectangularCell2D previousCell = getCell(xp, yp);
        // get the cell it is supposed to be in now
        RectangularCell2D nextCell = getCell(xc, yc);
        if (previousCell != nextCell) {// change in cell
            // remove the element from the previous cell
            previousCell.removeSpatialElement(element);
            // add it to the next cell
            nextCell.addSpatialElement(element);
            setObserverState(CELL_CHANGED);
            notifyObservers(this, null);
        }

    }

    /**
     * @param height The height to set.
     */
    protected final void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be > 0");
        }
        myHeight = height;
    }

    /**
     * @param width The width to set.
     */
    protected final void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be > 0");
        }
        myWidth = width;
    }

    /**
     * Sets up the rectangular grid into points, lines, and rectangle Dividing
     * the grid into the appropriate number of rows and columns.
     *
     * @param startX
     * @param startY
     * @param numRows
     * @param numCols
     */
    protected final void setGrid(double startX, double startY, int numRows, int numCols) {
        if (numRows < 1) {
            throw new IllegalArgumentException("The number of rows must be >=1");
        }

        if (numCols < 1) {
            throw new IllegalArgumentException("The number of rows must be >=1");
        }

        if (startX < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }

        if (startY < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }

        myNumRows = numRows;
        myNumCols = numCols;
        myCellWidth = myWidth / numCols;
        myCellHeight = myHeight / numRows;

        // make the points
        myPoints = new Point2D.Double[myNumRows + 1][myNumCols + 1];
        double x = 0.0;
        double y = startY;
        for (int i = 0; i <= myNumRows; i++) {
            x = startX;
            for (int j = 0; j <= myNumCols; j++) {
                myPoints[i][j] = new Point2D.Double(x, y);
                x = x + myCellWidth;
            }
            y = y + myCellHeight;
        }

        // set the outer corner points
        myUpperLeftCornerPt = myPoints[0][0];
        myUpperRightCornerPt = myPoints[0][myNumCols];
        myLowerLeftCornerPt = myPoints[myNumRows][0];
        myLowerRightCornerPt = myPoints[myNumRows][myNumCols];

        // set the outer lines
        myTopLine = new Line2D.Double(myUpperLeftCornerPt, myUpperRightCornerPt);
        myBottomLine = new Line2D.Double(myLowerLeftCornerPt, myLowerRightCornerPt);
        myLeftLine = new Line2D.Double(myUpperLeftCornerPt, myLowerLeftCornerPt);
        myRightLine = new Line2D.Double(myUpperRightCornerPt, myLowerRightCornerPt);

        // make the general path to be built from the points/lines
        myPath = new GeneralPath();
        // set the intial point on the path
        myPath.moveTo((float) myUpperLeftCornerPt.getX(), (float) myUpperLeftCornerPt.getY());

        // make the horizontal line segments
        myHorzLines = new Line2D[myNumRows + 1][myNumCols];

        for (int i = 0; i < myNumRows + 1; i++) {
            for (int j = 0; j < myNumCols; j++) {
                myHorzLines[i][j] = new Line2D.Double(myPoints[i][j], myPoints[i][j + 1]);
                myPath.append(myHorzLines[i][j], false);
            }
        }

        // make the vertical line segments
        myVertLines = new Line2D[myNumRows][myNumCols + 1];

        for (int j = 0; j < myNumCols + 1; j++) {
            for (int i = 0; i < myNumRows; i++) {
                myVertLines[i][j] = new Line2D.Double(myPoints[i][j], myPoints[i + 1][j]);
                myPath.append(myVertLines[i][j], false);
            }
        }

        myPath.closePath();

        // make the cells from the points and lines
        myCells = new RectangularCell2D[myNumRows][myNumCols];
        myCellList = new ArrayList<RectangularCell2D>(myNumRows * myNumCols);

        for (int i = 0; i < myNumRows; i++) {
            for (int j = 0; j < myNumCols; j++) {
                myCells[i][j] = new RectangularCell2D(this, i, j);
                myCellList.add(myCells[i][j]);
            }
        }
    }

    /**
     *
     * @return The points associated with the grid
     */
    final Point2D[][] getPoints() {
        return myPoints;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("Grid \n");
        s.append("width = ").append(myWidth).append(" height = ").append(myHeight).append("\n\n");
        s.append("ULPT = ").append(myUpperLeftCornerPt).append("---->");
        s.append("URPT = ").append(myUpperRightCornerPt).append("\n");
        s.append("LLPT = ").append(myLowerLeftCornerPt).append("---->");
        s.append("LRPT = ").append(myLowerRightCornerPt).append("\n\n");

        s.append("TopLine = ").append(myTopLine.getP1()).append("---->").append(myTopLine.getP2()).append("\n");
        s.append("BottomLine = ").append(myBottomLine.getP1()).append("---->").append(myBottomLine.getP2()).append("\n");
        s.append("LeftLine = ").append(myLeftLine.getP1()).append("---->").append(myLeftLine.getP2()).append("\n");
        s.append("RightLine = ").append(myRightLine.getP1()).append("---->").append(myRightLine.getP2()).append("\n\n");

        s.append("Points \n");
        for (int i = 0; i <= myNumRows; i++) {
            for (int j = 0; j <= myNumCols; j++) {
                s.append("Point[i=").append(i).append("][j=").append(j).append("]= ").append(myPoints[i][j]).append("\n");
            }
        }

        s.append("\nHorizontal lines \n");
        for (int i = 0; i < myNumRows + 1; i++) {
            for (int j = 0; j < myNumCols; j++) {
                s.append(myHorzLines[i][j].getP1()).append("---->").append(myHorzLines[i][j].getP2()).append("\n");
            }
        }

        s.append("\nVertical lines \n");
        for (int j = 0; j < myNumCols + 1; j++) {
            for (int i = 0; i < myNumRows; i++) {
                s.append(myVertLines[i][j].getP1()).append("---->").append(myVertLines[i][j].getP2()).append("\n");
            }
        }

        s.append("\nCells \n");

        for (int i = 0; i < myNumRows; i++) {
            for (int j = 0; j < myNumCols; j++) {
                s.append(myCells[i][j]).append("\n");
            }
        }

        return (s.toString());
    }

}
