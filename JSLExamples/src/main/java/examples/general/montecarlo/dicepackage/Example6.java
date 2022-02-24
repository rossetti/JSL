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

import jsl.utilities.statistic.IntegerFrequency;
import jsl.utilities.statistic.Statistic;

public class Example6 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        TwoSixSidedDice d2 = new TwoSixSidedDice();

        IntegerFrequency h = new IntegerFrequency("Distribution across points ");
        int n = 100000;
        for (int i=1;i<=n;i++){
            h.collect(d2.roll());
        }

        System.out.println(h);

        int point = 2;
        Statistic s = new Statistic("Rolls to reach " + point);

        int k = 100000;
        System.out.println();
        System.out.println("Estimating number of rolls to reach " + point);

        for(int i=1;i<=k;i++){
            int rolls = d2.countRolls(point);
            s.collect(rolls);
        }

        System.out.println(s);


    }

}
