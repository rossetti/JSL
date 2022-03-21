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
package jsl.utilities.distributions;

import jsl.simulation.JSLTooManyIterationsException;
import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.BetaRV;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.rootfinding.BisectionRootFinder;
import jsl.utilities.rootfinding.RootFinder;

/**
 * The standard beta distribution defined over the range from (0,1)
 */
public class Beta extends Distribution implements ContinuousDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private static final IncompleteBetaFunctionFraction myContinuedFraction = new IncompleteBetaFunctionFraction();

    private static final Interval myInterval = new Interval(0.0, 1.0);

    private static final RootFinder myRootFinder = new BisectionRootFinder();

    private static final double delta = 0.01;

    private double myAlpha1;

    private double myAlpha2;

    private double mylnBetaA1A2;

    private double myBetaA1A2;

    /**
     * Creates a Beta with parameters 1.0, 1.0
     */
    public Beta() {
        this(1.0, 1.0, null);
    }

    /**
     * Creates a beta with the supplied parameters
     *
     * @param alpha1 the alpha1 parameter
     * @param alpha2 the alpha2 parameter
     */
    public Beta(double alpha1, double alpha2) {
        this(alpha1, alpha2, null);
    }

    /**
     * Creates a beta with the supplied parameters
     *
     * @param parameters alpha1 is parameter[0], alpha2 is parameter[1]
     */
    public Beta(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /**
     * Creates a beta with the supplied parameters
     *
     * @param alpha1 the alpha1 parameter
     * @param alpha2 the alpha2 parameter
     * @param name   a label
     */
    public Beta(double alpha1, double alpha2, String name) {
        super(name);
        setParameters(alpha1, alpha2);
        myRootFinder.setMaximumIterations(200);
    }

    @Override
    public final Beta newInstance() {
        return (new Beta(getParameters()));
    }

    @Override
    public final Interval getDomain() {
        return new Interval(0.0, 1.0);
    }

    /**
     * @return the first shape parameter
     */
    public final double getAlpha1() {
        return myAlpha1;
    }

    /**
     * @return the second shape parameter
     */
    public final double getAlpha2() {
        return myAlpha2;
    }

    /**
     * Changes the parameters to the supplied values
     *
     * @param alpha1 the alpha1 parameter
     * @param alpha2 the alpha2 parameter
     */
    public final void setParameters(double alpha1, double alpha2) {
        if (alpha1 <= 0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0");
        }
        myAlpha1 = alpha1;

        if (alpha2 <= 0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0");
        }
        myAlpha2 = alpha2;

        myBetaA1A2 = betaFunction(myAlpha1, myAlpha2);
//		System.out.println("Beta("+myAlpha1+","+myAlpha2+")=" + myBetaA1A2);

        mylnBetaA1A2 = logBetaFunction(myAlpha1, myAlpha2);
//		System.out.println("lnBeta("+myAlpha1+","+myAlpha2+")=" + mylnBetaA1A2);

    }

    /**
     * Sets the parameters parameter[0] is alpha 1 parameter[1] is alpha 2
     *
     * @param parameters an array holding the parameters
     */
    @Override
    public final void setParameters(double[] parameters) {
        setParameters(parameters[0], parameters[1]);
    }

    @Override
    public final double getMean() {
        return (myAlpha1 / (myAlpha1 + myAlpha2));
    }

    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myAlpha1;
        param[1] = myAlpha2;
        return (param);
    }

    @Override
    public final double getVariance() {
        double n = myAlpha1 * myAlpha2;
        double d = (myAlpha1 + myAlpha2) * (myAlpha1 + myAlpha2) * (myAlpha1 + myAlpha2 + 1.0);
        return n / d;
    }

    /**
     * Computes the CDF, has accuracy to about 10e-9
     *
     * @param x the x value to be evaluated
     */
    @Override
    public double cdf(double x) {
        return stdBetaCDF(x, myAlpha1, myAlpha2, mylnBetaA1A2);
    }

    @Override
    public double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }
        return stdBetaInvCDF(p, myAlpha1, myAlpha2, mylnBetaA1A2);
//       return (inverseBetaCDF(p));
    }

    @Override
    public final double pdf(double x) {
        if ((0 < x) && (x < 1)) {
            double f1 = Math.pow(x, myAlpha1 - 1.0);
            double f2 = Math.pow(1.0 - x, myAlpha2 - 1.0);
            return ((f1 * f2 / myBetaA1A2));
        } else {
            return 0.0;
        }
    }

    /**
     * Computes Beta(z1,z2)
     *
     * @param z1 the first parameter
     * @param z2 the second parameter
     * @return the computed value
     */
    public static double betaFunction(double z1, double z2) {
        if (z1 <= 0) {
            throw new IllegalArgumentException("The 1st parameter must be > 0");
        }

        if (z2 <= 0) {
            throw new IllegalArgumentException("The 2nd parameter must be > 0");
        }

        double n1 = Gamma.gammaFunction(z1);
        double n2 = Gamma.gammaFunction(z2);
        double d = Gamma.gammaFunction(z1 + z2);
        return ((n1 * n2) / d);
    }

    /**
     * The natural logarithm of Beta(z1,z2)
     *
     * @param z1 the first parameter
     * @param z2 the second parameter
     * @return natural logarithm of Beta(z1,z2)
     */
    public static double logBetaFunction(double z1, double z2) {
        if (z1 <= 0) {
            throw new IllegalArgumentException("The 1st parameter must be > 0");
        }

        if (z2 <= 0) {
            throw new IllegalArgumentException("The 2nd parameter must be > 0");
        }

        double n1 = Gamma.logGammaFunction(z1);
        double n2 = Gamma.logGammaFunction(z2);
        double d = Gamma.logGammaFunction(z1 + z2);
        return (n1 + n2 - d);
    }

    /**
     * Computes the incomplete beta function at the supplied x Beta(x, a,
     * b)/Beta(a, b)
     *
     * @param x the point to be evaluated
     * @param a alpha 1
     * @param b alpha 2
     * @return the regularized beta function at the supplied x
     */
    public static double incompleteBetaFunction(double x, double a, double b) {
        double beta = Beta.betaFunction(a, b);
        double rBeta = Beta.regularizedIncompleteBetaFunction(x, a, b);
        return (rBeta * beta);
    }

    /**
     * Computes the regularized incomplete beta function at the supplied x
     *
     * @param x the point to be evaluated
     * @param a alpha 1
     * @param b alpha 2
     * @return the regularized incomplete beta function at the supplied x
     */
    public static double regularizedIncompleteBetaFunction(double x, double a, double b) {
        double lnbeta = logBetaFunction(a, b);
        return (regularizedIncompleteBetaFunction(x, a, b, lnbeta));
    }

    /**
     * Computes the regularized incomplete beta function at the supplied x
     *
     * @param x      the point to be evaluated
     * @param a      alpha 1
     * @param b      alpha 2
     * @param lnbeta the natural log of Beta(alpha1,alpha2)
     * @return the regularized incomplete beta function at the supplied x
     */
    protected static double regularizedIncompleteBetaFunction(double x, double a, double b, double lnbeta) {

        if ((x < 0.0) || (x > 1.0)) {
            throw new IllegalArgumentException("Argument x, must be in [0,1]");
        }

        if (a <= 0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0");
        }

        if (b <= 0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0");
        }

        if (x == 0.0) {
            return 0.0;
        }

        if (x == 1.0) {
            return 1.0;
        }

        double bt = Math.exp(-lnbeta + a * Math.log(x) + b * Math.log(1.0 - x));

        /* use the continued fraction object instead
        if ( x < (a + 1.0)/(a+b+2.0))
        return (bt*betaContinuedFraction(x, a, b)/a);
        else
        return (1.0 - bt*betaContinuedFraction(1.0 - x, b, a)/b);
         */
        if (x < (a + 1.0) / (a + b + 2.0)) {
            return (bt / (myContinuedFraction.evaluateFraction(x, a, b) * a));
        } else {
            return (1.0 - bt / (myContinuedFraction.evaluateFraction(1.0 - x, b, a) * b));
        }

    }

    /**
     * Computes the continued fraction for the incomplete beta function.
     *
     * @param x the point to be evaluated
     * @param a alpha 1
     * @param b alpha 2
     * @return the continued fraction
     */
    protected static double betaContinuedFraction(double x, double a, double b) {
        double em;
        double tem;
        double d;
        double bm = 1.0;
        double bp;
        double bpp;
        double az = 1.0;
        double am = 1.0;
        double ap;
        double app;
        double aold;

        double qab = a + b;
        double qap = a + 1.0;
        double qam = a - 1.0;
        double bz = 1.0 - qab * x / qap;
        int maxi = JSLMath.getMaxNumIterations();
        double eps = JSLMath.getDefaultNumericalPrecision();

        for (int i = 1; i <= maxi; i++) {
            em = (double) i;
            tem = em + em;
            d = em * (b - em) * x / ((qam + tem) * (a + tem));
            ap = az + d * am;
            bp = bz + d * bm;
            d = -(a + em) * (qab + em) * x / ((qap + tem) * (a + tem));
            app = ap + d * az;
            bpp = bp + d * bz;
            aold = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1.0;
            if (Math.abs(az - aold) < (eps * Math.abs(az))) {
                return az;
            }
        }
        throw new JSLTooManyIterationsException("Too many iterations in computing betaContinuedFraction, increase max iterations via setMaxNumIterations()");

    }

    /**
     * Computes the CDF of the standard beta distribution, has accuracy to about 10e-9
     *
     * @param p      the probability that needs to be evaluated
     * @param alpha1 the first shape parameter, must be greater than 0
     * @param alpha2 the second shape parameter, must be greater than 0
     */
    public static double stdBetaInvCDF(double p, double alpha1, double alpha2) {
        double lnBetaA1A2 = logBetaFunction(alpha1, alpha2);
        return stdBetaInvCDF(p, alpha1, alpha2, lnBetaA1A2);
    }

    /**
     * Computes the CDF of the standard beta distribution, has accuracy to about 10e-9
     *
     * @param p          the probability that needs to be evaluated
     * @param alpha1     the first shape parameter, must be greater than 0
     * @param alpha2     the second shape parameter, must be greater than 0
     * @param lnBetaA1A2 logBetaFunction(alpha1, alpha2)
     */
    public static double stdBetaInvCDF(double p, double alpha1, double alpha2, double lnBetaA1A2) {
        double initialX = approximateInvCDF(alpha1, alpha2, p, lnBetaA1A2);
        return stdBetaInvCDF(p, alpha1, alpha2, lnBetaA1A2, initialX, delta);
    }

    /**
     * Computes the CDF of the standard beta distribution, has accuracy to about 10e-9
     *
     * @param p           the probability that needs to be evaluated
     * @param alpha1      the first shape parameter, must be greater than 0
     * @param alpha2      the second shape parameter, must be greater than 0
     * @param lnBetaA1A2  the logBetaFunction(alpha1, alpha2)
     * @param initialX    an initial approximation for the returned value x
     * @param searchDelta the suggested delta around the initial approximation
     */
    public static double stdBetaInvCDF(double p, double alpha1, double alpha2, double lnBetaA1A2, double initialX, double searchDelta) {
        if ((initialX < 0.0) || (initialX > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be (0,1)");
        }
        if (searchDelta <= 0) {
            throw new IllegalArgumentException("The search delta must be > 0");
        }
        if (alpha1 <= 0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0");
        }
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0");
        }
        if (JSLMath.equal(p, 1.0)) {
            return (1.0);
        }
        if (JSLMath.equal(p, 0.0)) {
            return (0.0);
        }
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be (0,1)");
        }
        // set up the search for the root
        double xL = Math.max(0.0, initialX - searchDelta);
        double xU = Math.min(1.0, initialX + searchDelta);
        Interval interval = new Interval(xL, xU);
        class RootFunction implements FunctionIfc {
            public double fx(double x) {
                return stdBetaCDF(x, alpha1, alpha2, lnBetaA1A2) - p;
            }
        }
        RootFunction rootFunction = new RootFunction();
        boolean found = RootFinder.findInterval(rootFunction, interval);
        if (!found) {
            interval.setInterval(0.0, 1.0);
        } else {
            xL = Math.max(0.0, interval.getLowerLimit());
            xU = Math.min(1.0, interval.getUpperLimit());
            interval.setInterval(xL, xU);
        }
        myRootFinder.setInterval(rootFunction, interval);
        myRootFinder.evaluate();
        if (!myRootFinder.hasConverged()) {
            throw new JSLTooManyIterationsException("Unable to invert CDF for Beta: Beta(x," + alpha1 + "," + alpha2 + ")=" + p);
        }
        return (myRootFinder.getResult());
    }

    /**
     * Computes the CDF of the standard beta distribution, has accuracy to about 10e-9
     *
     * @param x      the x value to be evaluated
     * @param alpha1 the first shape parameter, must be greater than 0
     * @param alpha2 the second shape parameter, must be greater than 0
     */
    public static double stdBetaCDF(double x, double alpha1, double alpha2) {
        return stdBetaCDF(x, alpha1, alpha2, logBetaFunction(alpha1, alpha2));
    }

    /**
     * Computes the CDF of the standard beta distribution, has accuracy to about 10e-9
     *
     * @param x          the x value to be evaluated
     * @param alpha1     the first shape parameter, must be greater than 0
     * @param alpha2     the second shape parameter, must be greater than 0
     * @param lnBetaA1A2 the logBetaFunction(alpha1, alpha2)
     */
    public static double stdBetaCDF(double x, double alpha1, double alpha2, double lnBetaA1A2) {
        if (alpha1 <= 0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0");
        }
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0");
        }
        if (x <= 0.0) {
            return (0.0);
        }
        if (x >= 1.0) {
            return (1.0);
        }
        return regularizedIncompleteBetaFunction(x, alpha1, alpha2, lnBetaA1A2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Mean ");
        sb.append(getMean());
        sb.append(System.lineSeparator());
        sb.append("Variance ");
        sb.append(getVariance());
        sb.append(System.lineSeparator());
        sb.append("Alpha1 ");
        sb.append(this.myAlpha1);
        sb.append(System.lineSeparator());
        sb.append("Alpha2 ");
        sb.append(this.myAlpha2);
        return (sb.toString());
    }

    /**
     * Computes an approximation of the invCDF for the Beta distribution Uses part
     * of algorithm AS109, Applied Statistics, vol 26, no 1, 1977, pp 111-114
     *
     * @param pp     Alpha 1 parameter
     * @param qq     Alpha 2 parameter
     * @param a      The point to be evaluated
     * @param lnbeta The log of Beta(alpha1,alpha2)
     * @return the approx cdf value
     */
    private static double approximateInvCDF(double pp, double qq, double a, double lnbeta) {
        double r, y, t, s, h, w, x;

        r = Math.sqrt(-Math.log(a * a));
        y = r - (2.30753 + 0.27061 * r) / (1.0 + (0.99229 + 0.04481 * r) * r);

        if ((pp > 1.0) && (qq > 1.0)) {
            r = (y * y - 3.0) / 6.0;
            s = 1.0 / (pp + pp - 1.0);
            t = 1.0 / (qq + qq - 1.0);
            h = 2.0 / (s + t);
            w = y * Math.sqrt(h + r) / h - (t - s) * (r + 5.0 / 6.0 - 2.0 / (3.0 * h));
            x = pp / (pp + qq * Math.exp(w + w));
        } else {
            r = qq + qq;
            t = 1.0 / (9.0 * qq);
            t = r * Math.pow(1.0 - t + y * Math.sqrt(t), 3);
            if (t <= 0.0) {
                x = 1.0 - Math.exp(Math.log((1.0 - a) * qq) + lnbeta) / qq;
            } else {
                t = (4.0 * pp + r - 2.0) / t;
                if (t <= 1.0) {
                    x = Math.exp((Math.log(a * pp) + lnbeta) / pp);
                } else {
                    x = 1.0 - 2.0 / (t + 1.0);
                }
            }
        }

        return (x);
    }

    /**
     * Sets the desired precision in the continued fraction expansion for the
     * computation of the incompleteBetaFunction
     *
     * @param prec the desired precision
     */
    public static void setContinuedFractionDesiredPrecision(double prec) {
        myContinuedFraction.setDesiredPrecision(prec);
    }

    /**
     * Sets the maximum number of iterations in the continued fraction expansion
     * for the computation of the incompleteBetaFunction
     *
     * @param maxIter the maximum number of iterations
     */
    public static void setContinuedFractionMaximumIterations(int maxIter) {
        myContinuedFraction.setMaximumIterations(maxIter);
    }

    /**
     * Sets the desired precision of the root finding algorithm in the CDF
     * inversion computation
     *
     * @param prec the desired precision
     */
    public static void setRootFindingDesiredPrecision(double prec) {
        myRootFinder.setDesiredPrecision(prec);
    }

    /**
     * Sets the maximum number of iterations of the root finding algorithm in
     * the CDF inversion computation
     *
     * @param maxIter the max iterations
     */
    public static void setRootFindingMaximumIterations(int maxIter) {
        myRootFinder.setMaximumIterations(maxIter);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new BetaRV(getAlpha1(), getAlpha2(), rng);
    }

    public static void main(String args[]) {
        double a1 = .6;
        double a2 = 3.3;

        Beta n2 = new Beta(a1, a2);

        System.out.println("mean = " + n2.getMean());
        System.out.println("var = " + n2.getVariance());
        double x = 0.5;
        System.out.println("pdf at " + x + " = " + n2.pdf(x));
        System.out.println("cdf at " + x + " = " + n2.cdf(x));
        System.out.println("------------");
        for (int i = 0; i <= 10; i++) {
            x = 0.1 * i;
            System.out.println("cdf at " + x + " = " + n2.cdf(x));
        }
        System.out.println("------------");
        for (int i = 0; i <= 10; i++) {
            double p = 0.1 * i;
            System.out.println("invcdf at " + p + " = " + n2.invCDF(p));
        }

        System.out.println("------------");
        System.out.println("------------");

        RVariableIfc rv = n2.getRandomVariable();
        for (int i = 1; i <= 10; i++) {
            System.out.println("nextRandom(" + i + ")= " + rv.getValue());
        }
        System.out.println("------------");

        System.out.println("------------");
        for (int i = 0; i <= 10; i++) {
            double p = 0.1 * i;
            System.out.println("invcdf at " + p + " = " + stdBetaInvCDF(p, 0.6, 3.3));
        }
        System.out.println("done");
    }
}
