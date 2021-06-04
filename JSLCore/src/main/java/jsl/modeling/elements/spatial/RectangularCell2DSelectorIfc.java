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

/** An interface to define a general pattern of selecting a cell 
 *  from a list of cells. The list of cells should not contain null as
 *  an element.
 *
 * @author rossetti
 */
public interface RectangularCell2DSelectorIfc {
   
    /** A method for selecting cells from a list of cells.
     * 
     *  If cells is null, an IllegalArgumentException is thrown
     *  If cells is empty an IllegalStateException is thrown
     *  If the returned cell is to be null, an IllegalStateException is thrown
     * 
     * @param cells the cells to select from
     * @return the selected cell
     */
    public RectangularCell2D selectCell(List<RectangularCell2D> cells);
 
    
}
