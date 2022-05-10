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
package jsl.utilities.random.rng;

import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;

public interface RandU01Ifc extends GetAntitheticValueIfc, DoubleSupplier {

    /**
     * Returns a pseudo-random uniformly distributed number
     *
     * @return the random number
     */
    double randU01();

    /**
     * The previous U(0,1) generated (returned) by randU01()
     *
     * @return previous U(0,1) generated (returned) by randU01()
     */
    double getPrevU01();

    /** Returns the antithetic value of the last U(0,1) drawn.
     *  This facilitates antithetic sampling methods.
     *
     * @return  returns 1.0 - getPrevU01()
     */
    default double getAntitheticValue() {
        return 1.0 - getPrevU01();
    }

    /**
     *
     * @return the generated random number using randU01()
     */
    @Override
    default double getAsDouble() {
        return randU01();
    }

    /** Turns the doubles into a DoubleStream for the Stream API
     *
     * @return the doubles into a DoubleStream for the Stream API
     */
    default DoubleStream asDoubleStream(){
        return DoubleStream.generate(this);
    }
}
