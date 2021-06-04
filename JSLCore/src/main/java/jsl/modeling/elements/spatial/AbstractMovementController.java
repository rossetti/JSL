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
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

/**
 * An AbstractMovementController can be used to control the movements of an
 * AbstractMover
 *
 *
 */
public abstract class AbstractMovementController extends SchedulingElement implements MovementControllerIfc {

    /**
     * The velocity addFactor, default is 1. If this is changed, it is changed for
     * all replications
     */
    protected double myVelFactor;

    protected RandomVariable myVelocity;

    /**
     * @param parent
     */
    public AbstractMovementController(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public AbstractMovementController(ModelElement parent, String name) {
        super(parent, name);
        myVelFactor = 1.0;
        myVelocity = new RandomVariable(this, ConstantRV.ONE);
    }

    @Override
    abstract public void controlMovement(AbstractMover movingElement);

    @Override
    public final void setVelocityChangeFactor(double factor) {
        if (factor <= 0.0) {
            throw new IllegalArgumentException("The velocity addFactor must be > 0.");
        }
        myVelFactor = factor;
    }

    @Override
    public final double getVelocityChangeFactor() {
        return myVelFactor;
    }

    /**
     * The velocity for an individual movement
     *
     * @return
     */
    @Override
    public double getVelocity() {
        return myVelocity.getValue() * getVelocityChangeFactor();
    }

    /**
     * @return Returns the velocity.
     */
    @Override
    public final RandomIfc getVelocityInitialRandomSource() {
        return myVelocity.getInitialRandomSource();
    }

    /**
     * @param velocity The velocity to set.
     */
    @Override
    public final void setVelocityInitialRandomSource(RandomIfc velocity) {
        myVelocity.setInitialRandomSource(velocity);
    }

    /**
     * @return Returns the velocity.
     */
    @Override
    public final RandomIfc getVelocityRandomSource() {
        return myVelocity.getRandomSource();
    }

    /**
     * @param velocity The velocity to set.
     */
    @Override
    public final void setVelocityRandomSource(RandomIfc velocity) {
        myVelocity.setRandomSource(velocity);
    }

}
