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
package examples.montecarlo;

import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class PoissonProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PoissonProcess();
    }

    public static void PoissonProcess() {
        ExponentialRV e = new ExponentialRV(1.0);
        double x = 10.0;

        int r = 1000;
        Statistic s = new Statistic();
        for (int i = 1; i <= r; i++) {
            double t = 0.0;
            double n = 0;
            do {
                t = t + e.getValue();
                if (t <= x) {
                    n = n + 1;
                }
            } while (t <= x);
            s.collect(n);

        }
        System.out.println(s);
    }
}
