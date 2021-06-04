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

import java.util.List;

/** A base class for implementing movement within a 2D rectangular grid
 *
 * @author rossetti
 */
public abstract class AbstractRG2DMover extends AbstractMover {

    protected RectangularGridSpatialModel2D myGrid;

    protected RectangularGridModel myGridModel;

    protected RectangularCell2DSelectorIfc myCellSelector;

    protected EuclideanStepBasedMovementController myMoveController;

    protected boolean myStartRandomlyFlag;

    public AbstractRG2DMover(RectangularGridModel parent) {
        this(parent, new Vector3D(), null);
    }

    public AbstractRG2DMover(RectangularGridModel parent, String name) {
        this(parent, new Vector3D(), name);
    }

    public AbstractRG2DMover(RectangularGridModel parent, CoordinateIfc coordinate, String name) {
        super(parent, name, parent.getGrid(), coordinate);
        myStartRandomlyFlag = false;
        myGrid = parent.getGrid();
        myGridModel = parent;
        myCellSelector = new UniformCellSelector(this);
        myMoveController = new EuclideanStepBasedMovementController(this);
        setMovementController(myMoveController);
    }

    /**
     *
     * @return true if initialized with random trip
     */
    public final boolean isStartRandomlyOption() {
        return myStartRandomlyFlag;
    }

    /**
     *
     * @param startRandomlyFlag true means initialize with random trip
     */
    public final void setStartRandomlyOption(boolean startRandomlyFlag) {
        this.myStartRandomlyFlag = startRandomlyFlag;
    }

    /**
     * Move to the center of the cell
     *
     * @param cell the cell
     */
    public void moveToCenterOfCell(RectangularCell2D cell) {
        moveTo(cell.getCenterCoordinate());
    }

    /**
     * Moves to a random location within the specified cell
     *
     * @param cell the cell to move in
     */
    public void moveToInsideCellRandomly(RectangularCell2D cell) {
        moveTo(getGridModel().getRandomCoordinateInCell(cell));
    }

    /**
     * Randomly set the initial position using the getRandomCoordinate()
     */
    public void setInitialPositionRandomly() {
        setInitialPosition(getRandomCoordinate());
    }

    /**
     * Randomly generates a coordinate using the RectangularGridModel
     *
     * @return the coordinate
     */
    public CoordinateIfc getRandomCoordinate() {
        return getGridModel().getRandomCoordinate();
    }

    /**
     *
     * @return the RectangularGridSpatialModel2D
     */
    public final RectangularGridModel getGridModel() {
        return myGridModel;
    }

    /**
     *
     * @return the current cell of the mover
     */
    public final RectangularCell2D getCurrentCell() {
        return getRectagularGrid().getCell(getPosition());
    }

    /**
     *
     * @return a thing that knows how to select a cell
     */
    public RectangularCell2DSelectorIfc getCellSelector() {
        return myCellSelector;
    }

    /**
     *
     * @param cellSelector the selector
     */
    public void setCellSelector(RectangularCell2DSelectorIfc cellSelector) {
        if (cellSelector == null) {
            throw new IllegalArgumentException("The supplied cell selector was null");
        }
        myCellSelector = cellSelector;
    }

    /**
     * Does not include the core cell in the neighborhood
     *
     * @return a randomly selected cell using the cell selector
     */
    public RectangularCell2D selectNeighborRandomly() {
        return myCellSelector.selectCell(getNeighborhoodList());
    }

    /**
     *
     * @return the RectanularGridSpatialModel2D that contains the mover
     */
    public final RectangularGridSpatialModel2D getRectagularGrid() {
        return myGrid;
    }

    /**
     * A list of the Moore neighborhood for this mover. The core cell is not
     * included in the list
     *
     * @return
     */
    protected List<RectangularCell2D> getNeighborhoodList() {
        return getRectagularGrid().getMooreNeighborhoodAsList(this);
    }


}
