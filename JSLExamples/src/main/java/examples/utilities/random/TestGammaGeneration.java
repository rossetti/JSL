/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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

package examples.utilities.random;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.reporting.JSL;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

public class TestGammaGeneration {

    private static PrintWriter outAR = JSL.getInstance().makePrintWriter("gammaOutAR.txt");
    private static PrintWriter outInv = JSL.getInstance().makePrintWriter("gammaOutInv.txt");

    public static void main(String[] args) {

        RNStreamIfc stream = JSLRandom.getDefaultRNStream();

        long arTime = runGammaAR(1000000, stream);

        long invTime = runGammaInv(1000000, stream);

        System.out.printf("AR time = %d %n", arTime);
        System.out.printf("Inv time = %d %n", invTime);
        System.out.printf("Inv time - AR time = %d %n", (invTime-arTime));
    }

    public static long runGammaAR(int n, RNStreamIfc stream) {
        Instant start = Instant.now();
        for (int i = 1; i <= n; i++) {
            double x = JSLRandom.rGamma(3.0, 5.0, stream, JSLRandom.AlgoType.AcceptanceRejection);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        return timeElapsed;
    }

    public static long runGammaInv(int n, RNStreamIfc stream) {
        Instant start = Instant.now();
        for (int i = 1; i <= n; i++) {
            double x = JSLRandom.rGamma(3.0, 5.0, stream);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        return timeElapsed;
    }
}
