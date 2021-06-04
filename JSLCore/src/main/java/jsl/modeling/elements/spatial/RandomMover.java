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
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.UniformRV;

/**
 *
 */
public class RandomMover extends AbstractMover {

    private RandomVariable myTripDestinationX;
    private RandomVariable myTripDestinationY;
    private Vector3D myDestination;

    /**
     *
     * @param parent
     * @param smodel
     */
    public RandomMover(ModelElement parent, SpatialModel smodel) {
        this(parent, null, smodel);
    }

    /**
     *
     * @param parent
     * @param name
     * @param smodel
     */
    public RandomMover(ModelElement parent, String name, SpatialModel smodel) {
        super(parent, name, smodel);
        myTripDestinationX = new RandomVariable(this, new UniformRV(2, 10));
        myTripDestinationY = new RandomVariable(this, new UniformRV(3, 10));
        myDestination = new Vector3D();
        //MovementControllerIfc c = new EuclideanStepBasedMovementController(this);
        // setMovementController(c);
    }

    public final void setXDestinationInitialRandomSource(RandomIfc source) {
        myTripDestinationX.setInitialRandomSource(source);
    }

    public final void setYDestinationInitialRandomSource(RandomIfc source) {
        myTripDestinationY.setInitialRandomSource(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        double x = myTripDestinationX.getValue();
        double y = myTripDestinationY.getValue();
        myDestination.setCoordinates(x, y);
        moveTo(myDestination);
    }

    @Override
    protected void startNextTrip() {
        double x = myTripDestinationX.getValue();
        double y = myTripDestinationY.getValue();
        myDestination.setCoordinates(x, y);
        moveTo(myDestination);
    }

}
