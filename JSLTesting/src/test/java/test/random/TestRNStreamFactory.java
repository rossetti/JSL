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
import jsl.utilities.random.rng.RNStreamFactory;
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
public class TestRNStreamFactory {

    RNStreamFactory f;

    @BeforeEach
    public void setUp() {
        System.out.println("Making new factory.");
        f = new RNStreamFactory();
        System.out.println(f);
    }

    @Test
    public void test1() {
        RNStreamFactory rm = new RNStreamFactory();
        RNStreamIfc rng = rm.getStream();
        double sum = 0.0;
        int n = 1000;
        for (int i = 1; i <= n; i++) {
            sum = sum + rng.randU01();
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("This test program should print the number   490.9254839801 \n");
        System.out.println("Actual test result = " + sum + "\n");
        assertTrue(sum == 490.9254839801);
    }

    @Test
    public void test2() {
        // test the advancement of streams
        int count = 100;
        int advance = 20;
        RNStreamFactory rm = new RNStreamFactory();
        rm.advanceSeeds(advance);
        RNStreamIfc rng = rm.getStream();
        double sum = 0.0;
        for (int i = 1; i <= count; i++) {
            sum = sum + rng.randU01();
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("This test program should print the number   55.445704270784404 \n");
        System.out.println("Actual test result = " + sum + "\n");
        assertTrue(sum == 55.445704270784404);

    }

    @Test
    public void subStreamTest(){
        // test the advancement of sub streams
        int count = 100;
        int advance = 20;
        RNStreamFactory rm = new RNStreamFactory();
        RNStreamIfc rng = rm.getStream();
        for (int i = 0; i < advance; i++) {
            rng.advanceToNextSubstream();
        }
        double sum = 0.0;
        for (int i = 1; i <= count; i++) {
            sum = sum + rng.randU01();
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("This test program should print the number   49.28122645558211 \n");
        System.out.println("Actual test result = " + sum + "\n");
        assertTrue(sum == 49.28122645558211);
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
        RNStreamFactory.RNStream g1 = (RNStreamFactory.RNStream) f.getStream("g1");

        System.out.println(g1);
        System.out.println();

        System.out.println("The current package seed is now:");
        System.out.println(Arrays.toString(f.getFactorySeed()));

        System.out.println();
        System.out.println("Creating the 2nd stream");

        RNStreamFactory.RNStream g2 = (RNStreamFactory.RNStream) f.getStream("g2");

        System.out.println(g2);

        System.out.println("The current package seed is now:");
        System.out.println(Arrays.toString(f.getFactorySeed()));
    }

    @Test
    public void test5() {
        // factories produce the same streams
        RNStreamFactory f1 = new RNStreamFactory("f1");
        RNStreamFactory f2 = new RNStreamFactory("f2");

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
        RNStreamFactory.getDefaultFactory().resetFactorySeed();
        System.out.println(RNStreamFactory.getDefaultFactory());

        List<ExponentialRV> list1 = new ArrayList<ExponentialRV>();
        ExponentialRV e;

        System.out.println("Making some Exponentials using default factory");
        RNStreamFactory defaultFactory = RNStreamFactory.getDefaultFactory();
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
        RNStreamFactory f1 = new RNStreamFactory("f1");

        System.out.println("Changing the default factory");
        RNStreamFactory.setDefaultFactory(f1);
        System.out.println(RNStreamFactory.getDefaultFactory());

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
        RNStreamFactory f1 = new RNStreamFactory("f1");
        System.out.println(f1);
        System.out.println("Clone the factory");
        RNStreamFactory fc = f1.newInstance();
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
        RNStreamFactory fc2 = f1.newInstance();
        System.out.println(fc2);
    }

    @Test
    public void test8() {
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("Test 8");
        System.out.println("Make a factory");
        RNStreamFactory f1 = new RNStreamFactory("f1");
        System.out.println(f1);
        System.out.println("Make a stream from f1");
        RNStreamFactory.RNStream rngf1 = (RNStreamFactory.RNStream) f1.getStream();
        System.out.println(rngf1);
        System.out.println("Generate 5 numbers from rngf1");
        for (int i = 1; i <= 5; i++) {
            System.out.println(rngf1.randU01());
        }
        System.out.println(rngf1);
        System.out.println("Current state of f1");
        System.out.println(f1);
        System.out.println("Clone the stream");
        RNStreamFactory.RNStream rngf2 = rngf1.newInstance("clone of rngf1");
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
        RNStreamFactory f1 = new RNStreamFactory("f1");
        System.out.println(f1);
        System.out.println("Make a stream from f1");
        RNStreamFactory.RNStream rngf1 = (RNStreamFactory.RNStream) f1.getStream();
        System.out.println(rngf1);
        System.out.println("Generate 5 numbers from rngf1");
        for (int i = 1; i <= 5; i++) {
            System.out.println(rngf1.randU01());
        }
        System.out.println(rngf1);
        System.out.println("Current state of f1");
        System.out.println(f1);
        System.out.println("get antithetic of stream");
        RNStreamFactory.RNStream rngf2 = rngf1.newAntitheticInstance("antithetic of rngf1");
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
