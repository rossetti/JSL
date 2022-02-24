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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.utilities.random;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 *
 * @author rossetti
 */
public class RNStreamExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RNStreamIfc g1 = JSLRandom.nextRNStream();
        RNStreamIfc g2 = JSLRandom.nextRNStream();
        System.out.println("Two different streams from the same provider.");
        System.out.println("Note that they produce different random numbers");
        double u1;
        double u2;
        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

        System.out.println();

        g1.resetStartStream();
        g2.resetStartStream();
        System.out.println("Resetting to the start of each stream simply");
        System.out.println("causes them to repeat the above.");

        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

        g1.advanceToNextSubstream();
        g2.advanceToNextSubstream();
        System.out.println("Advancing to the start of the next substream ");
        System.out.println("causes them to advance to the beginning of the next substream.");

        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

        g1.resetStartStream();
        g2.resetStartStream();
        g1.setAntitheticOption(true);
        g2.setAntitheticOption(true);
        System.out.println("Resetting to the start of the stream and turning on antithetic");
        System.out.println("causes them to produce the antithetics for the original starting stream.");

        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

    }
}
