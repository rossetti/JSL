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

public class CrapsGame {

    protected boolean winner = false;

    protected int numberoftosses = 0;

    protected RollIfc dice = new TwoSixSidedDice();

    public boolean play() {
        winner = false;
        int point = dice.roll();
        numberoftosses = 1;
        if (point == 7 || point == 11) {
            winner = true; // automatic winner
        } else if (point == 2 || point == 3 || point == 12) {
            winner = false; // automatic loser
        } else { // now must roll to get point
            boolean continueRolling = true;
            while (continueRolling == true) {
                // increment number of tosses
                numberoftosses++;
                // make next roll
                int nextRoll = dice.roll();
                // if next roll == point then winner = 1, contineRolling = false
                // else if next roll = 7 then winner = 0, continueRolling = false
                if (nextRoll == point) {
                    winner = true;
                    continueRolling = false;
                } else if (nextRoll == 7) {
                    winner = false;
                    continueRolling = false;
                }
            }
        }

        return winner;

    }

    public boolean getResult() {
        return winner;
    }

    public int getNumberOfTosses() {
        return numberoftosses;
    }

    public static void main(String[] args) {
        CrapsGame g = new CrapsGame();
        g.play();
        System.out.println("result = " + g.getResult());
        System.out.println("# tosses = " + g.getNumberOfTosses());
    }
}
