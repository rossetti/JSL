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
package test.random;

import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.StatisticXY;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class TestAntithetic {

    @BeforeEach
    public void setup() {
    }

    @Test
    public void test1() {
        NormalRV e = new NormalRV();

        RVariableIfc ea = e.newAntitheticInstance();
        StatisticXY sxy = new StatisticXY();

        for (int i = 1; i <= 10; i++) {
            double x = e.getValue();
            double xa = ea.getValue();
            sxy.collectXY(x, xa);
        }
        System.out.println(sxy);
        System.out.println("Test 1");
        System.out.println("Correlation should be = -1.0");
        assertTrue(JSLMath.equal(sxy.getCorrelationXY(), -1.0));
    }

}
