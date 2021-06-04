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
import jsl.utilities.random.rng.RNStreamFactoryDepracated.RNStream;
import jsl.utilities.random.rng.RNStreamFactoryDepracated;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.ExponentialRV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO need to come up with test for new RNStreamFactory
/**
 * @author rossetti
 */
@Deprecated
public class RNStreamFactoryTestDepracated {

    RNStreamFactoryDepracated f;

    @BeforeEach
    public void setUp() {
        System.out.println("Making new factory.");
        f = new RNStreamFactoryDepracated();
        System.out.println(f);
    }

    @Test
    public void test1() {
        double sum = 0.0;
        int i;

        System.out.println();
        System.out.println(Arrays.toString(f.getFactorySeed()));

        RNStream g1 = (RNStream) f.getStream("g1");

        System.out.println(g1);

        System.out.println();
        System.out.println(Arrays.toString(f.getFactorySeed()));

        RNStream g2 = (RNStream) f.getStream("g2");

        System.out.println(g2);

        System.out.println();
        System.out.println(Arrays.toString(f.getFactorySeed()));

        RNStream g3 = (RNStream) f.getStream("g3");

        sum = g2.randU01() + g3.randU01();

        g1.advanceState(5, 3);
        sum += g1.randU01();

        g1.resetStartStream();
        for (i = 0; i < 35; i++) {
            g1.advanceState(0, 1);
        }
        sum += g1.randU01();

        g1.resetStartStream();
        long sumi = 0;
        for (i = 0; i < 35; i++) {
            sumi += g1.randInt(1, 10);
        }
        sum += sumi / 100.0;

        double sum3 = 0.0;
        for (i = 0; i < 100; i++) {
            sum3 += g3.randU01();
        }
        sum += sum3 / 10.0;

        g3.resetStartStream();
        for (i = 1; i <= 5; i++) {
            sum += g3.randU01();
        }

        for (i = 0; i < 4; i++) {
            g3.advanceToNextSubstream();
        }
        for (i = 0; i < 5; i++) {
            sum += g3.randU01();
        }

        g3.resetStartSubstream();
        for (i = 0; i < 5; i++) {
            sum += g3.randU01();
        }

        g2.advanceToNextSubstream();
        sum3 = 0.0;
        for (i = 1; i <= 100000; i++) {
            sum3 += g2.randU01();
        }
        sum += sum3 / 10000.0;

        g3.setAntitheticOption(true);
        sum3 = 0.0;
        for (i = 1; i <= 100000; i++) {
            sum3 += g3.randU01();
        }
        sum += sum3 / 10000.0;

        long[] germe = {1, 1, 1, 1, 1, 1};
        f.setFactorySeed(germe);

        RNStream[] gar = {(RNStream) f.getStream("Poisson"), (RNStream) f.getStream("Laplace"),
                (RNStream) f.getStream("Galois"), (RNStream) f.getStream("Cantor")};
        for (i = 0; i < 4; i++) {
            sum += gar[i].randU01();
        }

        gar[2].advanceState(-127, 0);
        sum += gar[2].randU01();

        gar[2].increasedPrecis(true);
        gar[2].advanceToNextSubstream();
        sum3 = 0.0;
        for (i = 0; i < 100000; i++) {
            sum3 += gar[2].randU01();
        }
        sum += sum3 / 10000.0;

        gar[2].setAntitheticOption(true);
        sum3 = 0.0;
        for (i = 0; i < 100000; i++) {
            sum3 += gar[2].randU01();
        }
        sum += sum3 / 10000.0;

        gar[2].setAntitheticOption(false);
        gar[2].increasedPrecis(false);

        for (i = 0; i < 4; i++) {
            sum += gar[i].randU01();
        }

        StringBuffer str = new StringBuffer(Double.toString(sum));
        str.setLength(9);
        System.out.println("-----------------------------------------------------");
        System.out.println("This test program should print the number   39.697547 \n");
        System.out.println("Actual test result = " + str + "\n");
        assertTrue((str.toString().equals("39.697547")));
    }

    @Test
    public void test2() {
        int i;
        double sum = 0.0;

        RNStream g1 = (RNStream) f.getStream("g1");
        RNStream g2 = (RNStream) f.getStream("g2");
        RNStream g3 = (RNStream) f.getStream("g3");

        System.out.println("Initial states of g1, g2, and g3:\n");
        g1.printState();
        g2.printState();
        g3.printState();
        sum = g2.randU01() + g3.randU01();
        for (i = 0; i < 12345; i++) {
            g2.randU01();
        }

        g1.advanceState(5, 3);
        System.out.println("State of g1 after advancing by 2^5 + 3 = 35 steps:");
        g1.printState();
        System.out.println("" + g1.randU01());

        g1.resetStartStream();
        for (i = 0; i < 35; i++) {
            g1.advanceState(0, 1);
        }
        System.out.println("\nState of g1 after reset and advancing 35 times by 1:");
        g1.printState();
        System.out.println(g1.randU01());

        g1.resetStartStream();
        int sumi = 0;
        for (i = 0; i < 35; i++) {
            sumi += g1.randInt(1, 10);
        }
        System.out.println("\nState of g1 after reset and 35 calls to randInt (1, 10):");
        g1.printState();
        System.out.println("   sum of 35 integers in [1, 10] = " + sumi);
        sum += sumi / 100.0;
        System.out.println("\nrandU01 (g1) = " + g1.randU01());

        double sum3 = 0.0;
        g1.resetStartStream();
        g1.increasedPrecis(true);
        sumi = 0;
        for (i = 0; i < 17; i++) {
            sumi += g1.randInt(1, 10);
        }
        System.out.println("\nState of g1 after reset, increasedPrecis (true) and 17 calls to randInt (1, 10):");
        g1.printState();
        g1.increasedPrecis(false);
        g1.randInt(1, 10);
        System.out.println("State of g1 after increasedPrecis (false) and 1 call to randInt");
        g1.printState();
        sum3 = sumi / 10.0;

        g1.resetStartStream();
        g1.increasedPrecis(true);
        for (i = 0; i < 17; i++) {
            sum3 += g1.randU01();
        }
        System.out.println("\nState of g1 after reset, IncreasedPrecis (true) and 17 calls to RandU01:");
        g1.printState();
        g1.increasedPrecis(false);
        g1.randU01();
        System.out.println("State of g1 after IncreasedPrecis (false) and 1 call to RandU01");
        g1.printState();
        sum += sum3 / 10.0;

        sum3 = 0.0;
        System.out.println("\nSum of first 100 output values from stream g3:");
        for (i = 1; i <= 100; i++) {
            sum3 += g3.randU01();
        }
        System.out.println("   sum = " + sum3);
        sum += sum3 / 10.0;

        System.out.println("\n\nReset stream g3 to its initial seed.");
        g3.resetStartStream();
        System.out.println("First 5 output values from stream g3:");
        for (i = 1; i <= 5; i++) {
            System.out.println(g3.randU01());
        }
        sum += g3.randU01();

        System.out.println("\nReset stream g3 to the next SubStream, 4 times.");
        for (i = 1; i <= 4; i++) {
            g3.advanceToNextSubstream();
        }
        System.out.println("First 5 output values from stream g3, fourth SubStream:\n");
        for (i = 1; i <= 5; i++) {
            System.out.println(g3.randU01());
        }
        sum += g3.randU01();

        System.out.println("\nReset stream g2 to the beginning of SubStream.");
        g2.resetStartSubstream();
        System.out.print(" Sum of 100000 values from stream g2 with double precision:   ");
        sum3 = 0.0;
        g2.increasedPrecis(true);
        for (i = 1; i <= 100000; i++) {
            sum3 += g2.randU01();
        }
        System.out.println(sum3);
        sum += sum3 / 10000.0;
        g2.increasedPrecis(false);

        g3.setAntitheticOption(true);
        System.out.print(" Sum of 100000 antithetic output values from stream g3:   ");
        sum3 = 0.0;
        for (i = 1; i <= 100000; i++) {
            sum3 += g3.randU01();
        }
        System.out.println(sum3);
        sum += sum3 / 10000.0;

        System.out.print("\nSetPackageSeed to seed = { 1, 1, 1, 1, 1, 1 }");
        long[] germe = {1, 1, 1, 1, 1, 1};
        f.setFactorySeed(germe);

        System.out.println("\nDeclare an array of 4 named streams and write their full state\n");
        RNStream[] gar = {(RNStream) f.getStream("Poisson"), (RNStream) f.getStream("Laplace"),
                (RNStream) f.getStream("Galois"), (RNStream) f.getStream("Cantor")};
        for (i = 0; i < 4; i++) {
            gar[i].printStateFull();
        }

        System.out.println("Jump stream Galois by 2^127 steps backward");
        gar[2].advanceState(-127, 0);
        gar[2].printState();
        gar[2].advanceToNextSubstream();

        for (i = 0; i < 4; i++) {
            sum += gar[i].randU01();
        }

        System.out.println("--------------------------------------");
        System.out.println("Final Sum = " + sum);
        StringBuffer str = new StringBuffer(Double.toString(sum));
        str.setLength(9);
        System.out.println("-----------------------------------------------------");
        System.out.println("This test program should print the number 23.705323 \n");
        System.out.println("Actual test result = " + str + "\n");
        assertTrue((str.toString().equals("23.705323")));

    }

    @Test
    public void test3() {
        RNStreamIfc g1 = f.getStream();
        RNStreamIfc g2 = f.getStream();
        System.out.println("Two different streams from the same factory.");
        System.out.println("Note that they produce different random numbers");
        double s1 = 0;
        double s2 = 0;
        double u1;
        double u2;
        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            s1 = s1 + u1;
            s2 = s2 + u2;
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }
        double t1 = s1;
        double t2 = s2;
        assertTrue(s1 != s2);

        System.out.println();

        g1.resetStartStream();
        g2.resetStartStream();
        System.out.println("Resetting to the start of each stream simply");
        System.out.println("causes them to repeat the above.");

        s1 = 0;
        s2 = 0;
        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            s1 = s1 + u1;
            s2 = s2 + u2;
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

        assertTrue(s1 != s2);

        g1.advanceToNextSubstream();
        g1.advanceToNextSubstream();
        System.out.println("Advancing to the start of the next substream ");
        System.out.println("causes them to advance to the beginning of the next substream.");

        s1 = 0;
        s2 = 0;
        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            s1 = s1 + u1;
            s2 = s2 + u2;
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }
        assertTrue(s1 != s2);

        g1.resetStartStream();
        g2.resetStartStream();
        g1.setAntitheticOption(true);
        g2.setAntitheticOption(true);
        System.out.println("Resetting to the start of the stream and turning on antithetic");
        System.out.println("causes them to produce the antithetics for the original starting stream.");

        s1 = 0;
        s2 = 0;
        for (int i = 0; i < 5; i++) {
            u1 = g1.randU01();
            u2 = g2.randU01();
            s1 = s1 + (1.0 - u1);
            s2 = s2 + (1.0 - u2);
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
        }

        assertTrue(s1 == t1);
        assertTrue(s2 == t2);
    }

    @Test
    public void test4() {

        System.out.println();
        System.out.println("Demonstrates the resetting of the package seed");
        System.out.println("After setting the package seed, all created streams ");
        System.out.println("start based on that location.");

        System.out.println();
        System.out.println("The current package stream.");
        System.out.println(Arrays.toString(f.getFactorySeed()));

        System.out.println();
        System.out.println("Resetting to the default package stream to default.");

        f.resetFactorySeed();

        System.out.println("The current package seed is now:");
        System.out.println(Arrays.toString(f.getFactorySeed()));

        System.out.println();
        System.out.println("Creating the 1st stream");
        RNStream g1 = (RNStream) f.getStream("g1");

        System.out.println(g1);
        System.out.println();

        System.out.println("The current package seed is now:");
        System.out.println(Arrays.toString(f.getFactorySeed()));

        System.out.println();
        System.out.println("Creating the 2nd stream");

        RNStream g2 = (RNStream) f.getStream("g2");

        System.out.println(g2);

        System.out.println("The current package seed is now:");
        System.out.println(Arrays.toString(f.getFactorySeed()));
    }

    @Test
    public void test5() {
        // factories produce the same streams
        RNStreamFactoryDepracated f1 = new RNStreamFactoryDepracated("f1");
        RNStreamFactoryDepracated f2 = new RNStreamFactoryDepracated("f2");

        RNStreamIfc g1f1 = f1.getStream("g1 from f1");
        RNStreamIfc g1f2 = f2.getStream("g1 from f2");

        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 5: Factories produce same streams");
        System.out.println(f1);
        System.out.println(f2);
        System.out.println(g1f1);
        System.out.println(g1f2);
        System.out.println("Generate from both");
        boolean flag = true;
        for (int i = 1; i <= 10; i++) {
            double u1 = g1f1.randU01();
            double u2 = g1f2.randU01();
            System.out.println("u1 = " + u1 + "\t u2 = " + u2);
            if (u1 != u2) {
                flag = false;
            }
        }

        System.out.println("Test passes if all generated are the same");
        System.out.println("**********************************************");

        assertTrue(flag);
    }

    @Test
    public void test6() {
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 6");
        System.out.println("Make and print the default stream factory");
        RNStreamFactoryDepracated.getDefaultFactory().resetFactorySeed();
        System.out.println(RNStreamFactoryDepracated.getDefaultFactory());

        List<ExponentialRV> list1 = new ArrayList<ExponentialRV>();
        ExponentialRV e;

        System.out.println("Making some Exponentials using default factory");
        RNStreamFactoryDepracated defaultFactory = RNStreamFactoryDepracated.getDefaultFactory();
        for (int i = 1; i <= 3; i++) {
            e = new ExponentialRV(1.0, defaultFactory.getStream());
            System.out.println(e);
            list1.add(e);
        }

        double sum1 = 0;
        System.out.println("Generating some values");
        for (int i = 1; i <= 10; i++) {
            double e1 = list1.get(0).getValue();
            double e2 = list1.get(1).getValue();
            double e3 = list1.get(2).getValue();
            System.out.println("e1 = " + e1 + "\t e2 = " + e2 + "\t e3 = " + e3);
            sum1 = sum1 + (e1 + e2 + e3);
        }

        System.out.println("sum1 = " + sum1);

        System.out.println("Creating a new factory");
        RNStreamFactoryDepracated f1 = new RNStreamFactoryDepracated("f1");

        System.out.println("Changing the default factory");
        RNStreamFactoryDepracated.setDefaultFactory(f1);
        System.out.println(RNStreamFactoryDepracated.getDefaultFactory());

        List<ExponentialRV> list2 = new ArrayList<ExponentialRV>();
        System.out.println("Making some Exponentials using new default factory");
        System.out.println("Exponentials actually have different stream objects,");
        System.out.println("but they start at the same location (seed)");
        for (int i = 1; i <= 3; i++) {
            e = new ExponentialRV(1.0, f1.getStream());
            list2.add(e);
            System.out.println(e);
        }

        double sum2 = 0;
        System.out.println("Generating some values");
        for (int i = 1; i <= 10; i++) {
            double e1 = list2.get(0).getValue();
            double e2 = list2.get(1).getValue();
            double e3 = list2.get(2).getValue();
            System.out.println("e1 = " + e1 + "\t e2 = " + e2 + "\t e3 = " + e3);
            sum2 = sum2 + (e1 + e2 + e3);
        }

        System.out.println("sum1 = " + sum1);
        System.out.println("sum2 = " + sum2);
        System.out.println("Test passes if sum1 == sum2");
        assertTrue(JSLMath.equal(sum1, sum2));
    }

    @Test
    public void test7() {
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 7");
        System.out.println("Make a factory");
        RNStreamFactoryDepracated f1 = new RNStreamFactoryDepracated("f1");
        System.out.println(f1);
        System.out.println("Clone the factory");
        RNStreamFactoryDepracated fc = f1.newInstance();
        System.out.println(fc);
        System.out.println("Make a stream from f1");
        RNStreamIfc rngf1 = f1.getStream();
        System.out.println(rngf1);
        System.out.println("Generate 5 numbers from rngf1");
        for (int i = 1; i <= 5; i++) {
            System.out.println(rngf1.randU01());
        }
        System.out.println(rngf1);
        System.out.println("Current state of f1");
        System.out.println(f1);
        System.out.println("Clone the factory");
        RNStreamFactoryDepracated fc2 = f1.newInstance();
        System.out.println(fc2);
    }

    @Test
    public void test8() {
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 8");
        System.out.println("Make a factory");
        RNStreamFactoryDepracated f1 = new RNStreamFactoryDepracated("f1");
        System.out.println(f1);
        System.out.println("Make a stream from f1");
        RNStreamFactoryDepracated.RNStream rngf1 = (RNStream) f1.getStream();
        System.out.println(rngf1);
        System.out.println("Generate 5 numbers from rngf1");
        for (int i = 1; i <= 5; i++) {
            System.out.println(rngf1.randU01());
        }
        System.out.println(rngf1);
        System.out.println("Current state of f1");
        System.out.println(f1);
        System.out.println("Clone the stream");
        RNStreamFactoryDepracated.RNStream rngf2 = rngf1.newInstance("clone of rngf1");
        System.out.println(rngf2);

        long[] s1 = rngf1.getState();
        long[] s2 = rngf2.getState();
        boolean b = true;

        for (int i = 0; i < s1.length; i++) {
            b = b && (s1[i] == s2[i]);
        }
        assertTrue(b);

    }

    @Test
    public void test9() {
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 9");
        System.out.println("Make a factory");
        RNStreamFactoryDepracated f1 = new RNStreamFactoryDepracated("f1");
        System.out.println(f1);
        System.out.println("Make a stream from f1");
        RNStreamFactoryDepracated.RNStream rngf1 = (RNStream) f1.getStream();
        System.out.println(rngf1);
        System.out.println("Generate 5 numbers from rngf1");
        for (int i = 1; i <= 5; i++) {
            System.out.println(rngf1.randU01());
        }
        System.out.println(rngf1);
        System.out.println("Current state of f1");
        System.out.println(f1);
        System.out.println("get antithetic of stream");
        RNStreamFactoryDepracated.RNStream rngf2 = rngf1.newAntitheticInstance("antithetic of rngf1");
        System.out.println(rngf2);

        boolean b = true;

        for (int i = 1; i <= 5; i++) {
            double u = rngf1.randU01();
            double ua = 1.0 - rngf2.randU01();
            System.out.println("u = " + u + " ua = " + ua);
            b = b && (JSLMath.equal(u, ua));
            //System.out.println("b = " + b);
        }
        assertTrue(b);
    }
}
