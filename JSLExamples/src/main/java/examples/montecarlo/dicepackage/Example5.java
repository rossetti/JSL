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

public class Example5 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TwoSixSidedDice d2 = new TwoSixSidedDice();

        System.out.println("The number of rolls to get snake eyes " + d2.countRolls(2));

        System.out.println("The number of rolls to get snake eyes " + d2.countRolls(2));

        int k = 1000;
        System.out.println("Estimating number of rolls to reach point");
        System.out.println("Sample Size = " + k);
        for(int point=2;point<=12;point++){
           double xbar = d2.estimateMeanNumberOfRolls(point, k);
           System.out.println("Average number of rolls to reach " + point + " = "+ xbar);
        }

    }

}
