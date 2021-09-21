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
package jsl.utilities.random.rvariable;

import jsl.utilities.math.JSLMath;
import jsl.utilities.distributions.*;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.RNStreamProvider;
import jsl.utilities.random.rng.RNStreamProviderIfc;

import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;

import static jsl.utilities.distributions.Gamma.invChiSquareDistribution;
import static jsl.utilities.distributions.Gamma.logGammaFunction;
import static jsl.utilities.distributions.Normal.stdNormalInvCDF;

/**
 * The purpose of this class is to facilitate random variate generation from
 * various distributions through a set of static class methods.
 * <p>
 * Each method marked rXXXX will generate random variates from the named
 * distribution. The user has the option of supplying a RNStreamIfc as the source of
 * the randomness. Methods that do not have a RNStreamIfc parameter use,
 * getDefaultRNStream() as the source of randomness. That is, they all <b>share</b> the same
 * stream, which is the default stream from the default random number stream factory.
 * The user has the option of supplying a stream number to identify the stream
 * from the underlying stream provider. By default, stream 1 is the default stream
 * for the default provider. Stream 2 refers to the 2nd stream, etc.
 * <p>
 * Also provides a number of methods for sampling with and without replacement
 * from arrays and lists as well as creating permutations of arrays and lists.
 *
 * @author rossetti
 */
public class JSLRandom {

    public enum AlgoType {Inverse, AcceptanceRejection}

    private static Beta myBeta;

    private static RNStreamProviderIfc myStreamProvider = new RNStreamProvider();

    private JSLRandom() {
    }

    /**
     * Sets the underlying stream provider for all JSLRandom method usage
     *
     * @param streamProvider an instance of a stream provider
     */
    public static void setRNStreamProvider(RNStreamProviderIfc streamProvider) {
        Objects.requireNonNull(streamProvider, "The stream provider cannot be null");
        myStreamProvider = streamProvider;
    }

    /**
     * @return the provider that is currently being used for all JSLRandom method calls
     */
    public static RNStreamProviderIfc getRNStreamProvider() {
        return myStreamProvider;
    }

    /**
     * @return gets the next stream of pseudo random numbers from the default random
     * number stream provider
     */
    public static RNStreamIfc nextRNStream() {
        return myStreamProvider.nextRNStream();
    }

    /**
     *
     * @param stream the stream associated with the default stream provider
     * @return the number associated with the provided stream or -1 if the stream was not provided by the default provider
     */
    public static int getStreamNumber(RNStreamIfc stream){ return myStreamProvider.getStreamNumber(stream);}

    /**
     *
     * @param streamNum the stream number associated with the stream
     * @return the stream associated with the stream number from the underlying stream provider
     */
    public static RNStreamIfc rnStream(int streamNum){
        return myStreamProvider.rnStream(streamNum);
    }

    /**
     * @return the default stream from the default random number stream provider
     */
    public static RNStreamIfc getDefaultRNStream() {
        return myStreamProvider.defaultRNStream();
    }

    /**
     * @return returns a new stream from the default stream factory using the Stream API
     */
    public static DoubleStream createDoubleStream() {
        return nextRNStream().asDoubleStream();
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @return the random value
     */
    public static double rBernoulli(double pSuccess) {
        return rBernoulli(pSuccess, getDefaultRNStream());
    }

    /**
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rBernoulli(double pSuccess, int streamNum) {
        return rBernoulli(pSuccess, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @param rng      the RNStreamIfc
     * @return the random value
     */
    public static double rBernoulli(double pSuccess, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if ((pSuccess <= 0.0) || (pSuccess >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be (0,1)");
        }

        if (rng.randU01() <= pSuccess) {
            return (1.0);
        } else {
            return (0.0);
        }
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @param nTrials  the number of trials, must be greater than 0
     * @return the random value
     */
    public static int rBinomial(double pSuccess, int nTrials) {
        return rBinomial(pSuccess, nTrials, getDefaultRNStream());
    }

    /**
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param nTrials   the number of trials, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static int rBinomial(double pSuccess, int nTrials, int streamNum) {
        return rBinomial(pSuccess, nTrials, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @param nTrials  the number of trials, must be greater than 0
     * @param rng      the RNStreamIfc, must not be null
     * @return the random value
     */
    public static int rBinomial(double pSuccess, int nTrials, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (nTrials <= 0) {
            throw new IllegalArgumentException("Number of trials must be >= 1");
        }
        if ((pSuccess <= 0.0) || (pSuccess >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be (0,1)");
        }
        return Binomial.binomialInvCDF(rng.randU01(), nTrials, pSuccess);
    }

    /**
     * @param mean the mean of the Poisson, must be greater than 0
     * @return the random value
     */
    public static int rPoisson(double mean) {
        return rPoisson(mean, getDefaultRNStream());
    }

    /**
     * @param mean      the mean of the Poisson, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static int rPoisson(double mean, int streamNum) {
        return rPoisson(mean, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param mean the mean of the Poisson, must be greater than 0
     * @param rng  the RNStreamIfc, must not be null
     * @return the random value
     */
    public static int rPoisson(double mean, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        return Poisson.poissonInvCDF(rng.randU01(), mean);
    }

    /**
     * Generates a discrete uniform over the range
     *
     * @param minimum the minimum of the range
     * @param maximum the maximum of the range
     * @return the random value
     */
    public static int rDUniform(int minimum, int maximum) {
        return rDUniform(minimum, maximum, getDefaultRNStream());
    }

    /**
     * Generates a discrete uniform over the range
     *
     * @param minimum   the minimum of the range
     * @param maximum   the maximum of the range
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static int rDUniform(int minimum, int maximum, int streamNum) {
        return rDUniform(minimum, maximum, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Generates a discrete uniform over the range
     *
     * @param minimum the minimum of the range
     * @param maximum the maximum of the range
     * @param rng     the RNStreamIfc, must not be null
     * @return the random value
     */
    public static int rDUniform(int minimum, int maximum, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        return rng.randInt(minimum, maximum);
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @return the random value
     */
    public static int rGeometric(double pSuccess) {
        return rGeometric(pSuccess, getDefaultRNStream());
    }

    /**
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static int rGeometric(double pSuccess, int streamNum) {
        return rGeometric(pSuccess, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param pSuccess the probability of success, must be in (0,1)
     * @param rng      the RNStreamIfc, must not be null
     * @return the random value
     */
    public static int rGeometric(double pSuccess, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if ((pSuccess < 0.0) || (pSuccess > 1.0)) {
            throw new IllegalArgumentException("Success Probability must be [0,1]");
        }
        double u = rng.randU01();
        return ((int) Math.ceil((Math.log(1.0 - u) / (Math.log(1.0 - pSuccess))) - 1.0));
    }

    /**
     * @param pSuccess   the probability of success, must be in (0,1)
     * @param rSuccesses number of trials until rth success, must be greater than 0
     * @return the random value
     */
    public static int rNegBinomial(double pSuccess, double rSuccesses) {
        return rNegBinomial(pSuccess, rSuccesses, getDefaultRNStream());
    }

    /**
     * @param pSuccess   the probability of success
     * @param rSuccesses number of trials until rth success
     * @param streamNum  the stream number from the stream provider to use
     * @return the random value
     */
    public static int rNegBinomial(double pSuccess, double rSuccesses, int streamNum) {
        return rNegBinomial(pSuccess, rSuccesses, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param pSuccess   the probability of success
     * @param rSuccesses number of trials until rth success
     * @param rng        the RNStreamIfc, must not be null
     * @return the random value
     */
    public static int rNegBinomial(double pSuccess, double rSuccesses, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        return NegativeBinomial.negBinomialInvCDF(rng.randU01(), pSuccess, rSuccesses);
    }

    /**
     * Generates a continuous U(0,1) using the default stream
     *
     * @return the random value
     */
    public static double rUniform() {
        return rUniform(0.0, 1.0, getDefaultRNStream());
    }

    /**
     * Generates a continuous U(0,1) using the supplied stream number
     *
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rUniform(int streamNum) {
        return rUniform(0.0, 1.0, streamNum);
    }

    /**
     * Generates a continuous U(0,1) using the supplied stream
     *
     * @param rnStream the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rUniform(RNStreamIfc rnStream) {
        return rUniform(0.0, 1.0, rnStream);
    }

    /**
     * Generates a continuous uniform over the range
     *
     * @param minimum the minimum of the range, must be less than maximum
     * @param maximum the maximum of the range
     * @return the random value
     */
    public static double rUniform(double minimum, double maximum) {
        return rUniform(minimum, maximum, getDefaultRNStream());
    }

    /**
     * Generates a continuous uniform over the range
     *
     * @param minimum   the minimum of the range, must be less than maximum
     * @param maximum   the maximum of the range
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rUniform(double minimum, double maximum, int streamNum) {
        return rUniform(minimum, maximum, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Generates a continuous uniform over the range
     *
     * @param minimum the minimum of the range, must be less than maximum
     * @param maximum the maximum of the range
     * @param rng     the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rUniform(double minimum, double maximum, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (minimum >= maximum) {
            throw new IllegalArgumentException("Lower limit must be < upper "
                    + "limit. lower limit = " + minimum + " upper limit = " + maximum);
        }

        return minimum + (maximum - minimum) * rng.randU01();
    }

    /**
     * Generates a N(0,1) random value using the default stream
     *
     * @return the random value
     */
    public static double rNormal() {
        return rNormal(0.0, 1.0, getDefaultRNStream());
    }

    /**
     * Generates a N(0,1) random value using the supplied stream number
     *
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rNormal(int streamNum) {
        return rNormal(0.0, 1.0, streamNum);
    }

    /**
     * Generates a N(0,1) random value using the supplied stream
     *
     * @param rng the RNStreamIfc, must not null
     * @return the random value
     */
    public static double rNormal(RNStreamIfc rng) {
        return rNormal(0.0, 1.0, rng);
    }

    /**
     * @param mean     the mean of the normal
     * @param variance the variance of the normal, must be greater than 0
     * @return the random value
     */
    public static double rNormal(double mean, double variance) {
        return rNormal(mean, variance, getDefaultRNStream());
    }

    /**
     * @param mean      the mean of the normal
     * @param variance  the variance of the normal, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rNormal(double mean, double variance, int streamNum) {
        return rNormal(mean, variance, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param mean     the mean of the normal
     * @param variance the variance of the normal, must be greater than 0
     * @param rng      the RNStreamIfc, must not null
     * @return the random value
     */
    public static double rNormal(double mean, double variance, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        double z = stdNormalInvCDF(rng.randU01());
        double stdDev = Math.sqrt(variance);
        return (z * stdDev + mean);
    }

    /**
     * @param mean     the mean of the lognormal, must be greater than 0
     * @param variance the variance of the lognormal, must be greater than 0
     * @return the random value
     */
    public static double rLogNormal(double mean, double variance) {
        return rLogNormal(mean, variance, getDefaultRNStream());
    }

    /**
     * @param mean      the mean of the lognormal, must be greater than 0
     * @param variance  the variance of the lognormal, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rLogNormal(double mean, double variance, int streamNum) {
        return rLogNormal(mean, variance, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param mean     the mean of the lognormal, must be greater than 0
     * @param variance the variance of the lognormal, must be greater than 0
     * @param rng      the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rLogNormal(double mean, double variance, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive");
        }
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        double z = Normal.stdNormalInvCDF(rng.randU01());
        double d = variance + mean * mean;
        double t = mean * mean;
        double normalMu = Math.log((t) / Math.sqrt(d));
        double normalSigma = Math.sqrt(Math.log(d / t));
        double x = z * normalSigma + normalMu;
        return (Math.exp(x));
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @return the random value
     */
    public static double rWeibull(double shape, double scale) {
        return rWeibull(shape, scale, getDefaultRNStream());
    }

    /**
     * @param shape     the shape, must be greater than 0
     * @param scale     the scale, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rWeibull(double shape, double scale, int streamNum) {
        return rWeibull(shape, scale, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @param rng   the RNStreamIfc, must not null
     * @return the random value
     */
    public static double rWeibull(double shape, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        checkShapeAndScale(shape, scale);
        double u = rng.randU01();
        return scale * Math.pow(-Math.log(1.0 - u), 1.0 / shape);
    }

    /**
     * Throws an exception if shape or scale are invalid
     *
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     */
    private static void checkShapeAndScale(double shape, double scale) {
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape parameter must be positive");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale parameter must be positive");
        }
    }

    /**
     * @param mean the mean, must be greater than 0
     * @return the random value
     */
    public static double rExponential(double mean) {
        return rExponential(mean, getDefaultRNStream());
    }

    /**
     * @param mean      the mean, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rExponential(double mean, int streamNum) {
        return rExponential(mean, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param mean the mean, must be greater than 0
     * @param rng  the RNStreamIfc, must not null
     * @return the random value
     */
    public static double rExponential(double mean, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (mean <= 0.0) {
            throw new IllegalArgumentException("Exponential mean must be > 0.0");
        }
        double u = rng.randU01();
        return (-mean * Math.log(1.0 - u));
    }

    /**
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter
     * @param min    the min
     * @param max    the max
     * @return the generated value
     */
    public static double rJohnsonB(double alpha1, double alpha2,
                                   double min, double max) {
        return rJohnsonB(alpha1, alpha2, min, max, getDefaultRNStream());
    }

    /**
     * @param alpha1    alpha1 parameter
     * @param alpha2    alpha2 parameter, must be greater than zero
     * @param min       the min, must be less than max
     * @param max       the max
     * @param streamNum the stream number from the stream provider to use
     * @return the generated value
     */
    public static double rJohnsonB(double alpha1, double alpha2,
                                   double min, double max, int streamNum) {
        return rJohnsonB(alpha1, alpha2, min, max, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter, must be greater than zero
     * @param min    the min, must be less than max
     * @param max    the max
     * @param rng    the RNStreamIfc, must not be null
     * @return the generated value
     */
    public static double rJohnsonB(double alpha1, double alpha2,
                                   double min, double max, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("alpha2 must be > 0");
        }
        if (max <= min) {
            throw new IllegalArgumentException("the min must be < than the max");
        }
        double u = rng.randU01();
        double z = Normal.stdNormalInvCDF(u);
        double y = Math.exp((z - alpha1) / alpha2);
        return (min + max * y) / (y + 1.0);
    }

    /**
     * @param shape the shape
     * @param scale the scale
     * @return the generated value
     */
    public static double rLogLogistic(double shape, double scale) {
        return rLogLogistic(shape, scale, getDefaultRNStream());
    }

    /**
     * @param shape     the shape, must be greater than 0
     * @param scale     the scale, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the generated value
     */
    public static double rLogLogistic(double shape, double scale, int streamNum) {
        return rLogLogistic(shape, scale, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @param rng   the RNStreamIfc, must not be null
     * @return the generated value
     */
    public static double rLogLogistic(double shape, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        checkShapeAndScale(shape, scale);
        double u = rng.randU01();
        double c = u / (1.0 - u);
        return (scale * Math.pow(c, 1.0 / shape));
    }

    /**
     * @param min  the min, must be less than or equal to mode
     * @param mode the mode, must be less than or equal to max
     * @param max  the max
     * @return the random value
     */
    public static double rTriangular(double min, double mode,
                                     double max) {
        return rTriangular(min, mode, max, getDefaultRNStream());
    }

    /**
     * @param min       the min, must be less than or equal to mode
     * @param mode      the mode, must be less than or equal to max
     * @param max       the max
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rTriangular(double min, double mode,
                                     double max, int streamNum) {
        return rTriangular(min, mode, max, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param min  the min, must be less than or equal to mode
     * @param mode the mode, must be less than or equal to max
     * @param max  the max
     * @param rng  the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rTriangular(double min, double mode,
                                     double max, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (min > mode) {
            throw new IllegalArgumentException("min must be <= mode");
        }
        if (min >= max) {
            throw new IllegalArgumentException("min must be < max");
        }
        if (mode > max) {
            throw new IllegalArgumentException("mode must be <= max");
        }
        double range = max - min;
        double c = (mode - min) / range;
        // get the invCDF for a triang(0,c,1)
        double x;
        double p = rng.randU01();
        if (c == 0.0) { // left triangular, mode equals min
            x = 1.0 - Math.sqrt(1 - p);
        } else if (c == 1.0) { //right triangular, mode equals max
            x = Math.sqrt(p);
        } else if (p < c) {
            x = Math.sqrt(c * p);
        } else {
            x = 1.0 - Math.sqrt((1.0 - c) * (1.0 - p));
        }
        // scale it back to original scale
        return (min + range * x);
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @return the generated value
     */
    public static double rGamma(double shape, double scale) {
        return rGamma(shape, scale, getDefaultRNStream());
    }

    /**
     * @param shape the shape, must be greater than 0.0
     * @param scale the scale, must be greater than 0.0
     * @param rng   the RNStreamIfc, must not null
     * @return the generated value
     */
    public static double rGamma(double shape, double scale, RNStreamIfc rng) {
        return rGamma(shape, scale, rng, AlgoType.Inverse);
    }

    /**
     * @param shape     the shape, must be greater than 0.0
     * @param scale     the scale, must be greater than 0.0
     * @param streamNum the stream number from the stream provider to use
     * @return the generated value
     */
    public static double rGamma(double shape, double scale, int streamNum) {
        return rGamma(shape, scale, streamNum, AlgoType.Inverse);
    }

    /**
     * @param shape     the shape, must be greater than 0.0
     * @param scale     the scale, must be greater than 0.0
     * @param streamNum the stream number from the stream provider to use
     * @param type,     must be appropriate algorithm type, if null then inverse transform is the default
     * @return the generated value
     */
    public static double rGamma(double shape, double scale, int streamNum, AlgoType type) {
        return rGamma(shape, scale, myStreamProvider.rnStream(streamNum), type);
    }

    /**
     * @param shape the shape, must be greater than 0.0
     * @param scale the scale, must be greater than 0.0
     * @param rng   the RNStreamIfc, must not null
     * @param type, must be appropriate algorithm type, if null then inverse transform is the default
     * @return the generated value
     */
    public static double rGamma(double shape, double scale, RNStreamIfc rng, AlgoType type) {
        if (type == AlgoType.AcceptanceRejection) {
            return rARGamma(shape, scale, rng);
        } else {
            return rInvGamma(shape, scale, rng);
        }
    }

    /**
     * Uses the inverse transform technique for generating from the gamma
     *
     * @param shape the shape, must be greater than 0.0
     * @param scale the scale, must be greater than 0.0
     * @param rng   the RNStreamIfc, must not null
     * @return the generated value
     */
    private static double rInvGamma(double shape, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        checkShapeAndScale(shape, scale);
        double p = rng.randU01();
        double x;
        /* ...special case: exponential distribution */
        if (shape == 1.0) {
            x = -scale * Math.log(1.0 - p);
            return (x);
        }
        /* ...compute the gamma(alpha, beta) inverse.                   *
         *    ...compute the chi-square inverse with 2*alpha degrees of *
         *       freedom, which is equivalent to gamma(alpha, 2).       */
        double v = 2.0 * shape;
        double g = logGammaFunction(shape);
        double chi2 = invChiSquareDistribution(p, v, g,
                Gamma.DEFAULT_MAX_ITERATIONS, JSLMath.getDefaultNumericalPrecision());
        /* ...transfer chi-square to gamma. */
        x = scale * chi2 / 2.0;
        return (x);
    }

    /**
     * Uses the acceptance-rejection technique for generating from the gamma
     *
     * @param shape the shape, must be greater than 0.0
     * @param scale the scale, must be greater than 0.0
     * @param rng   the RNStreamIfc, must not null
     * @return the generated value
     */
    private static double rARGamma(double shape, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        checkShapeAndScale(shape, scale);
        // first get gamma(shape, 1)
        double x = rARGammaScaleEQ1(shape, rng);
        // now scale to proper scale
        return (x * scale);
    }

    /**
     * Generates a gamma(shape, scale=1) random variable via acceptance rejection. Uses
     * Ahrens and Dieter (1974) for shape between 0 and 1 and uses Marsaglia and Tsang (2000) for
     * shape greater than 1
     *
     * @param shape the shape, must be positive
     * @param rng   the random number stream, must not be null
     * @return the randomly generated value
     */
    private static double rARGammaScaleEQ1(double shape, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape parameter must be positive");
        }
        if ((0.0 < shape) && (shape < 1.0)) {
            return rARGammaScaleEQ1ShapeBTW01(shape, rng);
        } else {
            return rARGammaScaleEQ1ShapeGT1(shape, rng);
        }
    }

    /**
     * Generates a gamma(shape, scale=1) random variable via acceptance rejection. Uses
     * Ahrens and Dieter (1974)
     *
     * @param shape the shape, must be in (0,1)
     * @param rng   the random number stream, must not be null
     * @return the randomly generated value
     */
    private static double rARGammaScaleEQ1ShapeBTW01(double shape, RNStreamIfc rng) {
        double e = Math.E;
        double b = (e + shape) / e;
        while (true) {
            double u1 = rng.randU01();
            double p = b * u1;
            if (p > 1) {
                double y = -Math.log((b - p) / shape);
                double u2 = rng.randU01();
                if (u2 <= Math.pow(y, shape - 1.0)) {
                    return y;
                }
            } else {
                double y = Math.pow(p, 1.0 / shape);
                double u2 = rng.randU01();
                if (u2 <= Math.exp(-y)) {
                    return y;
                }
            }
        }
    }

    /**
     * Generates a gamma(shape, scale=1) random variable via acceptance rejection. Uses
     * uses Marsaglia and Tsang (2000) for shape greater than 1
     *
     * @param shape the shape, must be greater than 1
     * @param rng   the random number stream, must not be null
     * @return the randomly generated value
     */
    private static double rARGammaScaleEQ1ShapeGT1(double shape, RNStreamIfc rng) {
        double d = shape - 1. / 3.;
        double c = 1. / Math.sqrt(9. * d);
        for (; ; ) {
            double x, v, u;
            do {
                x = rNormal(0, 1, rng);
                v = 1. + (c * x);
            } while (v <= 0.);
            v = v * v * v;
            u = rng.randU01();
            if (u < 1. - .0331 * (x * x) * (x * x)) return (d * v);
            if (Math.log(u) < 0.5 * x * x + d * (1. - v + Math.log(v))) return (d * v);
        }
    }

    /**
     * @param dof degrees of freedom, must be greater than 0
     * @return the random value
     */
    public static double rChiSquared(double dof) {
        return rChiSquared(dof, getDefaultRNStream());
    }

    /**
     * @param dof       degrees of freedom, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rChiSquared(double dof, int streamNum) {
        return rChiSquared(dof, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param dof degrees of freedom, must be greater than 0
     * @param rng the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rChiSquared(double dof, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (dof <= 0) {
            throw new IllegalArgumentException("The degrees of freedom should be > 0");
        }
        return Gamma.invChiSquareDistribution(rng.randU01(), dof);
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @return the generated value
     */
    public static double rPearsonType5(double shape, double scale) {
        return rPearsonType5(shape, scale, getDefaultRNStream());
    }

    /**
     * @param shape     the shape, must be greater than 0
     * @param scale     the scale, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the generated value
     */
    public static double rPearsonType5(double shape, double scale, int streamNum) {
        return rPearsonType5(shape, scale, myStreamProvider.rnStream(streamNum));
    }

    /**
     * @param shape the shape, must be greater than 0
     * @param scale the scale, must be greater than 0
     * @param rng   the RNStreamIfc, must not be null
     * @return the generated value
     */
    public static double rPearsonType5(double shape, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        checkShapeAndScale(shape, scale);
        double GShape = shape;
        double GScale = 1.0 / scale;
        double y = rGamma(GShape, GScale, rng);
        return 1.0 / y;
    }

    /**
     * This beta is restricted to the range of (0,1)
     *
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter
     * @return the random value
     */
    public static double rBeta(double alpha1, double alpha2) {
        return rBeta(alpha1, alpha2, getDefaultRNStream());
    }

    /**
     * This beta is restricted to the range of (0,1)
     *
     * @param alpha1    alpha1 parameter
     * @param alpha2    alpha2 parameter
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rBeta(double alpha1, double alpha2, int streamNum) {
        return rBeta(alpha1, alpha2, myStreamProvider.rnStream(streamNum));
    }

    /**
     * This beta is restricted to the range of (0,1)
     *
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter
     * @param rng    the RNStreamIfc
     * @return the random value
     */
    public static double rBeta(double alpha1, double alpha2, RNStreamIfc rng) {
        if (myBeta == null) {
            myBeta = new Beta(alpha1, alpha2);
        }
        myBeta.setParameters(alpha1, alpha2);
        return myBeta.invCDF(rng.randU01());
    }

    /**
     * This beta is restricted to the range of (minimum,maximum)
     *
     * @param alpha1  alpha1 parameter
     * @param alpha2  alpha2 parameter
     * @param minimum the minimum of the range
     * @param maximum the maximum of the range
     * @return the random value
     */
    public static double rBetaG(double alpha1, double alpha2,
                                double minimum, double maximum) {
        return rBetaG(alpha1, alpha2, minimum, maximum, getDefaultRNStream());
    }

    /**
     * This beta is restricted to the range of (minimum,maximum)
     *
     * @param alpha1    alpha1 parameter
     * @param alpha2    alpha2 parameter
     * @param minimum   the minimum of the range, must be less than maximum
     * @param maximum   the maximum of the range
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rBetaG(double alpha1, double alpha2,
                                double minimum, double maximum, int streamNum) {
        return rBetaG(alpha1, alpha2, minimum, maximum, myStreamProvider.rnStream(streamNum));
    }

    /**
     * This beta is restricted to the range of (minimum,maximum)
     *
     * @param alpha1  alpha1 parameter
     * @param alpha2  alpha2 parameter
     * @param minimum the minimum of the range, must be less than maximum
     * @param maximum the maximum of the range
     * @param rng     the RNStreamIfc
     * @return the random value
     */
    public static double rBetaG(double alpha1, double alpha2,
                                double minimum, double maximum, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (minimum >= maximum) {
            throw new IllegalArgumentException("Lower limit must be < upper "
                    + "limit. lower limit = " + minimum + " upper limit = " + maximum);
        }
        double x = rBeta(alpha1, alpha2, rng);
        return minimum + (maximum - minimum) * x;
    }

    /**
     * Pearson Type 6
     *
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter
     * @param beta   the beta parameter
     * @return the random value
     */
    public static double rPearsonType6(double alpha1, double alpha2,
                                       double beta) {
        return rPearsonType6(alpha1, alpha2, beta, getDefaultRNStream());
    }

    /**
     * Pearson Type 6
     *
     * @param alpha1    alpha1 parameter
     * @param alpha2    alpha2 parameter
     * @param beta      the beta parameter, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rPearsonType6(double alpha1, double alpha2,
                                       double beta, int streamNum) {
        return rPearsonType6(alpha1, alpha2, beta, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Pearson Type 6
     *
     * @param alpha1 alpha1 parameter
     * @param alpha2 alpha2 parameter
     * @param beta   the beta parameter, must be greater than 0
     * @param rng    the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rPearsonType6(double alpha1, double alpha2,
                                       double beta, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (beta <= 0.0) {
            throw new IllegalArgumentException("The scale parameter must be > 0.0");
        }
        double fib = rBeta(alpha1, alpha2, rng);
        return (beta * fib) / (1.0 - fib);
    }

    /**
     * Generates according to a Laplace(mean, scale)
     *
     * @param mean  mean or location parameter
     * @param scale scale parameter, must be greater than 0
     * @return the random value
     */
    public static double rLaplace(double mean, double scale) {
        return rLaplace(mean, scale, getDefaultRNStream());
    }

    /**
     * Generates according to a Laplace(mean, scale)
     *
     * @param mean      mean or location parameter
     * @param scale     scale parameter, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     * @return the random value
     */
    public static double rLaplace(double mean, double scale, int streamNum) {
        return rLaplace(mean, scale, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Generates according to a Laplace(mean, scale)
     *
     * @param mean  mean or location parameter
     * @param scale scale parameter, must be greater than 0
     * @param rng   the RNStreamIfc, must not be null
     * @return the random value
     */
    public static double rLaplace(double mean, double scale, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (scale <= 0.0) {
            throw new IllegalArgumentException("The scale parameter must be > 0.0");
        }
        double p = rng.randU01();
        double u = p - 0.5;
        return mean - scale * Math.signum(u) * Math.log(1.0 - 2.0 * Math.abs(u));
    }

    /**
     * Randomly select an element from the array
     *
     * @param array the array to select from
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array) {
        return randomlySelect(array, getDefaultRNStream());
    }

    /**
     * Randomly select an element from the array
     *
     * @param array     the array to select from
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array, int streamNum) {
        return randomlySelect(array, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly select an element from the array
     *
     * @param array the array to select from
     * @param rng   the source of randomness
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (array == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (array.length == 1) {
            return array[0];
        }
        int randInt = rng.randInt(0, array.length - 1);
        return array[randInt];
    }

    /**
     * Randomly select an element from the array
     *
     * @param array the array to select from
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array) {
        return randomlySelect(array, getDefaultRNStream());
    }

    /**
     * Randomly select an element from the array
     *
     * @param array     the array to select from
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array, int streamNum) {
        return randomlySelect(array, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly select an element from the array
     *
     * @param array the array to select from
     * @param rng   the source of randomness
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (array == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (array.length == 1) {
            return array[0];
        }
        int randInt = rng.randInt(0, array.length - 1);
        return array[randInt];
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array array to select from
     * @param cdf   the cumulative probability associated with each element of
     *              array
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array, double[] cdf) {
        return randomlySelect(array, cdf, getDefaultRNStream());
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array     array to select from
     * @param cdf       the cumulative probability associated with each element of
     *                  array
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array, double[] cdf, int streamNum) {
        return randomlySelect(array, cdf, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array array to select from
     * @param cdf   the cumulative probability associated with each element of
     *              array
     * @param rng   the source of randomness
     * @return the randomly selected value
     */
    public static double randomlySelect(double[] array, double[] cdf, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (array == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (cdf == null) {
            throw new IllegalArgumentException("The supplied cdf was null");
        }
        if (!isValidCDF(cdf)) {
            throw new IllegalArgumentException("The supplied cdf was not valid");
        }
        if (array.length != cdf.length) {
            throw new IllegalArgumentException("The arrays did not have the same length.");
        }
        if (cdf.length == 1) {
            return array[0];
        }

        int i = 0;
        double value = array[i];
        double u = rng.randU01();
        while (cdf[i] <= u) {
            i = i + 1;
            value = array[i];
        }

        return value;

    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array array to select from
     * @param cdf   the cumulative probability associated with each element of
     *              array
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array, double[] cdf) {
        return randomlySelect(array, cdf, getDefaultRNStream());
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array     array to select from
     * @param cdf       the cumulative probability associated with each element of
     *                  array
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array, double[] cdf, int streamNum) {
        return randomlySelect(array, cdf, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly selects from the array using the supplied cdf
     *
     * @param array array to select from
     * @param cdf   the cumulative probability associated with each element of
     *              array
     * @param rng   the source of randomness
     * @return the randomly selected value
     */
    public static int randomlySelect(int[] array, double[] cdf, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (array == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (cdf == null) {
            throw new IllegalArgumentException("The supplied cdf was null");
        }
        if (!isValidCDF(cdf)) {
            throw new IllegalArgumentException("The supplied cdf was not valid");
        }
        if (array.length != cdf.length) {
            throw new IllegalArgumentException("The arrays did not have the same length.");
        }
        if (cdf.length == 1) {
            return array[0];
        }

        int i = 0;
        int value = array[i];
        double u = rng.randU01();
        while (cdf[i] <= u) {
            i = i + 1;
            value = array[i];
        }

        return value;

    }

    /**
     * Randomly selects from the list using the supplied cdf
     *
     * @param <T>  the type returned
     * @param list list to select from
     * @param cdf  the cumulative probability associated with each element of
     *             array
     * @return the randomly selected value
     */
    public static <T> T randomlySelect(List<T> list, double[] cdf) {
        return randomlySelect(list, cdf, getDefaultRNStream());
    }

    /**
     * Randomly selects from the list using the supplied cdf
     *
     * @param <T>       the type returned
     * @param list      list to select from
     * @param cdf       the cumulative probability associated with each element of
     *                  array
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected value
     */
    public static <T> T randomlySelect(List<T> list, double[] cdf, int streamNum) {
        return randomlySelect(list, cdf, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly selects from the list using the supplied cdf
     *
     * @param <T>  the type returned
     * @param list list to select from
     * @param cdf  the cumulative probability associated with each element of
     *             array
     * @param rng  the source of randomness
     * @return the randomly selected value
     */
    public static <T> T randomlySelect(List<T> list, double[] cdf, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (list == null) {
            throw new IllegalArgumentException("The supplied list was null");
        }
        if (cdf == null) {
            throw new IllegalArgumentException("The supplied cdf was null");
        }
        if (!isValidCDF(cdf)) {
            throw new IllegalArgumentException("The supplied cdf was not valid");
        }
        if (list.size() != cdf.length) {
            throw new IllegalArgumentException("The list and cdf did not have the same length.");
        }
        if (cdf.length == 1) {
            return list.get(0);
        }

        int i = 0;
        T value = list.get(i);
        double u = rng.randU01();
        while (cdf[i] <= u) {
            i = i + 1;
            value = list.get(i);
        }

        return value;

    }

    /**
     * @param cdf the probability array. must have valid probability elements
     *            and last element equal to 1. Every element must be greater than or equal
     *            to the previous element. That is, monotonically increasing.
     * @return true if valid cdf
     */
    public static boolean isValidCDF(double[] cdf) {
        if (cdf == null) {
            return false;
        }
        if (cdf[cdf.length - 1] != 1.0) {
            return false;
        }
        for (int i = 0; i < cdf.length; i++) {
            if ((cdf[i] < 0.0) || (cdf[i] > 1.0)) {
                return false;
            }
            if (i < cdf.length - 1) {
                if (cdf[i + 1] < cdf[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Each element must be in (0,1) and sum of elements must be less than or equal to 1.0
     *
     * @param prob the array to check, must not be null, must have at least two elements
     * @return true if the array represents a probability mass function
     */
    public static boolean isValidPMF(double[] prob) {
        Objects.requireNonNull(prob, "The probability array was null");
        if (prob.length < 2) {
            return false;
        }
        double sum = 0.0;
        for (int i = 0; i < prob.length; i++) {
            if ((prob[i] <= 0.0) || (prob[i] >= 1.0)) {
                return false;
            }
            sum = sum + prob[i];
            if (sum > 1.0) {
                return false;
            }
        }
        if (sum <= 1.0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param prob the array representing a PMF
     * @return a valid CDF
     */
    public static double[] makeCDF(double[] prob) {
        Objects.requireNonNull(prob, "The probability array was null");
        if (!isValidPMF(prob)) {
            throw new IllegalArgumentException("The supplied array was not a valid PMF");
        }
        double[] cdf = new double[prob.length];
        double sum = 0.0;
        for (int i = 0; i < prob.length - 1; i++) {
            sum = sum + prob[i];
            cdf[i] = sum;
        }
        cdf[prob.length - 1] = 1.0;
        return cdf;
    }

    /**
     * Randomly select from the list using the default stream
     *
     * @param <T>  The type of element in the list
     * @param list the list
     * @return the randomly selected element
     */
    public static <T> T randomlySelect(List<T> list) {
        return randomlySelect(list, getDefaultRNStream());
    }

    /**
     * Randomly select from the list
     *
     * @param <T>       The type of element in the list
     * @param list      the list
     * @param streamNum the stream number from the stream provider to use
     * @return the randomly selected element
     */
    public static <T> T randomlySelect(List<T> list, int streamNum) {
        return randomlySelect(list, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly select from the list
     *
     * @param <T>  The type of element in the list
     * @param list the list
     * @param rng  the source of randomness
     * @return the randomly selected element
     */
    public static <T> T randomlySelect(List<T> list, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (list == null) {
            throw new IllegalArgumentException("The supplied list was null");
        }
        if (list.isEmpty()) {
            return null;
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        // more than 1, need to randomly pick
        return list.get(rng.randInt(0, list.size() - 1));
    }

    /**
     * Randomly permutes the supplied array using the default random
     * number generator.  The array is changed.
     *
     * @param x the array
     */
    public static void permutation(double[] x) {
        permutation(x, getDefaultRNStream());
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed
     *
     * @param x         the array
     * @param streamNum the stream number from the stream provider to use
     */
    public static void permutation(double[] x, int streamNum) {
        permutation(x, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed
     *
     * @param x   the array
     * @param rng the source of randomness
     */
    public static void permutation(double[] x, RNStreamIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     * using the default random number generator
     *
     * @param x          the array
     * @param sampleSize the size of the generate
     */
    public static void sampleWithoutReplacement(double[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, getDefaultRNStream());
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param streamNum  the stream number from the stream provider to use
     */
    public static void sampleWithoutReplacement(double[] x, int sampleSize, int streamNum) {
        sampleWithoutReplacement(x, sampleSize, myStreamProvider.rnStream(streamNum));
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param rng        the source of randomness
     */
    public static void sampleWithoutReplacement(double[] x, int sampleSize, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }
        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            double temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /**
     * Facilitates sampling without replacement to a new array. Example usage:
     * <p>
     * sampleWithoutReplacement(x, 5); // first sample into first 5 slots
     * double[] sample = copyFirstNValues(x, 5); // now copy from the first 5 slots
     *
     * @param x   the array to copy from
     * @param N   the number of values to copy
     * @param <T> the type of the array
     * @return a new array with the values
     */
    public static <T> T[] copyFirstNValues(T[] x, int N) {
        Objects.requireNonNull(x, "The supplied array was null");
        if (N > x.length) {
            throw new IllegalArgumentException("Cannot copy more than the number of elements available");
        }
        double[] y = new double[N];
        System.arraycopy(x, 0, y, 0, y.length);
        return (x);
    }

    /**
     * Randomly permutes the supplied array using the default random
     * number generator.  The array is changed.
     *
     * @param x the array
     */
    public static void permutation(int[] x) {
        permutation(x, getDefaultRNStream());
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed.
     *
     * @param x         the array
     * @param streamNum the stream number from the stream provider to use
     */
    public static void permutation(int[] x, int streamNum) {
        permutation(x, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed.
     *
     * @param x   the array
     * @param rng the source of randomness
     */
    public static void permutation(int[] x, RNStreamIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     * using the default random number generator
     *
     * @param x          the array
     * @param sampleSize the generate size
     */
    public static void sampleWithoutReplacement(int[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, getDefaultRNStream());
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param streamNum  the stream number from the stream provider to use
     */
    public static void sampleWithoutReplacement(int[] x, int sampleSize, int streamNum) {
        sampleWithoutReplacement(x, sampleSize, myStreamProvider.rnStream(streamNum));
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param rng        the source of randomness
     */
    public static void sampleWithoutReplacement(int[] x, int sampleSize, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            int temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /**
     * Randomly permutes the supplied array using the default random
     * number generator.  The array is changed.
     *
     * @param x the array
     */
    public static void permutation(boolean[] x) {
        permutation(x, getDefaultRNStream());
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed.
     *
     * @param x         the array
     * @param streamNum the stream number from the stream provider to use
     */
    public static void permutation(boolean[] x, int streamNum) {
        permutation(x, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed.
     *
     * @param x   the array
     * @param rng the source of randomness
     */
    public static void permutation(boolean[] x, RNStreamIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     * using the default random number generator
     *
     * @param x          the array
     * @param sampleSize the generate size
     */
    public static void sampleWithoutReplacement(boolean[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, getDefaultRNStream());
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param streamNum  the stream number from the stream provider to use
     */
    public static void sampleWithoutReplacement(boolean[] x, int sampleSize, int streamNum) {
        sampleWithoutReplacement(x, sampleSize, myStreamProvider.rnStream(streamNum));
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the random sample without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param rng        the source of randomness
     */
    public static void sampleWithoutReplacement(boolean[] x, int sampleSize, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }

        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            boolean temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /**
     * Randomly permutes the supplied array using the default random
     * number generator.  The array is changed
     *
     * @param x the array
     */
    public static <T> void permutation(T[] x) {
        permutation(x, getDefaultRNStream());
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed
     *
     * @param x         the array
     * @param streamNum the stream number from the stream provider to use
     */
    public static <T> void permutation(T[] x, int streamNum) {
        permutation(x, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly permutes the supplied array using the supplied random
     * number generator, the array is changed
     *
     * @param x   the array
     * @param rng the source of randomness
     */
    public static <T> void permutation(T[] x, RNStreamIfc rng) {
        sampleWithoutReplacement(x, x.length, rng);
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the randomly sampled values without replacement
     * using the default random number generator
     *
     * @param x          the array
     * @param sampleSize the source of randomness
     */
    public static <T> void sampleWithoutReplacement(T[] x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, getDefaultRNStream());
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the randomly sampled values without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param streamNum  the stream number from the stream provider to use
     */
    public static <T> void sampleWithoutReplacement(T[] x, int sampleSize, int streamNum) {
        sampleWithoutReplacement(x, sampleSize, myStreamProvider.rnStream(streamNum));
    }

    /**
     * The array x is changed, such that the first sampleSize elements contain the generated sample.
     * That is, x[0], x[1], ... , x[sampleSize-1] is the randomly sampled values without replacement
     *
     * @param x          the array
     * @param sampleSize the generate size
     * @param rng        the source of randomness
     */
    public static <T> void sampleWithoutReplacement(T[] x, int sampleSize, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        if (sampleSize > x.length) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }
        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, x.length - 1);
            T temp = x[j];
            x[j] = x[i];
            x[i] = temp;
        }
    }

    /**
     * Randomly permutes the supplied List using the supplied random
     * number generator, the list is changed
     *
     * @param <T> the type of the list
     * @param x   the list
     */
    public static <T> void permutation(List<T> x) {
        permutation(x, getDefaultRNStream());
    }

    /**
     * Randomly permutes the supplied List using the supplied random
     * number generator, the list is changed
     *
     * @param <T>       the type of the list
     * @param x         the list
     * @param streamNum the stream number from the stream provider to use
     */
    public static <T> void permutation(List<T> x, int streamNum) {
        permutation(x, myStreamProvider.rnStream(streamNum));
    }

    /**
     * Randomly permutes the supplied List using the supplied random
     * number generator, the list is changed
     *
     * @param <T> the type of the list
     * @param x   the list
     * @param rng the source of randomness
     */
    public static <T> void permutation(List<T> x, RNStreamIfc rng) {
        sampleWithoutReplacement(x, x.size(), rng);
    }

    /**
     * The List x is changed, such that the first sampleSize elements contain the generate.
     * That is, x.get(0), x.get(1), ... , x.get(sampleSize-1) is the random sample without replacement
     * using the default random number generator
     *
     * @param <T>        the type of the list
     * @param x          the list
     * @param sampleSize the generate size
     */
    public static <T> void sampleWithoutReplacement(List<T> x, int sampleSize) {
        sampleWithoutReplacement(x, sampleSize, getDefaultRNStream());
    }

    /**
     * The List x is changed, such that the first sampleSize elements contain the generate.
     * That is, x.get(0), x.get(1), ... , x.get(sampleSize-1) is the random sample without replacement
     *
     * @param <T>        the type of the list
     * @param x          the list
     * @param sampleSize the generate size
     * @param streamNum  the stream number from the stream provider to use
     */
    public static <T> void sampleWithoutReplacement(List<T> x, int sampleSize, int streamNum) {
        sampleWithoutReplacement(x, sampleSize, myStreamProvider.rnStream(streamNum));
    }

    /**
     * The List x is changed, such that the first sampleSize elements contain the generate.
     * That is, x.get(0), x.get(1), ... , x.get(sampleSize-1) is the random sample without replacement
     *
     * @param <T>        the type of the list
     * @param x          the list
     * @param sampleSize the generate size
     * @param rng        the source of randomness
     */
    public static <T> void sampleWithoutReplacement(List<T> x, int sampleSize, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RNStreamIfc was null");
        if (x == null) {
            throw new IllegalArgumentException("The supplied array was null");
        }
        int n = x.size();
        if (sampleSize > n) {
            throw new IllegalArgumentException("Can't draw without replacement more than the number of elements");
        }
        for (int j = 0; j < sampleSize; j++) {
            int i = rng.randInt(j, n - 1);
            T temp = x.get(j);
            x.set(j, x.get(i));
            x.set(i, temp);
        }
    }
}
