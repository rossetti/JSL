package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Gamma;
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
 *  Creates a multi-variate Student-T CDF that has a product structure based on
 *  ALGORITHM AS 251.1  APPL.STATIST. (1989), VOL.38, NO.3
 *
 *  FOR A MULTIVARIATE NORMAL VECTOR WITH CORRELATION STRUCTURE
 *  DEFINED BY RHO(I,J) = Lambda(I) * Lambda(J)
 *  @see <a href="https://www.jstor.org/stable/2347754">AS 251</a>
 */
public class MVTWithProductCorrelation extends MVNWithProductCorrelation{

    private final double dof;
    private final UnivariateFunction integrationFunc;
    private final MVNWithProductCorrelation mvn;

    public MVTWithProductCorrelation(double dof, int nDim, double correlation) {
        this(dof, makeLambda(nDim, correlation));
    }

    public MVTWithProductCorrelation(double dof, double[] lambda) {
        super(lambda);
        if (dof <= 0.0) {
            throw new IllegalArgumentException("The degrees of freedom must be > 0");
        }
        this.dof = dof;
        mvn = new MVNWithProductCorrelation(lambda);
        // set the default integrator to avoid creations when doing quantile searches
//        integrator = new SimpsonIntegrator(1E-06, 1E-07, 10, 64);
        integrationFunc = new IntegrationFunc();
    }

    private void transformLimits(double u){
        double s2 = Gamma.invChiSquareDistribution(u, dof);
        // generate r from a chi-distribution
        double s = Math.sqrt(s2);
        double sqrtDof = Math.sqrt(dof);
        double c = s / sqrtDof;
        JSLArrayUtil.multiplyConstant(a, c);
        System.out.println("Before transform");
        System.out.println(JSLArrayUtil.toCSVString(b));
        System.out.println();
        JSLArrayUtil.multiplyConstant(b, c);
        System.out.println("After transform");
        System.out.println(JSLArrayUtil.toCSVString(b));
        throw new IllegalStateException("just stopping");
    }

//    protected double computeCDF(){
//        double result = 0.0;
//        // overkill to handle cases
//        // needed a = 0.0000001 and b = 0.9999999 to avoid evaluation of normal inverse at 0.0 or at 1.0
//        try {
//            result = integrator.integrate(20000, integrationFunc, 0.0000001, 0.9999999);
//        } catch (TooManyEvaluationsException e1) {
//            result = integrator.integrate(30000, integrationFunc, 0.0000001, 0.9999999);
//        } catch (MaxCountExceededException e2) {
//            integrator = new SimpsonIntegrator(1E-06, 1E-08, 10, 64);
//            result = integrator.integrate(30000, integrationFunc, 0.0000001, 0.9999999);
//        }
//        return result;
//    }

    private class IntegrationFunc implements UnivariateFunction {
        public double value(double x) {
            double s2 = Gamma.invChiSquareDistribution(x, dof);
            // generate r from a chi-distribution
            double s = Math.sqrt(s2);
            double sqrtDof = Math.sqrt(dof);
            double c = s / sqrtDof;
            // the mvn should use its old limits transformed by c
            double[] lower = Arrays.copyOf(a, a.length);
            double[] upper = Arrays.copyOf(b, b.length);
            JSLArrayUtil.multiplyConstant(lower, c);
            JSLArrayUtil.multiplyConstant(upper, c);
            // now need to set up mvn
            // mvn.cdf (called with the intervals from the t's a and b)
            // thus this function requires and integration to occur to get functional evaluation
            // return the value from mvn.cdf()
            return mvn.cdf(lower, upper);
        }
    }

    public static void main(String[] args) {

        testMVT();
    }

    public static void testMVT() {
        MVTWithProductCorrelation mvn = new MVTWithProductCorrelation(20.0, 3, 0.5);
//        List<Interval> intervals = mvn.createUpperIntervals(2.15);

        List<Interval> intervals = mvn.createUpperIntervals(2.191936);

        System.out.println("Integral should evaluate to 0.95");
        double result = mvn.cdf(intervals);
        System.out.println("result = " + result);

        MVTWithProductCorrelation mvn2 = new MVTWithProductCorrelation(7.0, 3, 0.5);
        List<Interval> intervals2 = mvn.createUpperIntervals(2.478537);

        System.out.println("Integral should evaluate to 0.95");
        double result2 = mvn.cdf(intervals2);
        System.out.println("result2 = " + result2);
    }
}
