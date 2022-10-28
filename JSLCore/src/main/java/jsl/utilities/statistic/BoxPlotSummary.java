package jsl.utilities.statistic;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * Prepares the statistical quantities typically found on a box plot.
 * This implementation uses a full sort of the data. The original data
 * is not changed. Users may want to look for more efficient methods for use with very large data sets.
 */
public class BoxPlotSummary {

    private final double median;
    private final double firstQuartile;
    private final double thirdQuartile;
    private final double min;
    private final double max;
    private final double[] orderStatistics;
    private final Statistic statistic;

    /**
     * @param data the data to be summarized, must not be null and must not contain any Double.NaN values
     */
    public BoxPlotSummary(double[] data) {
        Objects.requireNonNull(data, "The data array was null");
        if (JSLArrayUtil.checkForNaN(data)) {
            throw new IllegalArgumentException("There were NaN in the array");
        }
        if (data.length == 1) {
            median = data[0];
            firstQuartile = data[0];
            thirdQuartile = data[0];
            min = data[0];
            max = data[0];
            orderStatistics = new double[]{data[0]};
        } else {
            orderStatistics = Arrays.copyOf(data, data.length);
            median = Statistic.getMedian(orderStatistics);
            min = orderStatistics[0];
            max = orderStatistics[orderStatistics.length - 1];
            firstQuartile = percentile(0.25);
            thirdQuartile = percentile(0.75);
//            firstQuartile = quantile(0.25);
//            thirdQuartile = quantile(0.75);
        }
        statistic = new Statistic("Summary Statistics");
        statistic.collect(data);
    }

    public final Statistic getStatistic() {
        return statistic.newInstance();
    }

    public final double[] getOrderStatistics() {
        return Arrays.copyOf(orderStatistics, orderStatistics.length);
    }

    /**
     * Uses definition 7, as per R definitions
     *
     * @param p the percentile, must be within (0, 1)
     * @return the quantile
     */
    public final double quantile(double p) {
        return Statistic.quantile(orderStatistics, p);
    }

    /**
     * As per Apache Math commons
     *
     * @param p the percentile, must be within (0, 1)
     * @return the percentile
     */
    public final double percentile(double p) {
        return Statistic.percentile(orderStatistics, p);
    }

    /**
     * @return the estimated median
     */
    public final double getMedian() {
        return median;
    }

    /**
     * @return the estimated 1st quartile
     */
    public final double getFirstQuartile() {
        return firstQuartile;
    }

    /**
     * @return the estimated 3rd quartile
     */
    public final double getThirdQuartile() {
        return thirdQuartile;
    }

    /**
     * @return the minimum value in the data
     */
    public final double getMin() {
        return min;
    }

    /**
     * @return the maximum value of the data
     */
    public final double getMax() {
        return max;
    }

    /**
     * @return the difference between 3rd quartile and 1st quartile
     */
    public final double interQuartileRange() {
        return thirdQuartile - firstQuartile;
    }

    /**
     * @return the difference between max and min
     */
    public final double range() {
        return max - min;
    }

    /**
     * @return the 1st quartile minus 1.5 times the inter-quartile range
     */
    public final double lowerInnerFence() {
        return firstQuartile - 1.5 * interQuartileRange();
    }

    /**
     * @return the 1st quartile minus 3 times the inter-quartile range
     */
    public final double lowerOuterFence() {
        return firstQuartile - 3.0 * interQuartileRange();
    }

    /**
     * @return the 3rd quartile plus 1.5 times the inter-quartile range
     */
    public final double upperInnerFence() {
        return thirdQuartile + 1.5 * interQuartileRange();
    }

    /**
     * @return the 3rd quartile plus 3 times the inter-quartile range
     */
    public final double upperOuterFence() {
        return thirdQuartile + 3.0 * interQuartileRange();
    }

    /**
     * @return the data points less than or equal to the lower outer fence
     */
    public final double[] lowerOuterPoints() {
        int i = 0;
        double l2 = lowerOuterFence();
        for (double v : orderStatistics) {
            if (v <= l2) {
                i = i + 1;
            } else {
                break;
            }
        }
        return Arrays.copyOf(orderStatistics, i);
    }

    /**
     * @return the data points greater than or equal to the upper outer fence
     */
    public final double[] upperOuterPoints() {
        int i = 0;
        int n = orderStatistics.length - 1;
        double u2 = upperOuterFence();
        for (int j = n; j >= 0; j--) {
            if (orderStatistics[j] >= u2) {
                i = i + 1;
            } else {
                break;
            }
        }
        return Arrays.copyOfRange(orderStatistics, n - i, n);
    }

    /**
     * @return the points between the lower inner and outer fences
     */
    public final double[] pointsBtwLowerInnerAndOuterFences() {
        Interval i = new Interval(lowerOuterFence(), lowerInnerFence());
        return JSLArrayUtil.getDataInInterval(orderStatistics, i);
    }

    /**
     * @return the points between the upper inner and outer fences
     */
    public final double[] pointsBtwUpperInnerAndOuterFences() {
        Interval i = new Interval(upperInnerFence(), upperOuterFence());
        return JSLArrayUtil.getDataInInterval(orderStatistics, i);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BoxPlotSummary");
        sb.append(System.lineSeparator());
        sb.append("median = ").append(median);
        sb.append(System.lineSeparator());
        sb.append("firstQuartile = ").append(firstQuartile);
        sb.append(System.lineSeparator());
        sb.append("thirdQuartile = ").append(thirdQuartile);
        sb.append(System.lineSeparator());
        sb.append("min = ").append(min);
        sb.append(System.lineSeparator());
        sb.append("max = ").append(max);
        sb.append(System.lineSeparator());
        sb.append("Statistical Summary ");
        sb.append(System.lineSeparator());
        sb.append(statistic);
        return sb.toString();
    }

    public static void main(String[] args) {
        double[] x = new double[]{9.57386907765005, 12.2683505035727, 9.57737208532118, 9.46483590382401,
                10.7426270820019, 13.6417539779286, 14.4009905460358, 11.9644504015896, 6.26967756749078,
                11.6697189446463, 8.05817835081046, 9.15420225990855, 12.6661856696446, 5.55898016788418,
                11.5509859097328, 8.09378382643764, 10.2800698254101, 11.8820042371248, 6.83122972495244,
                7.76415517242856, 8.07037124078289, 10.1936926483873, 6.6056340897386, 8.67523311054818,
                10.2860106642238, 7.18655355368101, 13.7326532837148, 10.8384432167312, 11.20127362594,
                9.10597298849603, 13.1143167471166, 11.461547274424, 12.8686686397317, 11.6123823346184,
                11.1766595994422, 9.96640484955756, 7.60884520541602, 10.4027823841526, 13.6119110527044,
                10.1927388924956, 11.0479192016999, 10.8335646086984, 11.3464245020951, 11.7370035652721,
                7.86882502350181, 10.1677674083453, 7.19107507247878, 10.3219440236855, 11.8751033160937,
                12.0507178860171, 10.2452271541559, 12.3574170333615, 8.61783541196255, 10.8759327855332,
                10.8965790925989, 9.78508632755152, 9.57354838522572, 10.668697248695, 10.4413115727436,
                11.7056055258128, 10.6836383463882, 9.00275936849233, 11.1546020461964, 11.5327569604436,
                12.6632213399552, 9.04144921258077, 8.34070478790018, 8.90537066541892, 8.9538251666728,
                10.6587406131769, 9.46657058183544, 11.9067728468743, 7.31151723229678, 10.3473820074211,
                8.51409684117935, 15.061683701397, 7.67016173387284, 9.63463245914518, 11.9544975062154,
                8.75291180980926, 10.5902626954236, 10.7290328701981, 11.6103046633603, 9.18588529341066,
                11.7832770526927, 11.5803842329369, 8.77282669099311, 11.1605258465085, 9.87370336332192,
                11.0792461569289, 12.1457106152585, 8.16900025019337, 12.0963212801111, 10.7943060404262,
                10.6648080893662, 10.7821384837463, 9.20756684199006, 13.0421837951471, 8.50476579169282, 7.7653569673433};

        BoxPlotSummary boxPlotSummary = new BoxPlotSummary(x);
        System.out.println(boxPlotSummary);

        double m = Statistic.getMedian(x);
        System.out.println("median = " + m);
    }
}
