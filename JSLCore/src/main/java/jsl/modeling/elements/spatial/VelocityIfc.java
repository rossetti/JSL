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

import jsl.utilities.random.RandomIfc;

/**
 * An interface for working with velocity
 *
 * @author rossetti
 */
public interface VelocityIfc {

    /**
     * The addFactor will be used to increase or decrease the velocity returned by
     * getVelocity()
     *
     * @param factor must be greater than zero
     */
    void setVelocityChangeFactor(double factor);

    /**
     * The addFactor will be used to increase or decrease the velocity returned by
     * getVelocity()
     *
     * @return the addFactor
     */
    double getVelocityChangeFactor();

    /**
     *
     * @return the velocity
     */
    double getVelocity();

    /**
     * @return Returns the velocity.
     */
    RandomIfc getVelocityInitialRandomSource();

    /**
     * Sets the underlying initial random source associated with the
     * determination of the velocity
     *
     * @param source the source
     */
    void setVelocityInitialRandomSource(RandomIfc source);

    /**
     * @return Returns the velocity.
     */
    RandomIfc getVelocityRandomSource();

    /**
     * Sets the current underlying random source associated with the
     * determination of the velocity
     *
     * @param source the source
     */
    void setVelocityRandomSource(RandomIfc source);

}
