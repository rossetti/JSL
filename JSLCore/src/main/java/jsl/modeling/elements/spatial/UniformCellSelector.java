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
import jsl.modeling.elements.variable.RandomVariable;
import jsl.utilities.random.rvariable.UniformRV;

import java.util.List;

/**
 *
 * @author rossetti
 */
public class UniformCellSelector extends ModelElement implements RectangularCell2DSelectorIfc {

    protected RandomVariable myRV;

    public UniformCellSelector(ModelElement parent) {
        this(parent, null);
    }

    public UniformCellSelector(ModelElement parent, String name) {
        super(parent, name);
        myRV = new RandomVariable(this, new UniformRV(0.0, 1.0));
    }

    /** Randomly generates an integer between i and j
     * 
     * @param i the lower limit
     * @param j the upper limit
     * @return the random integer
     */
    public final int randInt(int i, int j) {
        if (i > j){
            throw new IllegalArgumentException("The lower limit must be <= the upper limit");
        }
        return (i + (int) (myRV.getValue() * (j - i + 1)));
    }

    @Override
    public RectangularCell2D selectCell(List<RectangularCell2D> cells) {
        if (cells == null){
            throw new IllegalArgumentException("The list of cells was null");
        }
        if (cells.isEmpty()){
            throw new IllegalStateException("The cell list was empty");
        }
        
        int randInt = randInt(0, cells.size()-1);
        RectangularCell2D cell = cells.get(randInt);
        if (cell == null){
            throw new IllegalStateException("The selected cell was null");
        }
        return cell;
    }

}
