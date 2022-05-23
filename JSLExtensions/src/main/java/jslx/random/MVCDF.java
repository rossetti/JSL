package jslx.random;

import jsl.utilities.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract public class MVCDF {

    protected final int nDim;
    protected final double[] a; // stores the lower limits for cdf computation
    protected final double[] b; // stores the upper limits for cdf computation

    public MVCDF(int nDim) {
        if (nDim <= 1) {
            throw new IllegalArgumentException("Some abs(lamdba[i]) was >= 1");
        }
        this.nDim = nDim;
        a = new double[nDim];
        b = new double[nDim];
        for (int i = 0; i < nDim; i++) {
            a[i] = Double.NEGATIVE_INFINITY;
            b[i] = Double.POSITIVE_INFINITY;
        }
    }

    /** Implementors must provide computation for computing the
     *  value of the CDF across whatever domain limits as specified
     *  by the integration limits supplied to the cdf() method
     *
     * @return the computed value of the CDF
     */
    abstract protected double computeCDF();

    /**
     * @return the dimension of the MVT distribution
     */
    public final int getDimension() {
        return nDim;
    }

    /**
     * Evaluation of the integral. Accuracy should be about 7 decimal places
     *
     * @param integrands the integrands for the computation, must not be null
     * @return the computed value
     */
    public final double cdf(List<Interval> integrands) {
        double[][] limits = createIntegrationLimits(integrands);
        return cdf(limits[0], limits[1]);
    }

    /** Computes the CDF over the rectangular region
     *
     * @param lower (common) lower limit
     * @param upper (common) upper limit
     * @return the computed probability
     */
    public final double cdf(double lower, double upper){
        setIntegrationLimits(lower, upper);
        return computeCDF();
    }

    /** The probability from -infinity to the upper limit, with
     *  the upper limit being the same for all dimensions
     *
     * @param upperLimit the (common) upper limit
     * @return the computed probability
     */
    public final double cdf(double upperLimit){
        setIntegrationLimits(Double.NEGATIVE_INFINITY, upperLimit);
        return computeCDF();
    }

    /**
     * Evaluation of the integral. Accuracy should be about 7 decimal places
     *
     * @param lowerLimits the lower limits for the computation, must not be null
     * @param upperLimits the upper limits for the computation, must not be null
     * @return the computed value
     */
    public final double cdf(double[] lowerLimits, double[] upperLimits){
        setIntegrationLimits(lowerLimits, upperLimits);
        return computeCDF();
    }

    /**
     *
     * @param integrands the list of integrands for each dimension, must not be null
     * @return the limits in a 2-D array with row 0 as the lower limits and row 1 as the upper limits
     */
    public final double[][] createIntegrationLimits(List<Interval> integrands){
        Objects.requireNonNull(integrands, "The integrand interval list was null");
        if (integrands.size() != nDim) {
            throw new IllegalArgumentException("The number of integrand intervals does not match the dimension of the distribution");
        }
        for (Interval i : integrands) {
            if (i == null) {
                throw new IllegalArgumentException("A supplied integrand interval was null!");
            }
        }
        double[][] limits = new double[2][nDim];
        for (int i = 0; i < nDim; i++) {
            limits[0][i] = integrands.get(i).getLowerLimit();
            limits[1][i]= integrands.get(i).getUpperLimit();
        }
        return limits;
    }

    protected void setIntegrationLimits(double[] lower, double[] upper){
        Objects.requireNonNull(lower, "The lower limit array was null");
        Objects.requireNonNull(lower, "The upper limit array was null");
        if (lower.length != upper.length){
            throw new IllegalArgumentException("The integration limit arrays do not have the same length");
        }
        if ((lower.length != nDim)){
            throw new IllegalArgumentException("The integration limit arrays are not of size = " + nDim);
        }
        for (int i = 0; i < nDim; i++) {
            if (lower[i] >= upper[i]){
                String s = String.format("The integration limit lower[%d] = %f was bigger than upper[%d] = %f %n", i, lower[i], i, upper[i]);
                throw new IllegalArgumentException(s);
            }
            a[i] = lower[i];
            b[i] = upper[i];
        }
    }

    /** Sets upper and lower limits to the supplied values
     *
     * @param lower the lower limits
     * @param upper the upper limits
     */
    protected final void setIntegrationLimits(double lower, double upper){
        if (lower >= upper){
            String s = String.format("The lower limit = %f was greater than or equal to the upper limit %f %n", lower, upper);
            throw new IllegalArgumentException(s);
        }
        Arrays.fill(a, lower);
        Arrays.fill(b, upper);
    }

    /**
     * The upper limit will be Double.POSITIVE_INFINITY
     *
     * @param lowerLimit the (common) lower integration limit
     * @return a set of intervals for the computation of the CDF
     */
    public final List<Interval> createLowerIntervals(double lowerLimit){
        return createIntervals(lowerLimit, Double.POSITIVE_INFINITY);
    }

    /**
     * The lower limit will be Double.NEGATIVE_INFINITY
     *
     * @param upperLimit the (common) upper integration limit
     * @return a set of intervals for the computation of the CDF
     */
    public final List<Interval> createUpperIntervals(double upperLimit){
        return createIntervals(Double.NEGATIVE_INFINITY, upperLimit);
    }

    /**
     * @param lowerLimit the (common) lower limit, must be less than the upper limit
     * @param upperLimit the (common) upper integration limit
     * @return a set of intervals for the computation of the CDF
     */
    public final List<Interval> createIntervals(double lowerLimit, double upperLimit){
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("The lower limit must be < upper limit");
        }
        List<Interval> list = new ArrayList<>();
        for (int i = 0; i < nDim; i++) {
            list.add(new Interval(lowerLimit, upperLimit));
        }
        return list;
    }

}
