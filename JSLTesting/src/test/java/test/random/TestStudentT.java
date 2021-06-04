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
package test.random;

import jsl.utilities.math.JSLMath;
import jsl.utilities.distributions.StudentT;
import jsl.utilities.random.rvariable.StudentTRV;
import jsl.utilities.reporting.JSL;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class TestStudentT {

    double precision = 0.000001;

    public TestStudentT() {
    }

    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void test1() {
        StudentT t = new StudentT(20);
        double[] p = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99};

        double[] d = {-1.3253407, -0.8599644, -0.5328628, -0.2567428,
            0.0000000, 0.2567428, 0.5328628, 0.8599644, 1.3253407,
            1.7247182, 2.5279770};

        int j = 0;
        for (double x : d) {
            assertTrue(JSLMath.equal(t.cdf(x), p[j], precision));
            j++;
            System.out.print(x);
            System.out.print("\t");
            System.out.println(t.cdf(x));
        }

        int i = 0;
        for (double u : p) {
            assertTrue(JSLMath.equal(t.invCDF(u), d[i], precision));
            i++;
            System.out.print(u);
            System.out.print("\t");
            System.out.println(t.invCDF(u));
        }
    }

    @Test
    public void test2() {
        StudentT t = new StudentT(5);
        double[] p = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99};

        double[] d = {-1.4758840, -0.9195438, -0.5594296, -0.2671809,
            0.0000000, 0.2671809, 0.5594296, 0.9195438, 1.4758840,
            2.0150484, 3.3649300};

        int j = 0;
        for (double x : d) {
            assertTrue(JSLMath.equal(t.cdf(x), p[j], precision));
            j++;
            System.out.print(x);
            System.out.print("\t");
            System.out.println(t.cdf(x));
        }

        int i = 0;
        for (double u : p) {
            assertTrue(JSLMath.equal(t.invCDF(u), d[i], precision));
            i++;
            System.out.print(u);
            System.out.print("\t");
            System.out.println(t.invCDF(u));
        }
    }

    @Test
    public void test3() {
        StudentT t = new StudentT(1);
        double[] p = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99};

        double[] d = {-3.077684e+00, -1.376382e+00, -7.265425e-01,
            -3.249197e-01, 6.123234e-17, 3.249197e-01, 7.265425e-01,
            1.376382e+00, 3.077684e+00, 6.313752e+00, 3.182052e+01};

        int j = 0;
        for (double x : d) {
            assertTrue(JSLMath.equal(t.cdf(x), p[j], precision));
            j++;
            System.out.print(x);
            System.out.print("\t");
            System.out.println(t.cdf(x));
        }

        int i = 0;
        for (double u : p) {
            assertTrue(JSLMath.equal(t.invCDF(u), d[i], precision));
            i++;
            System.out.print(u);
            System.out.print("\t");
            System.out.println(t.invCDF(u));
        }
    }

    //@Test
    public void test4() {
        // check the histogram of the generated data
        StudentTRV t = new StudentTRV(10);
        PrintWriter out = JSL.getInstance().makePrintWriter("Tdata.txt");
        for (int i = 1; i <= 10000; i++) {
            out.println(t.getValue());
        }
    }

    public void test5() {
        // check the generated t-table versus known t-tables
        PrintWriter out = JSL.getInstance().makePrintWriter("TTable.csv");
        
        double[] p = {0.6, 0.75, 0.9, 0.95, 0.975, 0.99, 0.995, 0.9995};
        out.print("");
        out.print(",");
        for (int j = 0; j < p.length; j++) {
            out.print(p[j]);
            if (j < p.length - 1) {
                out.print(",");
            }
        }
        out.println();
        StudentT t = new StudentT();
        for (int i = 1; i <= 30; i++) {
            t.setDegreesOfFreedom(i);
            out.print(i);
            out.print(",");
            for (int j = 0; j < p.length; j++) {
                out.print(t.invCDF(p[j]));
                if (j < p.length - 1) {
                    out.print(",");
                }
            }
            out.println();
        }
    }
}
