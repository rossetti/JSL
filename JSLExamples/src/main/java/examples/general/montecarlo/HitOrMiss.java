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
package examples.general.montecarlo;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

/**
 *
 */
public class HitOrMiss {

    /**
     *
     */
    public HitOrMiss() {
        super();
    }

    public static void main(String[] args) {

        UniformRV u1RN = new UniformRV();
//		Uniform u2RN = new Uniform();
        Statistic s = new Statistic();
        u1RN.advanceToNextSubStream();

        for (int i = 1; i <= 1000; i++) {
            double hit = 0.0;
            double u1 = u1RN.getValue();
            double u2 = u1RN.getValue();

            double y = Math.sqrt(1.0 - u1 * u1);
            if (u2 <= y) {
                hit = 1.0;
            }
            s.collect(hit);
        }

        System.out.println(s);
        System.out.println("pi estimate = " + 4.0 * s.getAverage());

        System.out.println("p = " + Normal.stdNormalCDF(1.965));

    }
}
