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

package examples.general.montecarlo.dicepackage;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;


public class Die implements RollIfc {

    private int myNumSides;

    private RNStreamIfc myRNG;

    public Die() {
        this(6);
    }

    public Die(int sides){
        if (sides <= 0)
            throw new IllegalArgumentException("Number of sides must be > 0");

        myNumSides = sides;
        myRNG = JSLRandom.getDefaultRNStream();

    }

    @Override
    public int roll(){
        double u = myRNG.randU01();
        return 1 + (int)Math.floor(myNumSides*u);
    }

    public int getNumberOfSides(){
        return myNumSides;
    }
}
