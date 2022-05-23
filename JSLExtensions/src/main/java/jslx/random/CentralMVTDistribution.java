package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Gamma;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.mcintegration.MCMultiVariateIntegration;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.rootfinding.GridEnumerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a multi-variate t-distribution with means = 0.0
 * and the provided covariances.  The computed CDF values are to about 2 decimal places
 * using Monte-Carlo integration
 */
public class CentralMVTDistribution extends CentralMVNDistribution {

    private final double dof;

    /**
     * @param dof         the degrees of freedom, must be greater than zero
     * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     */
    public CentralMVTDistribution(double dof, double[][] covariances) {
        this(dof, covariances, JSLRandom.nextRNStream());
    }

    /**
     * @param dof         the degrees of freedom, must be greater than zero
     * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     * @param stream      the stream for the sampler
     */
    public CentralMVTDistribution(double dof, double[][] covariances, RNStreamIfc stream) {
        super(covariances, stream);
        if (dof <= 0.0) {
            throw new IllegalArgumentException("The degrees of freedom must be > 0");
        }
        this.dof = dof;
        GenzTFunc genzTFunc = new GenzTFunc();
        integrator = new MCMultiVariateIntegration(genzTFunc, sampler);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CentralMVTDistribution");
        sb.append(System.lineSeparator());
        sb.append("dof = ").append(dof);
        sb.append(System.lineSeparator());
        sb.append("nDim = ").append(nDim);
        sb.append(System.lineSeparator());
        sb.append("covariances = ");
        sb.append(System.lineSeparator());
        for (int i = 0; i < covariances.length; i++) {
            sb.append("[");
            sb.append(JSLArrayUtil.toCSVString(covariances[i]));
            sb.append("]");
            sb.append(System.lineSeparator());
        }
        sb.append("cfL = ");
        sb.append(System.lineSeparator());
        for (int i = 0; i < cfL.length; i++) {
            sb.append("[");
            sb.append(JSLArrayUtil.toCSVString(cfL[i]));
            sb.append("]");
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * Uses the Genz transform function for Monte-carlo evaluation of the integral.
     * Accuracy depends on the sampling.  Should be to about 2 decimal places with default settings.
     * <p>
     * Refer to equation (3) of this paper <a href="https://informs-sim.org/wsc15papers/032.pdf</a>
     *
     * @return the estimated value
     */
    @Override
    protected double computeCDF() {
        return integrator.runSimulation();
    }

    private class GenzTFunc implements FunctionMVIfc {

        public double fx(double[] u) {
            return genzTFunction(u);
        }

    }

    /**
     * @param u a vector of U(0,1) random variates
     * @return the evaluation of the Genz transformed function at the point u
     */
    private double genzTFunction(double[] u) {
        Objects.requireNonNull(u, "The U(0,1) array was null");
        double[] z = new double[nDim];
        double r2 = Gamma.invChiSquareDistribution(u[nDim - 1], dof);
        // generate r from a chi-distribution
        double r = Math.sqrt(r2);
        double sqrtDof = Math.sqrt(dof);
        double c = r / sqrtDof;
        double ap = c * a[0] / cfL[0][0];
        double bp = c * b[0] / cfL[0][0];
        //no need to check for infinities in a[] and b[] because stdNormalCDF handles them correctly
        double d = Normal.stdNormalCDF(ap);
        double e = Normal.stdNormalCDF(bp);
        double f = e - d;
        for (int m = 1; m < nDim; m++) {
            z[m - 1] = Normal.stdNormalInvCDF(u[m - 1]);
            double mu = sumProdLandY(m, m - 1, z);
            ap = (c * a[m] - mu) / cfL[m][m];
            bp = (c * b[m] - mu) / cfL[m][m];
            d = Normal.stdNormalCDF(ap);
            e = Normal.stdNormalCDF(bp);
            f = f * (e - d);
        }
        return f;
    }

    private double qmvt(double level, Interval startingInterval) {
        throw new UnsupportedOperationException("Not ready for prime time");
        //TODO doesn't really work. Not enough precision in integral calculation
//        Objects.requireNonNull(startingInterval, "The starting interval was null");
//        if ((level <= 0.0) || (level >= 1.0)) {
//            throw new IllegalArgumentException("Confidence Level must be (0,1)");
//        }
//        // set integration limits
//        double ll = startingInterval.getLowerLimit();
//        double ul = startingInterval.getUpperLimit();
//        setLimits(-4.0, 4.0);
//        RootFunction rf = new RootFunction(level);
//        BisectionRootFinder finder = new BisectionRootFinder(rf, ll, ul);
//        finder.setInitialPoint((ll + ul) / 2.0);
//        finder.setDesiredPrecision(0.01);
////        finder.setMaximumIterations(200);
//        finder.evaluate();
//        double result = finder.getResult();
//        return result;
    }

    private RootFunction getRootFunction(double level) {
        return new RootFunction(level);
    }

    public static void main(String[] args) {
        testCDF();
        testQuantile();
//        enumerateQuantiles();
    }

    static public void testCDF() {
        double[][] cov = {
                {1.0, 1.0, 1.0, 1.0, 1.0},
                {1.0, 2.0, 2.0, 2.0, 2.0},
                {1.0, 2.0, 3.0, 3.0, 3.0},
                {1.0, 2.0, 3.0, 4.0, 4.0},
                {1.0, 2.0, 3.0, 4.0, 5.0},
        };
        Interval i1 = new Interval(-5.0, 6.0);
        Interval i2 = new Interval(-4.0, 5.0);
        Interval i3 = new Interval(-3.0, 4.0);
        Interval i4 = new Interval(-2.0, 3.0);
        Interval i5 = new Interval(-1.0, 2.0);
        List<Interval> intervals = new ArrayList<>();
        intervals.add(i1);
        intervals.add(i2);
        intervals.add(i3);
        intervals.add(i4);
        intervals.add(i5);
        CentralMVTDistribution d = new CentralMVTDistribution(8.0, cov);
        System.out.println(d);
        System.out.println();
        double v = d.cdf(intervals);
        System.out.println("Answer should be = 0.447862");
        System.out.println("v = " + v);
        System.out.println();
        System.out.println(d.getCDFCalculationStatistics());
    }

    static public void testQuantile() {
        double[][] cov = {
                {1.0, 0.5, 0.5},
                {0.5, 1.0, 0.5},
                {0.5, 0.5, 1.0},
        };

        CentralMVTDistribution d = new CentralMVTDistribution(20.0, cov);
        Interval i1 = new Interval(Double.NEGATIVE_INFINITY, 2.191936);
        Interval i2 = new Interval(Double.NEGATIVE_INFINITY, 2.191936);
        Interval i3 = new Interval(Double.NEGATIVE_INFINITY, 2.191936);
        List<Interval> intervals = new ArrayList<>();
        intervals.add(i1);
        intervals.add(i2);
        intervals.add(i3);
        System.out.println(d);
        System.out.println();
        double v = d.cdf(intervals);
        System.out.println("Integral should evaluate to 0.95");
        System.out.println("v = " + v);
        System.out.println();
        System.out.println(d.getCDFCalculationStatistics());
        System.out.println();

        //TODO does not work
//        double result = d.qmvt(0.95, new Interval(2.15, 2.2));
//        System.out.println();
//        System.out.println("Result = " + result);


//        d = new CentralMVTDistribution(2.0, cov);
////        d.setMaxSampleSize(1000000);
//        i1 = new Interval(Double.NEGATIVE_INFINITY, 4.34);
//        i2 = new Interval(Double.NEGATIVE_INFINITY, 4.34);
//        i3 = new Interval(Double.NEGATIVE_INFINITY, 4.34);
//        List<Interval> intervals2 = new ArrayList<>();
//        intervals2.add(i1);
//        intervals2.add(i2);
//        intervals2.add(i3);
//        System.out.println(d);
//        System.out.println();
//       v = d.cdf(intervals2);
//        System.out.println("Integral should evaluate to 0.95");
//        System.out.println("v = " + v);
//        System.out.println();
//        System.out.println(d.getCDFCalculationStatistics());

    }

    public static void enumerateQuantiles() {
        double[][] cov = {
                {1.0, 0.5, 0.5},
                {0.5, 1.0, 0.5},
                {0.5, 0.5, 1.0},
        };

        CentralMVTDistribution d = new CentralMVTDistribution(20.0, cov);
        GridEnumerator grid = new GridEnumerator(d.getQuantileFunction());
        grid.evaluate(2.1, 0.01, 20);
        System.out.println(grid);

        System.out.println();
        System.out.println("Sorted evaluations");
        List<GridEnumerator.Evaluation> list = grid.getSortedEvaluations();
        for (GridEnumerator.Evaluation e : list) {
            System.out.println(e);
        }

    }

    // R test
//    A = as.matrix(data.frame(c(1.0, 1.0, 1.0, 1.0, 1.0),
//    c(1.0, 2.0, 2.0, 2.0, 2.0),
//    c(1.0, 2.0, 3.0, 3.0, 3.0),
//    c(1.0, 2.0, 3.0, 4.0, 4.0),
//    c(1.0, 2.0, 3.0, 4.0, 5.0)))
//    colnames(A) = NULL
//install.packages("mvtnorm")
//    library("mvtnorm")
//    a = c(-5.0, -4.0, -3.0, -2.0, -1.0)
//    b = c(6.0, 5.0, 4.0, 3.0, 2.0)
//    rs = pmvt(lower = a, upper = b, df=8, sigma = A)

}
