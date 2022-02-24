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

package examples.general.utilities.statistics;

import jsl.simulation.State;
import jsl.utilities.random.rvariable.BinomialRV;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.sp.TwoStateMarkovChain;
import jsl.utilities.statistic.IntegerFrequency;
import jsl.utilities.statistic.StateFrequency;

import java.util.List;

public class TestFrequencies {

    public static void main(String[] args) {

//        testStateTransitions();
    }

    private static void testStateTransitions(){
        TwoStateMarkovChain d = new TwoStateMarkovChain();
        d.setProbabilities(0.9, 0.8);
        StateFrequency sf = new StateFrequency(2);
        List<State> states = sf.getStates();
        for (int i = 1; i <= 20000; i++) {
            int x = (int)d.getValue();
            sf.collect(states.get(x));
        }
        System.out.println(sf);
    }

}
