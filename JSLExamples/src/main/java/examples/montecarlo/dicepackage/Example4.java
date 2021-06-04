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

public class Example4 {

    public static void main(String[] args) {
        
        RollIfc d1 = new Die(12);

        System.out.println("Rolling 12 sided die");
        for(int i=1;i<=5;i++){
            System.out.println("roll " + i + " = " + d1.roll());
        }

        RollIfc d2 = new TwoSixSidedDice();

        System.out.println();
        System.out.println("Rolling 2 six sided dice");
         for(int i=1;i<=5;i++){
            System.out.println("roll " + i + " = " + d2.roll());
        }

    }

}
