package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Gamma;
import jsl.utilities.distributions.Normal;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.mcintegration.MCMultiVariateIntegration;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.MVIndependentRV;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.rootfinding.BisectionRootFinder;
import jsl.utilities.rootfinding.GridEnumerator;
import jsl.utilities.rootfinding.StochasticApproximationRootFinder;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.primes.Primes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CentralMVTDistribution {

    private final double[][] covariances;
    private final double dof;
    private final double[][] cfL;
    private final int nDim;
    private final MCMultiVariateIntegration integrator;
//    private final QMCMultiVariateIntegration integrator;
    private final double[] a;
    private final double[] b;

    /**
     *
     * @param dof  the degrees of freedom, must be greater than zero
     * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     */
    public CentralMVTDistribution(double dof, double[][] covariances) {
        this(dof, covariances, JSLRandom.nextRNStream());
    }

    /**
     *
     * @param dof  the degrees of freedom, must be greater than zero
     * @param covariances the variance-covariance matrix, must not be null, must be square and positive definite
     * @param stream the stream for the sampler
     */
    public CentralMVTDistribution(double dof, double[][] covariances, RNStreamIfc stream) {
        Objects.requireNonNull(covariances, "The supplied stream for the sampler was null");
        Objects.requireNonNull(covariances, "The covariance array was null");
        if (dof <= 0.0) {
            throw new IllegalArgumentException("The degrees of freedom must be > 0");
        }
        if (!JSLArrayUtil.isSquare(covariances)) {
            throw new IllegalArgumentException("The covariance array was not square");
        }
        if (covariances.length <= 1) {
            throw new IllegalArgumentException("The covariance array dimension must be >= 2");
        }
        nDim = covariances.length;
        // use of Apache Commons
        RealMatrix cv = MatrixUtils.createRealMatrix(covariances);
        CholeskyDecomposition cd = new CholeskyDecomposition(cv);
        RealMatrix lm = cd.getL();
        cfL = lm.getData();
        // end use of Apache Commons
        this.covariances = JSLArrayUtil.copy2DArray(covariances);
        this.dof = dof;
        a = new double[nDim];
        b = new double[nDim];
        for (int i = 0; i < nDim; i++) {
            a[i] = Double.NEGATIVE_INFINITY;
            b[i] = Double.POSITIVE_INFINITY;
        }
        MVIndependentRV sampler = new MVIndependentRV(nDim, new UniformRV(0.0, 1.0, stream));
        GenzFunc genzFunc = new GenzFunc();
        integrator = new MCMultiVariateIntegration(genzFunc, sampler);
//        integrator = new QMCMultiVariateIntegration(nDim, genzFunc);
        integrator.setConfidenceLevel(0.99);
        integrator.setDesiredAbsError(0.00001);
        integrator.setInitialSampleSize(10);
        integrator.setMaxSampleSize(100);
    }

    public void setConfidenceLevel(double level) {
        integrator.setConfidenceLevel(level);
    }

    public int getInitialSampleSize() {
        return integrator.getInitialSampleSize();
    }

    public void setInitialSampleSize(int initialSampleSize) {
        integrator.setInitialSampleSize(initialSampleSize);
    }

    public long getMaxSampleSize() {
        return integrator.getMaxSampleSize();
    }

    public void setMaxSampleSize(int maxSampleSize) {
        integrator.setMaxSampleSize(maxSampleSize);
    }

    public double getDesiredAbsError() {
        return integrator.getDesiredAbsError();
    }

    public void setDesiredAbsError(double desiredAbsError) {
        integrator.setDesiredAbsError(desiredAbsError);
    }

    public boolean isResetStreamOptionOn() {
        return integrator.isResetStreamOptionOn();
    }

    public void setResetStreamOption(boolean resetStreamOptionOn) {
        integrator.setResetStreamOption(resetStreamOptionOn);
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
     *
     * @return the dimension of the MVT distribution
     */
    public int getDimension() {
        return nDim;
    }

    /** Uses the Genz transform function for Monte-carlo evaluation of the integral.
     *  Accuracy depends on the sampling.  Should be to about 2 decimal places with default settings.
     *
     * Refer to equation (3) of this paper <a href="https://informs-sim.org/wsc15papers/032.pdf</a>
     *
     * @param integrands the integrands for the computation, must not be null
     * @return the estimated value
     */
    public double cdf(List<Interval> integrands) {
        Objects.requireNonNull(integrands, "The integrands list was null");
        if (integrands.size() != nDim) {
            throw new IllegalArgumentException("The number of integrands does not match the dimension of the distribution");
        }
        for (Interval i : integrands) {
            if (i == null) {
                throw new IllegalArgumentException("A supplied integrand was null!");
            }
        }
        // set up lower (a) and upper (b) integration limits
        for (int i = 0; i < nDim; i++) {
            a[i] = integrands.get(i).getLowerLimit();
            b[i] = integrands.get(i).getUpperLimit();
        }
        return integrator.runSimulation();
    }

    /**
     *
     * @return the statistical results of the CDF calculation
     */
    final public Statistic getCDFCalculationStatistics() {
        return integrator.getStatistic();
    }

    /**
     *
     * @return  the results of the CDF integration as a string
     */
    final public String getCDFIntegrationResults(){
        return integrator.toString();
    }

    private class GenzFunc implements FunctionMVIfc {

        public double fx(double[] u) {
            return genzFunction(u);
        }

    }

    /**
     *
     * @param u a vector of U(0,1) random variates
     * @return the evaluation of the Genz transformed function at the point u
     */
    private double genzFunction(double[] u) {
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

    private double sumProdLandY(int r, int k, double[] y) {
        double sum = 0.0;
        for (int n = 0; n < k; n++) {
            sum = sum + cfL[r][n] * y[n];
        }
        return sum;
    }

    private double qmvt(double level, Interval startingInterval){
        Objects.requireNonNull(startingInterval, "The starting interval was null");
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        // set integration limits
        double ll = startingInterval.getLowerLimit();
        double ul = startingInterval.getUpperLimit();
        setLowerLimits(Double.NEGATIVE_INFINITY);
        setLowerLimits(-4.0);
        setUpperLimits(4);
        RootFunction rf = new RootFunction(level);
//        BisectionRootFinder finder = new BisectionRootFinder(rf, ll, ul);
        StochasticApproximationRootFinder finder = new StochasticApproximationRootFinder(rf, ll, ul);
        finder.setInitialPoint((ll+ul)/2.0);
        finder.setDesiredPrecision(0.01);
//        finder.setMaxIterations(10000);
//        finder.evaluate();
//        double result = finder.getResult();
        finder.run();
        double result = finder.getRoot();
        System.out.println(finder);
        return result;
    }

    private void setLowerLimits(double value){
        Arrays.fill(a, value);
    }

    private void setUpperLimits(double value){
        Arrays.fill(b, value);
    }

    public class RootFunction implements FunctionIfc{
        double confidLevel = 0.95;

        public RootFunction(double confidLevel) {
            this.confidLevel = confidLevel;
        }

        @Override
        public double fx(double x) {
//            setLowerLimits(Double.NEGATIVE_INFINITY);
            setUpperLimits(x);
            double cdfofx = integrator.runSimulation();
            return cdfofx - confidLevel;
        }
    }

    public RootFunction getRootFunction(double level){
        return new RootFunction(level);
    }

    public static void main(String[] args) {
//        testCDF();
        testQuantile();
//        enumerateQuantiles();
    }

    static public void testCDF(){
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

    static public void testQuantile(){
        double[][] cov = {
                {1.0, 0.5, 0.5},
                {0.5, 1.0, 0.5},
                {0.5, 0.5, 1.0},
        };

        CentralMVTDistribution d = new CentralMVTDistribution(20.0, cov);
//        d.setMaxSampleSize(1000000);
        Interval i1 = new Interval(Double.NEGATIVE_INFINITY, 2.19);
        Interval i2 = new Interval(Double.NEGATIVE_INFINITY, 2.19);
        Interval i3 = new Interval(Double.NEGATIVE_INFINITY, 2.19);
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
        double result = d.qmvt(0.95, new Interval(2.15, 2.2));
        System.out.println();
        System.out.println("Result = " + result);


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

    public class QuantileFunction implements FunctionIfc{
        @Override
        public double fx(double x) {
            setLowerLimits(Double.NEGATIVE_INFINITY);
            setUpperLimits(x);
            return integrator.runSimulation();
        }
    }

    public QuantileFunction getQuantileFunction(){
        return new QuantileFunction();
    }

    public static void enumerateQuantiles(){
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
        for(GridEnumerator.Evaluation e: list){
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
