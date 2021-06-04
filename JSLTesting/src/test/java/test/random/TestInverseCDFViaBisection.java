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
import jsl.utilities.distributions.Distribution;
import jsl.utilities.distributions.Normal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class TestInverseCDFViaBisection {
    
    public TestInverseCDFViaBisection() {
    }

    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void test1() {
        
        Normal n = new Normal();
        
        double p = 0.95;
        double x1 = n.invCDF(p);
        System.out.println("invCDF("+ p + ") = " + x1);

        System.out.println("CDF("+ -5 + ") = " + n.cdf(-5));
        System.out.println("CDF("+ 5 + ") = " + n.cdf(5));
        
        double x2 = Distribution.inverseContinuousCDFViaBisection(n, p, -5, 5);
        
        System.out.println("invCDF("+ p + ") = " + x2);
        
        assertTrue(JSLMath.within(x1, x2, 0.000001));
    }
}
