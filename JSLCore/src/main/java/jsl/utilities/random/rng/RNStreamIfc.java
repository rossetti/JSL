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

/**
 * Represents a random number stream with stream control
 *
 * @author rossetti
 */
public interface RNStreamIfc extends RandU01Ifc, RNStreamControlIfc,
        RNStreamNewInstanceIfc, GetAntitheticStreamIfc {

    /**
     * Returns a (pseudo)random number from the discrete uniform distribution
     * over the integers {i, i + 1, . . . , j }, using this stream. Calls randU01 once.
     *
     * @param i start of range
     * @param j end of range
     * @return The integer pseudo random number
     */
    int randInt(int i, int j);
}
