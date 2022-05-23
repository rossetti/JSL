package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Normal;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.mcintegration.MCExperimentSetUpIfc;
import jsl.utilities.random.mcintegration.MCMultiVariateIntegration;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.MVIndependentRV;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a multi-variate normal distribution with means = 0.0
 * and the provided covariances.  The computed CDF values are to about 2 decimal places
 * using Monte-Carlo integration.
 */
public class CentralMVNDistribution extends MVCDF {

    protected final double[][] covariances;
    protected final double[][] cfL;
    protected final int nDim;
    protected MCMultiVariateIntegration integrator;
    protected MVIndependentRV sampler;

    /**
     * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     */
    public CentralMVNDistribution(double[][] covariances) {
        this(covariances, JSLRandom.nextRNStream());
    }

    /**
      * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     * @param stream      the stream for the sampler
     */
    public CentralMVNDistribution(double[][] covariances, RNStreamIfc stream) {
        super(covariances.length);
        Objects.requireNonNull(stream, "The supplied stream for the sampler was null");
        Objects.requireNonNull(covariances, "The covariance array was null");
        if (!MVNormalRV.isValidCovariance(covariances)){
            throw new IllegalArgumentException("The covariance array is not a valid covariance matrix");
        }
        nDim = covariances.length;
        // use of Apache Commons
        RealMatrix cv = MatrixUtils.createRealMatrix(covariances);
        CholeskyDecomposition cd = new CholeskyDecomposition(cv);
        RealMatrix lm = cd.getL();
        cfL = lm.getData();
        // end use of Apache Commons
        this.covariances = JSLArrayUtil.copy2DArray(covariances);
        sampler = new MVIndependentRV(nDim, new UniformRV(0.0, 1.0, stream));
        GenzFunc genzFunc = new GenzFunc();
        integrator = new MCMultiVariateIntegration(genzFunc, sampler);
    }

    /** The user can use this to control the specification of the monte-carlo
     *  integration of the CDF.
     *
     * @return  the controller
     */
    public final MCExperimentSetUpIfc getMCIntegrationController(){
        return integrator;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CentralMVNDistribution");
        sb.append(System.lineSeparator());
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

    /**
     * @return the statistical results of the CDF calculation
     */
    final public Statistic getCDFCalculationStatistics() {
        return integrator.getStatistic();
    }

    /**
     * @return the results of the CDF integration as a string
     */
    final public String getCDFIntegrationResults() {
        return integrator.toString();
    }

    private class GenzFunc implements FunctionMVIfc {

        public double fx(double[] u) {
            return genzFunction(u);
        }

    }

    /**
     * @param u a vector of U(0,1) random variates
     * @return the evaluation of the Genz transformed function at the point u
     */
    protected double genzFunction(double[] u) {
        Objects.requireNonNull(u, "The U(0,1) array was null");
        double[] z = new double[nDim];
        double ap = a[0] / cfL[0][0];
        double bp = b[0] / cfL[0][0];
        //no need to check for infinities in a[] and b[] because stdNormalCDF handles them correctly
        double d = Normal.stdNormalCDF(ap);
        double e = Normal.stdNormalCDF(bp);
        double f = e - d;
        for (int m = 1; m < nDim; m++) {
            z[m - 1] = Normal.stdNormalInvCDF(u[m - 1]);
            double mu = sumProdLandY(m, m - 1, z);
            ap = (a[m] - mu) / cfL[m][m];
            bp = (b[m] - mu) / cfL[m][m];
            d = Normal.stdNormalCDF(ap);
            e = Normal.stdNormalCDF(bp);
            f = f * (e - d);
        }
        return f;
    }

    protected double sumProdLandY(int r, int k, double[] y) {
        double sum = 0.0;
        for (int n = 0; n < k; n++) {
            sum = sum + cfL[r][n] * y[n];
        }
        return sum;
    }

    protected class RootFunction implements FunctionIfc {
        private double confidLevel = 0.95;

        public RootFunction(double confidLevel) {
            this.confidLevel = confidLevel;
        }

        @Override
        public double fx(double x) {
            return cdf(x) - confidLevel;
        }
    }

    private RootFunction getRootFunction(double level) {
        return new RootFunction(level);
    }

    public class QuantileFunction implements FunctionIfc {
        @Override
        public double fx(double x) {
            return cdf(x);
        }
    }

    public QuantileFunction getQuantileFunction() {
        return new QuantileFunction();
    }

    public static void main(String[] args) {
        testCDF1();
    }

    static public void testCDF1() {
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
        CentralMVNDistribution d = new CentralMVNDistribution(cov);
        System.out.println(d);
        System.out.println();
        double v = d.cdf(intervals);
        System.out.println("Answer should be = 0.4741284");
        System.out.println("v = " + v);
        System.out.println();
        System.out.println(d.getCDFCalculationStatistics());
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
//    rs = pmvnorm(lower = a, upper = b, sigma = A)

}
