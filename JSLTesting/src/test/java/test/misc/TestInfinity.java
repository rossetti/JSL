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
package test.misc;

import jsl.utilities.math.JSLMath;

/**
 *
 * @author rossetti
 */
public class TestInfinity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        testing1();
    }

    public static void testing1() {
        JSLMath.printParameters(System.out);
        System.out.println("This is a test");
        if (Double.isInfinite(Double.NEGATIVE_INFINITY)) {
            System.out.println("Double.NEGATIVE_INFINITY is infinite");
        }

        if (Double.isInfinite(Double.POSITIVE_INFINITY)) {
            System.out.println("Double.POSITIVE_INFINITY is infinite");
        }

        if (Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY) {
            System.out.println("Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY");
        }

        if (Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY) {
            System.out.println("Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY");
        }

        if (0 < Double.POSITIVE_INFINITY) {
            System.out.println("0< Double.POSITIVE_INFINITY");
        }

        if (2 < Double.POSITIVE_INFINITY) {
            System.out.println("2< Double.POSITIVE_INFINITY");
        }

        if (0 > Double.NEGATIVE_INFINITY) {
            System.out.println("0> Double.NEGATIVE_INFINITY");
        }

        if (Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY) {
            System.out.println("Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY");
        }

        if (Double.NaN < Double.NEGATIVE_INFINITY) {
            System.out.println("Double.NaN < Double.NEGATIVE_INFINITY");
        } else if (Double.NaN > Double.NEGATIVE_INFINITY) {
            System.out.println("Double.NaN > Double.NEGATIVE_INFINITY");
        } else {
            System.out.println("Double.NaN = Double.NEGATIVE_INFINITY");
        }


        if (Double.NaN < Double.POSITIVE_INFINITY) {
            System.out.println("Double.NaN < Double.Double.POSITIVE_INFINITY");
        } else if (Double.NaN > Double.POSITIVE_INFINITY) {
            System.out.println("Double.NaN > Double.POSITIVE_INFINITY");
        } else {
            System.out.println("Double.NaN = Double.POSITIVE_INFINITY");
        }

        if (Double.NaN < 0) {
            System.out.println("Double.NaN < 0");
        } else if (Double.NaN > 0) {
            System.out.println("Double.NaN > 0");
        } else {
            System.out.println("Double.NaN = 0");
        }

        double value = Double.NaN;//Double.POSITIVE_INFINITY;
        double myLowerLimit = 0;//Double.NEGATIVE_INFINITY;
        double myUpperLimit = 2;//Double.POSITIVE_INFINITY;

        if ((value < myLowerLimit) || (value > myUpperLimit)) {
            throw new IllegalArgumentException("Invalid argument. supplied value was not in range, [" + myLowerLimit + "," + myUpperLimit + "]");
        }
    }
}
