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

import java.util.Random;

/** A wrapper on java.util.Random that implements the RandU01Ifc
 *
 * @author rossetti
 */
public class JavaRNG extends Random implements RandU01Ifc {

    private double myPrevU;

    public double randU01() {
        double u = nextDouble();
        myPrevU = u;
        return u;
    }

    /** The previous U(0,1) generated (returned) by randU01()
     *
     * @return
     */
    public final double getPrevU01() {
        return myPrevU;
    }

    /** Returns the antithetic of the previous U(0,1)
     *  i.e. 1.0 - getPrevU01()
     *
     * @return
     */
    public final double getAntitheticValue() {
        return 1.0 - myPrevU;
    }
}
