package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.distributions.Normal;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *  Creates a multi-variate normal CDF that has a product structure.
 *  ALGORITHM AS 251.1  APPL.STATIST. (1989), VOL.38, NO.3
 *
 *  FOR A MULTIVARIATE NORMAL VECTOR WITH CORRELATION STRUCTURE
 *  DEFINED BY RHO(I,J) = Lambda(I) * Lambda(J)
 *  @see <a href="https://www.jstor.org/stable/2347754">AS 251</a>
 */
public class MVNWithProductCorrelation extends MVCDF {

    protected final double[] lambda;
    protected final double[] dlam; // 1/sqrt(1-lambda*lambda)
    protected UnivariateIntegrator integrator;
    private final UnivariateFunction integrationFunc;

    public MVNWithProductCorrelation(int nDim, double correlation) {
        this(makeLambda(nDim, correlation));
    }

    public MVNWithProductCorrelation(double[] lambda) {
        super(lambda.length);
        Objects.requireNonNull(lambda, "The product correlation specification array was null");
        if (!isValidProductCorrelation(lambda)) {
            throw new IllegalArgumentException("Some abs(lamdba[i]) was >= 1");
        }
        this.lambda = Arrays.copyOf(lambda, lambda.length);
        dlam = new double[nDim];
        // initialize arrays
        for (int i = 0; i < nDim; i++) {
            dlam[i] = 1.0 / Math.sqrt(1.0 - lambda[i] * lambda[i]);
//            System.out.printf("lambda[%d] = %f \t dlam[%d]= %f %n", i, lambda[i], i, dlam[i]);
        }
        // set the default integrator to avoid creations when doing quantile searches
        integrator = new SimpsonIntegrator(1E-06, 1E-08, 10, 60);
        integrationFunc = new IntegrationFunc();
    }

    protected double computeCDF(){
        double result = 0.0;
        // overkill to handle cases
        // needed a = 0.0000001 and b = 0.9999999 to avoid evaluation of normal inverse at 0.0 or at 1.0
        try {
            result = integrator.integrate(20000, integrationFunc, 0.0000001, 0.9999999);
        } catch (TooManyEvaluationsException e1) {
            result = integrator.integrate(30000, integrationFunc, 0.0000001, 0.9999999);
        } catch (MaxCountExceededException e2) {
            integrator = new SimpsonIntegrator(1E-06, 1E-08, 10, 64);
            result = integrator.integrate(30000, integrationFunc, 0.0000001, 0.9999999);
        }
        return result;
    }

    private double computeIntegrand01(double u) {
        double f = 1.0;
        double z = Normal.stdNormalInvCDF(u);
//        System.out.println("z = " + z);
        for (int i = 0; i < lambda.length; i++) {
            double zu = (b[i] - lambda[i] * z) * dlam[i];
//            System.out.println("zu = " + zu);
            double zl = (a[i] - lambda[i] * z) * dlam[i];
//            System.out.println("zl = " + zl);
            double e = Normal.stdNormalCDF(zu);
            double d = Normal.stdNormalCDF(zl);
            f = f * (e - d);
        }
        return f;
    }

    private class IntegrationFunc implements UnivariateFunction {
        public double value(double x) {
            return computeIntegrand01(x);
        }
    }

    /**
     *
     * @param nDim the dimension of the product correlation array
     * @param correlation the correlation for specifying the product correlation
     * @return the product correlation array
     */
    public static double[] makeLambda(int nDim, double correlation) {
        if ((correlation < 0.0) || (correlation > 1.0)) {
            throw new IllegalArgumentException("The correlation must be (0,1)");
        }
        if (nDim <= 1) {
            throw new IllegalArgumentException("The dimension of the array needs to be >= 2");
        }
        double[] lambda = new double[nDim];
        Arrays.fill(lambda, Math.sqrt(correlation));
        return lambda;
    }

    /**
     *
     * @param lambda a product correlation array
     * @return true if the product correlation array is valid
     */
    public static boolean isValidProductCorrelation(double[] lambda) {
        Objects.requireNonNull(lambda, "The product correlation specification array was null");
        if (lambda.length <= 1) {
            throw new IllegalArgumentException("The dimension of the array needs to be >= 2");
        }
        for (double v : lambda) {
            if (Math.abs(v) >= 1.0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {

        testMVN();
    }

    public static void testMVN() {
        double[] lam = {Math.sqrt(6.0) / 3.0, Math.sqrt(6.0) / 4.0, Math.sqrt(6.0) / 5.0};
        MVNWithProductCorrelation mvn = new MVNWithProductCorrelation(lam);
        Interval i1 = new Interval(0.0, Double.POSITIVE_INFINITY);
        Interval i2 = new Interval(0.0, Double.POSITIVE_INFINITY);
        Interval i3 = new Interval(0.0, Double.POSITIVE_INFINITY);
        List<Interval> intervals = new ArrayList<>();
        intervals.add(i1);
        intervals.add(i2);
        intervals.add(i3);
        double result = mvn.cdf(intervals);
        System.out.println("Answer should be = 0.22366084");
        System.out.println("result = " + result);
    }
}
