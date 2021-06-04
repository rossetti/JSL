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

package examples.utilities.random;

import jsl.utilities.random.rng.RandU01Ifc;

/**
 * Random is a PMMLCG generator that returns a pseudo-random real number
 * uniformly distributed between 0.0 and 1.0.  The period is (m - 1)
 * where m = 2,147,483,647 and the smallest and largest possible values
 * are (1 / m) and 1 - (1 / m) respectively.
 *
 * @author rossetti
 */
public class PMMLCG implements RandU01Ifc {

    private long myModulus = 2147483647;

    private long myMultiplier = 48271;

    /* initial seed, use 0 < DEFAULT < MODULUS   */
    private long myDefaultSeed = 123456789L;

    /* seed is the state of the generator        */
    private long mySeed = myDefaultSeed;

    private long Q;

    private long R;

    private double myPrevU;

    public PMMLCG() {
        this(123456789L);
    }

    public PMMLCG(long seed) {
        setSeed(seed);
        Q = myModulus / myMultiplier;
        R = myModulus % myMultiplier;
        myPrevU = Double.NaN;
    }

    public final long getDefaultSeed() {
        return myDefaultSeed;
    }

    public double randU01() {
        long t;

        t = myMultiplier * (mySeed % Q) - R * (mySeed / Q);
        if (t > 0) {
            mySeed = t;
        } else {
            mySeed = t + myModulus;
        }
        double u = (double) mySeed / myModulus;
        myPrevU = u;
        return (u);
    }

    public double getPrevU01() {
        return myPrevU;
    }

    public double getAntitheticValue() {
        return 1.0 - myPrevU;
    }

    public final void setSeed(long seed) {
        if (seed <= 0L) {
            throw new IllegalArgumentException("The seed must be > 0");
        }
        if (seed >= myModulus) {
            throw new IllegalArgumentException("The seed must be < " + myModulus);
        }

        mySeed = seed;

    }

    public final long getSeed() {
        return mySeed;
    }

    public static void main(String[] args) {

        PMMLCG r = new PMMLCG();

        for (int i = 1; i <= 10; i++) {
            System.out.println(r.randU01());
        }


    }
}
