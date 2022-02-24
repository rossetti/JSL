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
import jsl.utilities.random.rng.RNStreamProvider;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticXY;

/**
 *
 * @author rossetti
 */
public class UseRNG {

    public static void main(String[] args) {
//        test();
//        example1();
       // crnExample1();
        //crnExample2();
 //       antitheticExample1();
 //       antitheticExample2();
 //       test1();
        //notCRN();
        //antithetic();
    }

    public static void test(){
        RNStreamProvider p1 = new RNStreamProvider();
        RNStreamIfc stream = p1.defaultRNStream();
        for (int i = 0; i < 9; i++) {
            System.out.println(stream.randU01());
        }
        System.out.println();
        RNStreamIfc stream2 = p1.nextRNStream();
        for (int i = 0; i < 9; i++) {
            System.out.println(stream2.randU01());
        }
    }


    public static void notCRN() {

        Statistic d = new Statistic("X - Y");
        StatisticXY statXY = new StatisticXY("Stat X Y");
//        Normal n1 = new Normal(2, 1, f1s1);
//        Normal n2 = new Normal(2.1, 1, f2s1);
        NormalRV n1 = new NormalRV(2, 1);
        NormalRV n2 = new NormalRV(2.1, 1);
        for (int i = 0; i < 10; i++) {
            double x = n1.getValue();
            double y = n2.getValue();
            d.collect(x-y);
            statXY.collectXY(x, y);
            System.out.println("x = " + x + " y = " + y);
        }
        
        System.out.println("CRN");
        System.out.println(d);
        System.out.println("=============================");
        System.out.println(statXY);
        System.out.println("=============================");
    }

    /**
     *
     */
    public static void antithetic() {
        // make a factory for creating streams
        RNStreamProvider f1 = new RNStreamProvider();

        // get the first stream from the factory
        RNStreamIfc f1s1 = f1.nextRNStream();

        // make another factory, the factories are identical
        RNStreamProvider f2 = new RNStreamProvider();

        // thus the first streams returned are identical
        RNStreamIfc f2s1 = f2.nextRNStream();
        
        // now tell the stream to produce antithetic random numbers
        // f2s1 and f1s1 are now antithetic to each other
        f2s1.setAntitheticOption(true);

        for (int i = 0; i < 10; i++) {
            System.out.println("f1s1 = " + f1s1.randU01() + " f2s1 = " + f2s1.randU01());
        }

        Statistic d = new Statistic("(X + Y)/2");
        StatisticXY statXY = new StatisticXY("Stat X Y");
        NormalRV n1 = new NormalRV(2, 1, f1s1);
        NormalRV n2 = new NormalRV(2, 1, f2s1);
        for (int i = 0; i < 10; i++) {
            double x = n1.getValue();
            double y = n2.getValue();
            d.collect((x+y)/2.0);
            statXY.collectXY(x, y);
            System.out.println("x = " + x + " y = " + y);
        }
        
        System.out.println("Antithetic");
        System.out.println(d);
        System.out.println("=============================");
        System.out.println(statXY);
        System.out.println("=============================");

    }

    public static void test1() {
        RNStreamIfc defaultStream = JSLRandom.getDefaultRNStream();

        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + defaultStream.randU01());
        }

        RNStreamProvider f = new RNStreamProvider();
        RNStreamIfc s1 = f.defaultRNStream();
        System.out.println("default stream");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }

        s1.advanceToNextSubstream();
        System.out.println("advanced");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }

        s1.resetStartStream();
        System.out.println("reset");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }
        RNStreamIfc s2 = f.nextRNStream();
        System.out.println("2nd stream");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s2.randU01());
        }
    }
}
