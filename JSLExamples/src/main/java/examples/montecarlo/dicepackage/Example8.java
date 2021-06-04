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

package examples.montecarlo.dicepackage;

import jsl.utilities.statistic.HalfWidthSequentialSampler;

/**
 *
 * @author rossetti
 */
public class Example8 {

    public static void main(String[] args) {

        HalfWidthSequentialSampler s = new HalfWidthSequentialSampler();

        boolean converged = s.run(new CrapsGameSampler(), 0.01, 1000000);
        if (converged)
            System.out.println("The half-width was met");
        else
            System.out.println("The half-width was not met");
        System.out.println(s.getStatistic());

    }
}
