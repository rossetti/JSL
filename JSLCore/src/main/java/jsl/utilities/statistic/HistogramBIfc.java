package jsl.utilities.statistic;

import jsl.utilities.IdentityIfc;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.math.JSLMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface HistogramBIfc extends CollectorIfc, IdentityIfc, ArraySaverIfc, StatisticAccessorIfc, GetCSVStatisticIfc, Comparable<AbstractStatistic> {

    /**
     * @param x the observation to bin
     * @return the bin that the observation falls within
     */
    HistogramBin findBin(double x);

    /**
     * Bins are numbered starting at 1 through the number of bins
     *
     * @param x double
     * @return int    the number of the bin where x is located
     */
    int getBinNumber(double x);

    /**
     * The number of observations that fell below the first bin's lower limit
     *
     * @return number of observations that fell below the first bin's lower limit
     */
    double getUnderFlowCount();

    /**
     * The number of observations that fell past the last bin's upper limit
     *
     * @return number of observations that fell past the last bin's upper limit
     */
    double getOverFlowCount();

    /**
     * @return the number of bins that were defined
     */
    int getNumberBins();

    /**
     * The bin that x falls in. The bin is a copy. It will not
     * reflect observations collected after this call.
     *
     * @param x the data to check
     * @return bin that x falls in
     */
    HistogramBin getBin(double x);

    /**
     * Returns an instance of a Bin for the supplied bin number
     * The bin does not reflect changes to the histogram after
     * this call. May throw IndexOutOfBoundsException
     *
     * @param binNum the bin number to get
     * @return the bin, or null
     */
    HistogramBin getBin(int binNum);

    /**
     * Returns a List of Bins based on the current state of the
     * histogram
     *
     * @return the list of bins
     */
    List<HistogramBin> getBins();

    /**
     * Returns an array of Bins based on the current state of the
     * histogram
     *
     * @return the array of bins
     */
    HistogramBin[] getBinArray();

    /**
     * @return the break points for the bins
     */
    double[] getBreakPoints();

    /**
     * @return the bin counts as an array
     */
    double[] getBinCounts();

    /**
     * Returns the current bin count for the bin associated with x
     *
     * @param x the data to check
     * @return the bin count
     */
    double getBinCount(double x);

    /**
     * Returns the bin count for the indicated bin
     *
     * @param binNum the bin number
     * @return the bin count for the indicated bin
     */
    double getBinCount(int binNum);

    /**
     * Returns the fraction of the data relative to those
     * tabulated in the bins for the supplied bin number
     *
     * @param binNum the bin number
     * @return the fraction of the data
     */
    double getBinFraction(int binNum);

    /**
     * Returns the fraction of the data relative to those
     * tabulated in the bins for the bin number associated with the x
     *
     * @param x the data point
     * @return the fraction
     */
    double getBinFraction(double x);

    /**
     * Returns the cumulative count of all bins up to and
     * including the bin containing the value x
     *
     * @param x the data point
     * @return the cumulative bin count
     */
    double getCumulativeBinCount(double x);

    /**
     * Returns the cumulative count of all the bins up to
     * and including the indicated bin number
     *
     * @param binNum the bin number
     * @return cumulative count
     */
    double getCumulativeBinCount(int binNum);

    /**
     * Returns the cumulative fraction of the data up to and
     * including the indicated bin number
     *
     * @param binNum the bin number
     * @return the cumulative fraction
     */
    double getCumulativeBinFraction(int binNum);

    /**
     * Returns the cumulative fraction of the data up to and
     * including the bin containing the value of x
     *
     * @param x the datum
     * @return the cumulative fraction
     */
    double getCumulativeBinFraction(double x);

    /**
     * Returns the cumulative count of all the data (including underflow and overflow)
     * up to and including the indicated bin
     *
     * @param binNum the bin number
     * @return the cumulative count
     */
    double getCumulativeCount(int binNum);

    /**
     * Returns the cumulative count of all the data (including underflow
     * and overflow) for all bins up to and including the bin containing x
     *
     * @param x the datum
     * @return the cumulative count
     */
    double getCumulativeCount(double x);

    /**
     * Returns the cumulative fraction of all the data up to and including
     * the supplied bin (includes over and under flow)
     *
     * @param binNum the bin number
     * @return the cumulative fraction
     */
    double getCumulativeFraction(int binNum);

    /**
     * Returns the cumulative fraction of all the data up to an including
     * the bin containing the value x, (includes over and under flow)
     *
     * @param x the datum
     * @return the cumulative fraction
     */
    double getCumulativeFraction(double x);

    /**
     * Total number of observations collected including overflow and underflow
     *
     * @return Total number of observations
     */
    double getTotalCount();

    /**
     * The first bin's lower limit
     *
     * @return first bin's lower limit
     */
    double getFirstBinLowerLimit();

    /**
     * The last bin's upper limit
     *
     * @return last bin's upper limit
     */
    double getLastBinUpperLimit();

    /**
     * Create a histogram with lower limit set to zero
     *
     * @param upperLimit the upper limit of the last bin, cannot be positive infinity
     * @param numBins    the number of bins to create, must be greater than 0
     * @return the histogram
     */
    public static HistogramBIfc create(double upperLimit, int numBins) {
        return (create(0.0, upperLimit, numBins, null));
    }

    /**
     * Create a histogram
     *
     * @param lowerLimit lower limit of first bin, cannot be negative infinity
     * @param upperLimit the upper limit of the last bin, cannot be positive infinity
     * @param numBins    the number of bins to create, must be greater than 0
     * @return the histogram
     */
    static HistogramBIfc create(double lowerLimit, double upperLimit, int numBins) {
        return (create(lowerLimit, upperLimit, numBins, null));
    }

    /**
     * Create a histogram with the given name based on the provided values
     *
     * @param lowerLimit lower limit of first bin, cannot be negative infinity
     * @param upperLimit the upper limit of the last bin, cannot be positive infinity
     * @param numBins    the number of bins to create, must be greater than zero
     * @param name       the name of the histogram
     * @return the histogram
     */
    static HistogramBIfc create(double lowerLimit, double upperLimit, int numBins, String name) {
        return (new HistogramB(createBreakPoints(lowerLimit, upperLimit, numBins)));
    }

    /**
     * @param numBins    the number of bins to make, must be greater than zero
     * @param lowerLimit the lower limit of the first bin, cannot be negative infinity
     * @param width      the width of each bin, must be greater than zero
     * @return the created histogram
     */
    static HistogramBIfc create(double lowerLimit, int numBins,  double width) {
        return new HistogramB(createBreakPoints(lowerLimit, numBins, width));
    }

    /**
     * Divides the range equally across the number of bins.
     *
     * @param lowerLimit lower limit of first bin, cannot be negative infinity
     * @param upperLimit the upper limit of the last bin, cannot be positive infinity
     * @param numBins    the number of bins to create, must be greater than zero
     * @return the break points
     */
    static double[] createBreakPoints(double lowerLimit, double upperLimit, int numBins) {
        if (Double.isInfinite(lowerLimit)) {
            throw new IllegalArgumentException("The lower limit of the range cannot be infinite.");
        }
        if (Double.isInfinite(upperLimit)) {
            throw new IllegalArgumentException("The upper limit of the range cannot be infinite.");
        }
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("The lower limit must be < the upper limit of the range");
        }
        if (numBins <= 0) {
            throw new IllegalArgumentException("The number of bins must be > 0");
        }
        double binWidth = JSLMath.roundToScale((upperLimit - lowerLimit) / numBins, false);
        return createBreakPoints(lowerLimit, numBins, binWidth);
    }

    /**
     * @param numBins    the number of bins to make, must be greater than 0
     * @param lowerLimit the lower limit of the first bin, cannot be negative infinity
     * @param width      the width of each bin, must be greater than 0
     * @return the constructed break points
     */
    static double[] createBreakPoints(double lowerLimit, int numBins, double width) {
        if (Double.isInfinite(lowerLimit)) {
            throw new IllegalArgumentException("The lower limit of the range cannot be infinite.");
        }
        if (numBins <= 0) {
            throw new IllegalArgumentException("The number of bins must be > 0");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("The width of the bins must be > 0");
        }
        double[] points = new double[numBins + 1];
        points[0] = lowerLimit;
        for (int i = 1; i < points.length; i++) {
            points[i] = points[i - 1] + width;
        }
        return points;
    }

    /**
     * @param breakPoints the break points w/o negative infinity
     * @return the break points with Double.NEGATIVE_INFINITY as the first break point
     */
    static double[] addNegativeInfinity(double[] breakPoints) {
        Objects.requireNonNull(breakPoints, "The supplied break points array was null");
        if (breakPoints.length == 0) {
            throw new IllegalArgumentException("The break points array was empty");
        }
        double[] b = new double[breakPoints.length + 1];
        System.arraycopy(breakPoints, 0, b, 1, breakPoints.length);
        b[0] = Double.NEGATIVE_INFINITY;
        return b;
    }

    /**
     * @param breakPoints the break points w/o positive infinity
     * @return the break points with Double.POSITIVE_INFINITY as the last break point
     */
    static double[] addPositiveInfinity(double[] breakPoints) {
        Objects.requireNonNull(breakPoints, "The supplied break points array was null");
        if (breakPoints.length == 0) {
            throw new IllegalArgumentException("The break points array was empty");
        }
        double[] b = Arrays.copyOf(breakPoints, breakPoints.length + 1);
        b[b.length - 1] = Double.POSITIVE_INFINITY;
        return b;
    }

    /**
     * http://www.fmrib.ox.ac.uk/analysis/techrep/tr00mj2/tr00mj2/node24.html
     *
     * @param observations observations for a histogram
     * @return a set of break points based on some theory
     */
    static double[] recommendBreakPoints(double[] observations) {
        Objects.requireNonNull(observations, "The supplied observations array was null");
        if (observations.length == 0) {
            throw new IllegalArgumentException("The supplied observations array was empty");
        }
        if (observations.length == 1) {
            // use the sole observation
            double[] b = new double[1];
            b[0] = Math.floor(observations[0]);
            return b;
        }
        // 2 or more observations
        Statistic statistic = new Statistic(observations);
        double LL = statistic.getMin();
        double UL = statistic.getMax();
        if (JSLMath.equal(LL, UL)) {
            // essentially the same, go back to 1 observation
            double[] b = new double[1];
            b[0] = Math.floor(LL);
            return b;
        }
        // more than 2 and some spread
        // try to approximate a reasonable number of bins from the observations
        // first determine a reasonable bin width
        double s = statistic.getStandardDeviation();
        double n = statistic.getCount();
        // http://www.fmrib.ox.ac.uk/analysis/techrep/tr00mj2/tr00mj2/node24.html
        //double iqr = 1.35*s;
        // use the more "optimal" estimate
        double width = 3.49 * s * Math.pow(n, -1.0 / 3.0);
        // round the width to a reasonable scale
        double binWidth = JSLMath.roundToScale(width, false);
        // now compute a number of bins for this width
        double nb = (Math.ceil(UL) - Math.floor(LL)) / binWidth;
        int numBins = (int) Math.ceil(nb);
        return HistogramBIfc.createBreakPoints(Math.floor(LL), numBins, binWidth);
    }

    /** Creates a list of ordered bins for use in a histogram
     *
     * @param breakPoints the break points
     * @return the list of histogram bins
     */
    static List<HistogramBin> makeBins(double[] breakPoints) {
        Objects.requireNonNull(breakPoints, "The break point array was null");
        if (!JSLArrayUtil.isStrictlyIncreasing(breakPoints)) {
            throw new IllegalArgumentException("The break points were not strictly increasing.");
        }
        List<HistogramBin> binList = new ArrayList<>();
        // case of 1 break point must be handled
        if (breakPoints.length == 1) {
            // two bins, 1 below and 1 above
            binList.add(new HistogramBin(1, Double.NEGATIVE_INFINITY, breakPoints[0]));
            binList.add(new HistogramBin(2, breakPoints[0], Double.POSITIVE_INFINITY));
            return binList;
        }

        // two or more break points
        for (int i = 1; i < breakPoints.length; i++) {
            binList.add(new HistogramBin(i, breakPoints[i - 1], breakPoints[i]));
        }
        return binList;
    }
}
