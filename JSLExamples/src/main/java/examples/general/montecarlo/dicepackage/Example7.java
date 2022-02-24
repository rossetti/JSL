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

import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class Example7 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("Simulating the game of craps");
        
        Statistic probOfWinning = new Statistic("Prob of winning");
        Statistic numTosses = new Statistic("Number of Toss Statistics");

        RollIfc dice = new TwoSixSidedDice();
        int point = 0;
        int nextRoll = 0;
        int numGames = 31141407;
        for (int k = 1; k <= numGames; k++) {
            int winner = 0;
            point = dice.roll();
            int numberoftoss = 1;

            if (point == 7 || point == 11) // automatic winner
            {
                winner = 1;
            } else if (point == 2 || point == 3 || point == 12) // automatic loser
            {
                winner = 0;
            } else { // now must roll to get point
                boolean continueRolling = true;
                while (continueRolling == true) {
                    // increment number of tosses
                    numberoftoss++;
                    // make next roll
                    nextRoll = dice.roll();
                    // if next roll == point then winner = 1, contineRolling = false
                    // else if next roll = 7 then winner = 0, continueRolling = false
                    if (nextRoll == point) {
                        winner = 1;
                        continueRolling = false;
                    } else if (nextRoll == 7) {
                        winner = 0;
                        continueRolling = false;
                    }
                }
            }

            probOfWinning.collect(winner);
            numTosses.collect(numberoftoss);
        }

        System.out.println(probOfWinning);
        System.out.println(numTosses);
    }

}
