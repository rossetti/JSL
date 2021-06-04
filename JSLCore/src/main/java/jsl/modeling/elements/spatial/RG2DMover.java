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

/** A concrete sub-class of AbstractRG2Mover that randomly moves around
 *
 * @author rossetti
 */
public class RG2DMover extends AbstractRG2DMover {

    public RG2DMover(RectangularGridModel parent) {
        this(parent, new Vector3D(), null);
    }

    public RG2DMover(RectangularGridModel parent, String name) {
        this(parent, new Vector3D(), name);
    }

    public RG2DMover(RectangularGridModel parent, CoordinateIfc coordinate, String name) {
        super(parent, coordinate, name);
    }

    @Override
    protected void initialize() {
        super.initialize();
        if (isStartRandomlyOption()) {
            moveToCenterOfCell(selectNeighborRandomly());
        }
    }

    @Override
    protected void startNextTrip() {
        moveToCenterOfCell(selectNeighborRandomly());
    }

}
