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
package ksl.utilities.random.rng

import java.util.function.DoubleSupplier
import java.util.stream.DoubleStream

interface RandU01Ifc : GetAntitheticValueIfc, DoubleSupplier {
    /**
     * Returns a pseudo-random uniformly distributed number
     *
     * @return the random number
     */
    fun randU01(): Double

    /**
     * The previous U(0,1) generated (returned) by randU01()
     *
     * @return previous U(0,1) generated (returned) by randU01()
     */
    val previousU: Double

    /**
     *
     * @return the generated random number using randU01()
     */
    override fun getAsDouble(): Double {
        return randU01()
    }

    /** Turns the doubles into a DoubleStream for the Stream API
     *
     * @return the doubles into a DoubleStream for the Stream API
     */
    fun asDoubleStream(): DoubleStream? {
        return DoubleStream.generate(this)
    }
}