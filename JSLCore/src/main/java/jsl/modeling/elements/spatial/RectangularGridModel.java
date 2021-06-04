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
import jsl.modeling.elements.variable.RandomVariable;
import jsl.observers.ObserverIfc;
import jsl.utilities.random.rvariable.UniformRV;

import java.util.List;

/**
 * This class can be used to hold ModelElements that can be in a
 * RectangularGridSpatialModel2D. This defines a model element with the ability
 * to schedule that has a RectangularGridSpatialModel2D spatial model.
 * Sub-classes can builder on this.
 *
 * @author rossetti
 */
public class RectangularGridModel extends SchedulingElement {

    protected RectangularGridSpatialModel2D myGrid;

    protected RandomVariable myUniformRV;

    /**
     * Can be used by sub-classes to redefine the grid ULHC x = 0.0, ULHC y =
     * 0.0
     *
     * @param parent the parent model element
     * @param width the width of the grid
     * @param height the height of the grid
     * @param numRows the number of rows in the grid
     * @param numCols the number of columns in the grid
     */
    public RectangularGridModel(ModelElement parent, double width, double height,
            int numRows, int numCols) {
        this(parent, 0, 0, width, height, numRows, numCols, null);
    }

    /**
     * Can be used by sub-classes to redefine the grid
     *
     * @param parent the parent model element
     * @param x the ULHC x
     * @param y the ULHC y
     * @param width the width of the grid
     * @param height the height of the grid
     * @param numRows the number of rows in the grid
     * @param numCols the number of columns in the grid
     * @param name the name of the model element
     */
    public RectangularGridModel(ModelElement parent, double x, double y,
            double width, double height, int numRows, int numCols, String name) {
        super(parent, name);
        setGrid(x, y, width, height, numRows, numCols);
        myUniformRV = new RandomVariable(this, new UniformRV());
        getGrid().addObserver(new PositionObserver());
    }

    /**
     * Randomly generates an integer between i and j
     *
     * @param i the lower limit
     * @param j the upper limit
     * @return the random integer
     */
    public final int randInt(int i, int j) {
        if (i > j) {
            throw new IllegalArgumentException("The lower limit must be <= the upper limit");
        }
        return (i + (int) (myUniformRV.getValue() * (j - i + 1)));
    }

    /**
     *
     * @return the RectangularGridSpatialModel2D
     */
    public final RectangularGridSpatialModel2D getGrid() {
        return myGrid;
    }

    /**
     * Can be used by sub-classes to redefine the grid
     *
     * @param x the ULHC x
     * @param y the ULHC y
     * @param width the width of the grid
     * @param height the height of the grid
     * @param numRows the number of rows in the grid
     * @param numCols the number of columns in the grid
     */
    protected final void setGrid(double x, double y, double width, double height,
            int numRows, int numCols) {
        myGrid = new RectangularGridSpatialModel2D(x, y, width, height,
                numRows, numCols);
    }

    /**
     *
     * @return a uniformly picked coordinate in the grid
     */
    public CoordinateIfc getRandomCoordinate() {
        double w = getGrid().getWidth();
        double h = getGrid().getHeight();
        double x = myUniformRV.getValue() * w;
        double y = myUniformRV.getValue() * h;
        return getGrid().getCoordinate(x, y);
    }

    /**
     *
     * @param cell the cell to be within
     * @return a random point in the cell
     */
    public CoordinateIfc getRandomCoordinateInCell(RectangularCell2D cell) {
        double w = cell.getWidth();
        double h = cell.getHeight();
        double dx = myUniformRV.getValue() * w;
        double dy = myUniformRV.getValue() * h;
        double x = cell.getX() + dx;
        double y = cell.getY() + dy;
        return getGrid().getCoordinate(x, y);
    }

    /** Uniformly picks a cell from the list of cells. The supplied list of cells
     *  must not contain null members
     * 
     * @param cells the cells to pick from
     * @return the picked cell
     */
    public RectangularCell2D getRandomCell(List<RectangularCell2D> cells) {
        if (cells == null) {
            throw new IllegalStateException("The supplied cells was null");
        }
        int randInt = randInt(0, cells.size() - 1);
        RectangularCell2D cell = cells.get(randInt);
        if (cell == null) {
            throw new IllegalStateException("The selected cell was null");
        }
        return cell;
    }

    /** Uniformly picks a cell from the grid
     *
     * @return the picked cell
     */
    public RectangularCell2D getRandomCell() {
        return getRandomCell(getGrid().getCellsAsList());
    }

    /**
     * Sub-classes can use this when an element transition from cells
     *
     * @param element the element involved in the transition
     * @param cCell the elements current cell
     * @param pCell the elements previous cell
     */
    protected void cellTransition(SpatialElementIfc element,
            RectangularCell2D cCell, RectangularCell2D pCell) {
//        Modelelement me = element.getModelElement();
//        System.out.print(element.getName());
//        System.out.print("move from cell: ");
//        System.out.print(pCell.getRowColName());
//        System.out.print("to cell: ");
//        System.out.println(cCell.getRowColName());
    }

    /**
     * Sub-classes can use this when an element moves its position
     *
     * @param element
     */
    protected void positionUpdate(SpatialElementIfc element) {

    }

    private class PositionObserver implements ObserverIfc {

        @Override
        public void update(Object o, Object arg) {
            int state = getGrid().getObserverState();
            if (state == SpatialModel.UPDATED_POSITION) {
                SpatialElementIfc se = getGrid().getUpdatingSpatialElement();
                positionUpdate(se);
            }
            if (state == RectangularGridSpatialModel2D.CELL_CHANGED) {
                SpatialElementIfc se = getGrid().getUpdatingSpatialElement();
                RectangularCell2D cC = getGrid().getCell(se.getPosition());
                RectangularCell2D pC = getGrid().getCell(se.getPreviousPosition());
                cellTransition(se, cC, pC);
            }
        }

    }

}
