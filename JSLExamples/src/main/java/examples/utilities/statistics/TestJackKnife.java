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

package examples.utilities.statistics;

import jsl.utilities.random.rvariable.LognormalRV;
import jsl.utilities.random.rvariable.NormalRV;
import jslx.statistics.EstimatorIfc;
import jslx.statistics.JackKnifeEstimator;

public class TestJackKnife {

    public static void main(String[] args) {
        example1();
        example2();
    }

    public static void example1(){
        NormalRV n = new NormalRV(10, 3);

        JackKnifeEstimator bs = new JackKnifeEstimator(n.sample(50), new EstimatorIfc.Average());

        System.out.println(bs);
    }

    public static void example2(){
        LognormalRV n = new LognormalRV(10, 3);

        JackKnifeEstimator bs = new JackKnifeEstimator(
                n.sample(50), new EstimatorIfc.Minimum());

        System.out.println(bs);
    }
}
