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

public class TwoSixSidedDice implements RollIfc {

    private Dice myDice;

    public TwoSixSidedDice() {
        myDice = new Dice();
        myDice.add(new Die());
        myDice.add(new Die());
    }

    public int roll(){
        return myDice.roll();
    }

    public int countRolls(int point){
        if ((point < 2) || (point > 12))
            throw new IllegalArgumentException("The point must be in 2 to 12");
        
        int n = 0;
        do {
            n = n + 1;
        } while (roll() != point);
        return n;
    }

    public double estimateMeanNumberOfRolls(int point, int sampleSize){
        if (sampleSize < 1)
            throw new IllegalArgumentException("The sample size must be >= 1");
        double sum = 0.0;
        for(int i=1;i<=sampleSize;i++)
            sum = sum + countRolls(point);
        return sum/sampleSize;
    }

}
