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
package jsl.utilities.math;

import jsl.utilities.distributions.Gamma;

import java.io.PrintStream;
import java.util.*;

//TODO move all the array manipulation functionality to something like ArrayUtil
// or consolidate this kind of stuff in JSLUtil

/**
 * This class implements additional mathematical functions and determines the
 * parameters of the floating point representation.
 * <p>
 * This is based on the DhbMath class of Didier Besset in "Object-Oriented
 * Implementation of Numerical Methods", Morgan-Kaufmann
 */
public final class JSLMath {

    /**
     * holds initial factorials
     */
    private static double[] a = new double[33];

    private static int maxLnTop = 101;

    private static double[] lna = new double[maxLnTop];

    private static int ntop = 4;

    static {
        a[0] = 1.0;
        a[1] = 1.0;
        a[2] = 2.0;
        a[3] = 6.0;
        a[4] = 24.0;
    }

    /**
     * A constant that can be used in algorithms to specify the maximum number
     * of iterations. This is static and thus a change will change it for any
     * algorithm that depends on this constant
     * <p>
     */
    static private int maxNumIterations = 200;
    /**
     * Typical meaningful precision for numerical calculations.
     */
    static private double defaultNumericalPrecision = 0;
    /**
     * Typical meaningful small number for numerical calculations.
     */
    static private double smallNumber = 0;
    /**
     * Radix used by floating-point numbers.
     */
    static private int radix = 0;
    /**
     * Largest positive value which, when added to 1.0, yields 0.
     */
    static private double machinePrecision = 0;
    /**
     * Largest positive value which, when subtracted to 1.0, yields 0.
     */
    static private double negativeMachinePrecision = 0;
    /**
     * Smallest number different from zero.
     */
    static private double smallestNumber = 0;
    /**
     * Largest possible number
     */
    static private double largestNumber = 0;
    /**
     * Largest argument for the exponential
     */
    static private double largestExponentialArgument = 0;
    /**
     * Smallest argument for the exponential
     */
    static private double smallestExponentialArgument = 0;
    /**
     * Values used to compute human readable scales.
     */
    private static final double[] scales = {1.25, 2, 2.5, 4, 5, 7.5, 8, 10};
    private static final double[] semiIntegerScales = {2, 2.5, 4, 5, 7.5, 8, 10};
    private static final double[] integerScales = {2, 4, 5, 8, 10};

    private JSLMath(){};

    private static void computeLargestNumber() {
        double floatingRadix = getRadix();
        double fullMantissaNumber = 1.0d
                - floatingRadix * getNegativeMachinePrecision();
        while (!Double.isInfinite(fullMantissaNumber)) {
            largestNumber = fullMantissaNumber;
            fullMantissaNumber *= floatingRadix;
        }
    }

    private static void computeMachinePrecision() {
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d / floatingRadix;
        machinePrecision = 1.0d;
        double tmp = 1.0d + machinePrecision;
        while (tmp - 1.0d != 0.0d) {
            machinePrecision *= inverseRadix;
            tmp = 1.0d + machinePrecision;
        }
    }

    private static void computeNegativeMachinePrecision() {
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d / floatingRadix;
        negativeMachinePrecision = 1.0d;
        double tmp = 1.0d - negativeMachinePrecision;
        while (tmp - 1.0d != 0.0d) {
            negativeMachinePrecision *= inverseRadix;
            tmp = 1.0d - negativeMachinePrecision;
        }
    }

    private static void computeRadix() {
        double a = 1.0d;
        double tmp1, tmp2;
        do {
            a += a;
            tmp1 = a + 1.0d;
            tmp2 = tmp1 - a;
        } while (tmp2 - 1.0d != 0.0d);
        double b = 1.0d;
        while (radix == 0) {
            b += b;
            tmp1 = a + b;
            radix = (int) (tmp1 - a);
        }
    }

    private static void computeSmallestNumber() {
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d / floatingRadix;
        double fullMantissaNumber = 1.0d - floatingRadix * getNegativeMachinePrecision();
        while (fullMantissaNumber != 0.0d) {
            smallestNumber = fullMantissaNumber;
            fullMantissaNumber *= inverseRadix;
        }
    }

    /**
     * Gets the default numerical precision. This represents an estimate of the
     * precision expected for a general numerical computation. For example, two
     * numbers x and y can be considered equal if the relative difference
     * between them is less than the default numerical precision. This value has
     * been defined as the square root of the machine precision
     *
     * @return the default numerical precision
     */
    public static double getDefaultNumericalPrecision() {
        if (defaultNumericalPrecision == 0) {
            defaultNumericalPrecision = Math.sqrt(getMachinePrecision());
        }
        return defaultNumericalPrecision;
    }

    /**
     * Gets the default maximum number of iterations A constant that can be used
     * in algorithms to specify the maximum number of iterations. This is static
     * and thus a change will change it for any algorithm that depends on this
     * constant
     *
     * @return the maximum number of iterations
     */
    public static int getMaxNumIterations() {
        return (maxNumIterations);
    }

    /**
     * Sets the default maximum number of iterations A constant that can be used
     * in algorithms to specify the maximum number of iterations. This is static
     * and thus a change will change it for any algorithm that depends on this
     * constant
     *
     * @param iterations the number of iterations
     */
    public static void setMaxNumIterations(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("The number of iterations must be > 0, recommended at least 100.");
        }
        maxNumIterations = iterations;
    }

    /**
     * Compares two numbers a and b and checks if they are within the default
     * numerical precision of each other.
     *
     * @param a double
     * @param b double
     * @return boolean    true if the difference between a and b is less than the
     * default numerical precision
     */
    public static boolean equal(double a, double b) {
        return equal(a, b, getDefaultNumericalPrecision());
    }

    /**
     * Compares two numbers a and b and checks if they are within the supplied
     * precision of each other.
     *
     * @param a         double
     * @param b         double
     * @param precision double
     * @return boolean    true if the relative difference between a and b is less
     * than precision
     */
    public static boolean equal(double a, double b, double precision) {
        double norm = Math.max(Math.abs(a), Math.abs(b));
        return norm < precision || Math.abs(a - b) < precision * norm;
    }

    /**
     * Returns true if Math.abs(a-b) &lt; precision
     *
     * @param a         the first number
     * @param b         the second number
     * @param precision the precision to check
     * @return true if within the precision
     */
    public static boolean within(double a, double b, double precision) {
        return Math.abs(a - b) < precision;
    }

    /**
     * Computes the largest exponent argument
     *
     * @return the largest exponent argument
     */
    public static double getLargestExponentialArgument() {
        if (largestExponentialArgument == 0) {
            largestExponentialArgument = Math.log(getLargestNumber());
        }
        return largestExponentialArgument;
    }

    /**
     * Computes the smallest exponent argument
     *
     * @return the smallest exponent argument
     */
    public static double getSmallestExponentialArgument() {
        if (smallestExponentialArgument == 0) {
            smallestExponentialArgument = Math.log(getSmallestNumber());
            //smallestExponentialArgument = Math.log(getSmallNumber());
        }
        return smallestExponentialArgument;
    }

    /**
     * Gets the largest positive number that can be represented
     *
     * @return the largest positive number that can be represented
     */
    public static double getLargestNumber() {
        if (largestNumber == 0) {
            computeLargestNumber();
        }
        return largestNumber;
    }

    /**
     * Gets the largest positive number that when added to 1 yields 1
     *
     * @return the largest positive number that when added to 1 yields 1
     */
    public static double getMachinePrecision() {
        if (machinePrecision == 0) {
            computeMachinePrecision();
        }
        return machinePrecision;
    }

    /**
     * Gets the largest positive number that when subtracted from 1 yield 1
     *
     * @return the largest positive number that when subtracted from 1 yield 1
     */
    public static double getNegativeMachinePrecision() {
        if (negativeMachinePrecision == 0) {
            computeNegativeMachinePrecision();
        }
        return negativeMachinePrecision;
    }

    /**
     * Gets the radix of the floating point representation
     *
     * @return the radix of the floating point representation
     */
    public static int getRadix() {
        if (radix == 0) {
            computeRadix();
        }
        return radix;
    }

    /**
     * Gets the smallest positive number different from 0.0
     *
     * @return the smallest positive number different from 0.0
     */
    public static double getSmallestNumber() {
        if (smallestNumber == 0) {
            computeSmallestNumber();
        }
        return smallestNumber;
    }

    /**
     * Computes and prints the mathematical precision parameters to the supplied
     * PrintStream
     *
     * @param printStream the stream to write to
     */
    public static void printParameters(PrintStream printStream) {
        printStream.println("\nFloating-point machine parameters");
        printStream.println("---------------------------------");
        printStream.println("radix = " + getRadix());
        printStream.println("Machine precision = " + getMachinePrecision());
        printStream.println("Default precision = " + getDefaultNumericalPrecision());
        printStream.println("Negative machine precision = " + getNegativeMachinePrecision());
        printStream.println("Smallest positive number = " + getSmallestNumber());
        printStream.println("Largest positive number = " + getLargestNumber());
        printStream.println("Small number = " + getSmallNumber());
        printStream.println("Largest exponential argument = " + getLargestExponentialArgument());
        printStream.println("Smallest exponential argument = " + getSmallestExponentialArgument());
        printStream.println("1.0 - getMachinePrecision() = " + (1.0 - getMachinePrecision()));
        printStream.println("0.0 + getMachinePrecision() = " + (0.0 + getMachinePrecision()));
        printStream.println("1.0 - getDefaultNumericalPrecision() = " + (1.0 - getDefaultNumericalPrecision()));
        printStream.println("0.0 + getDefaultNumericalPrecision() = " + (0.0 + getDefaultNumericalPrecision()));
        return;
    }

    /**
     * Resets the constants. They will be recomputed at next usage
     */
    public static void reset() {
        defaultNumericalPrecision = 0;
        smallNumber = 0;
        radix = 0;
        machinePrecision = 0;
        negativeMachinePrecision = 0;
        smallestNumber = 0;
        largestNumber = 0;
    }

    /**
     * This method returns the specified value rounded to the nearest integer
     * multiple of the specified scale.
     *
     * @param value number to be rounded
     * @param scale defining the rounding scale
     * @return rounded value
     */
    public static double roundTo(double value, double scale) {
        return Math.round(value / scale) * scale;
    }

    /**
     * Round the specified value upward to the next scale value.
     *
     * @param value         the value to be rounded.
     * @param integerValued flag specified whether integer scale are used,
     *                      otherwise double scale is used.
     * @return a number rounded upward to the next scale value.
     */
    public static double roundToScale(double value, boolean integerValued) {
        double[] scaleValues;
        int orderOfMagnitude = (int) Math.floor(Math.log(value) / Math.log(10.0));
        if (integerValued) {
            orderOfMagnitude = Math.max(1, orderOfMagnitude);
            if (orderOfMagnitude == 1) {
                scaleValues = integerScales;
            } else if (orderOfMagnitude == 2) {
                scaleValues = semiIntegerScales;
            } else {
                scaleValues = scales;
            }
        } else {
            scaleValues = scales;
        }
        double exponent = Math.pow(10.0, orderOfMagnitude);
        double rValue = value / exponent;
        for (int n = 0; n < scaleValues.length; n++) {
            if (rValue <= scaleValues[n]) {
                return scaleValues[n] * exponent;
            }
        }
        return exponent;    // Should never reach here
    }

    /**
     * Returns the number that can be added to some value without noticeably
     * changing the result of the computation
     *
     * @return the number that can be added to some value without noticeably
     * changing the result of the computation
     */
    public static double getSmallNumber() {
        if (smallNumber == 0) {
            smallNumber = Math.sqrt(getSmallestNumber());
        }
        return smallNumber;
    }

    /**
     * Get the sign of the number based on the equal() method Equal is 0.0,
     * positive is 1.0, negative is -1.0
     *
     * @param x the number
     * @return the sign of the number
     */
    public static double sign(double x) {
        if (equal(0.0, x)) {
            return (0.0);
        }

        if (x > 0.0) {
            return (1.0);
        } else {
            return (-1.0);
        }

    }

    /**
     * Returns the factorial (n!) of the number
     *
     * @param n The number to take the factorial of
     * @return The factorial of the number.
     */
    public static double factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Argument must be > 0");
        }

        if (n > 32) {
            return Math.exp(Gamma.logGammaFunction(n + 1.0));
        }

        int j;
        while (ntop < n) {
            j = ntop++;
            a[ntop] = a[j] * ntop;
        }
        return (a[n]);
    }

    /**
     * Computes the binomial coefficient. Computes the number of combinations of
     * size k that can be formed from n distinct objects.
     *
     * @param n The total number of distinct items
     * @param k The number of subsets
     * @return the binomial coefficient
     */
    public static double binomialCoefficient(int n, int k) {
        return (Math.floor(0.5 + Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n - k))));
    }

    /**
     * Computes the natural logarithm of the factorial operator. ln(n!)
     *
     * @param n The value to be operated on.
     * @return the log of the factorial
     */
    public static double logFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Argument must be > 0");
        }

        if (n <= 1) {
            return (0.0);
        }

        if (n < maxLnTop) {
            if (lna[n] > 0) // already been computed
            {
                return (lna[n]); // just return it
            } else {
                lna[n] = Gamma.logGammaFunction(n + 1.0); // compute it, save it, return it
                return (lna[n]);
            }
        } else {
            return (Gamma.logGammaFunction(n + 1.0));
        }
    }

}
