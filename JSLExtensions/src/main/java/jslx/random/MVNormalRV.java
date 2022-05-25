package jslx.random;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.MVRVariableIfc;
import jsl.utilities.random.rvariable.NormalRV;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;
import java.util.Objects;

/**
 * Generations multi-dimensional normal random variates
 */
public class MVNormalRV implements MVRVariableIfc {

    protected final double[][] covariances;
    protected final double[][] cfL;// Cholesky decomposition array
    protected final int nDim;
    protected final double[] means;
    protected final NormalRV normalRV;

    /**
     * @param means       the desired mean of the random variable, must not be null
     * @param covariances the covariance of the random variable
     */
    public MVNormalRV(double[] means, double[][] covariances) {
        this(means, covariances, JSLRandom.nextRNStream());
    }

    /**
     * @param means       the desired mean of the random variable, must not be null
     * @param covariances the covariance of the random variable
     * @param stream      the stream for sampling
     */
    public MVNormalRV(double[] means, double[][] covariances, RNStreamIfc stream) {
        Objects.requireNonNull(means, "The supplied array of mean values was null");
        Objects.requireNonNull(stream, "The supplied stream for the sampler was null");
        if (!isValidCovariance(covariances)) {
            throw new IllegalArgumentException("The covariance array was not valid");
        }
        nDim = covariances.length;
        cfL = choleskyDecomposition(covariances);
        this.covariances = JSLArrayUtil.copy2DArray(covariances);
        this.means = Arrays.copyOf(means, means.length);
        normalRV = new NormalRV(0.0, 1.0, stream);
    }

    /**
     * @return the dimension of the MVT distribution
     */
    public final int getDimension() {
        return nDim;
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return normalRV.getRandomNumberStream();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        normalRV.setRandomNumberStream(stream);
    }

    @Override
    public MVRVariableIfc newInstance(RNStreamIfc rng) {
        return new MVNormalRV(this.means, this.covariances, rng);
    }

    @Override
    public MVRVariableIfc newAntitheticInstance() {
        return new MVNormalRV(this.means, this.covariances, normalRV.newAntitheticInstance().getRandomNumberStream());
    }

    @Override
    public void sample(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length != nDim) {
            throw new IllegalArgumentException("The length of the array was not the proper dimension");
        }
        double[] c = JSLArrayUtil.postProduct(cfL, normalRV.sample(nDim));
        double[] result = JSLArrayUtil.addElements(means, c);
        System.arraycopy(result, 0, array, 0, result.length);
    }

    @Override
    public void resetStartStream() {
        normalRV.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        normalRV.resetStartSubstream();
    }

    @Override
    public void advanceToNextSubstream() {
        normalRV.advanceToNextSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        normalRV.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return normalRV.getAntitheticOption();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MVNormalRV");
        sb.append(System.lineSeparator());
        sb.append("nDim = ").append(nDim);
        sb.append(System.lineSeparator());
        sb.append("means = ");
        sb.append(System.lineSeparator());
        sb.append("[");
        sb.append(JSLArrayUtil.toCSVString(means));
        sb.append("]");
        sb.append(System.lineSeparator());
        sb.append("covariances = ");
        sb.append(System.lineSeparator());
        for (int i = 0; i < covariances.length; i++) {
            sb.append("[");
            sb.append(JSLArrayUtil.toCSVString(covariances[i]));
            sb.append("]");
            sb.append(System.lineSeparator());
        }
        sb.append("Cholesky decomposition = ");
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
     * @param means       the means for the distribution
     * @param stdDevs     an array holding the standard deviations
     * @param correlation the correlation matrix as an array
     * @return the created multi-variate normal
     */
    public static MVNormalRV createRV(double[] means, double[] stdDevs, double[][] correlation) {
        double[][] covariances = convertToCovariance(stdDevs, correlation);
        return new MVNormalRV(means, covariances);
    }

    /**
     * @param means       the means for the distribution
     * @param stdDevs     an array holding the standard deviations
     * @param correlation the correlation matrix as an array
     * @param stream      the source for randomness
     * @return the created multi-variate normal
     */
    public static MVNormalRV createRV(double[] means, double[] stdDevs, double[][] correlation, RNStreamIfc stream) {
        double[][] covariances = convertToCovariance(stdDevs, correlation);
        return new MVNormalRV(means, covariances, stream);
    }

    /**
     * @param stdDevs     an array holding the standard deviations
     * @param correlation the correlation matrix as an array
     * @return the covariance matrix as determined by the correlation and standard deviations
     */
    public static double[][] convertToCovariance(double[] stdDevs, double[][] correlation) {
        Objects.requireNonNull(correlation, "The correlation array was null");
        Objects.requireNonNull(stdDevs, "The correlation array was null");
        if (correlation.length != stdDevs.length) {
            throw new IllegalArgumentException("The correlation array dimension does not match the std deviations length");
        }
        if (!isValidCorrelation(correlation)) {
            throw new IllegalArgumentException("Not a valid correlation array");
        }
        if (!JSLArrayUtil.isStrictlyPositive(stdDevs)) {
            throw new IllegalArgumentException("Not a valid std dev array");
        }
        RealMatrix s1 = MatrixUtils.createRealDiagonalMatrix(stdDevs);
        RealMatrix s2 = MatrixUtils.createRealDiagonalMatrix(stdDevs);
        RealMatrix cor = MatrixUtils.createRealMatrix(correlation);
        RealMatrix cov = s1.multiply(cor).multiply(s2);
        return cov.getData();
    }

    /**
     * @param correlation the correlation matrix to check, must not be null
     * @return true if elements are valid correlation values
     */
    public static boolean isValidCorrelation(double[][] correlation) {
        Objects.requireNonNull(correlation, "The correlation array was null");
        if (correlation.length <= 1) {
            return false;
        }
        if (!JSLArrayUtil.isSquare(correlation)) {
            return false;
        }
        for (int i = 0; i < correlation.length; i++) {
            for (int j = 0; j < correlation.length; j++) {
                if ((correlation[i][j] < -1.0) || (correlation[i][j] > 1.0)) {
                    return false;
                }
                if (correlation[i][j] != correlation[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param covariances the covariances matrix to check, must not be null
     * @return true if elements are valid covariance values
     */
    public static boolean isValidCovariance(double[][] covariances) {
        Objects.requireNonNull(covariances, "The covariance array was null");
        if (!JSLArrayUtil.isSquare(covariances)) {
            return false;
        }
        if (covariances.length <= 1) {
            return false;
        }
        double[] diagonal = JSLArrayUtil.getDiagonal(covariances);
        if (!JSLArrayUtil.isStrictlyPositive(diagonal)) {
            return false;
        }
        for (int i = 0; i < covariances.length; i++) {
            for (int j = 0; j < covariances.length; j++) {
                if (covariances[i][j] != covariances[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param covariances the correlation matrix to convert
     * @return an array holding the correlations associated with the covariance matrix
     */
    public static double[][] convertToCorrelation(double[][] covariances) {
        if (!isValidCovariance(covariances)) {
            throw new IllegalArgumentException("The covariance array was not valid");
        }
        double[] s = JSLArrayUtil.getDiagonal(covariances); // variances extracted
        JSLArrayUtil.apply(s, Math::sqrt); // take square root to get standard deviations
        for (double x : s) { // invert the array values
            x = 1.0 / x;
        }
        RealMatrix d = MatrixUtils.createRealDiagonalMatrix(s);
        RealMatrix cov = MatrixUtils.createRealMatrix(covariances);
        RealMatrix result = d.multiply(cov).multiply(d);
        return result.getData();
    }

    /**
     * @param covariances a valid variance-covariance matrix
     * @return the Cholesky decomposition of the supplied matrix
     */
    public static double[][] choleskyDecomposition(double[][] covariances) {
        if (!isValidCovariance(covariances)) {
            throw new IllegalArgumentException("The covariance array was not valid");
        }
        // use of Apache Commons
        RealMatrix cv = MatrixUtils.createRealMatrix(covariances);
        CholeskyDecomposition cd = new CholeskyDecomposition(cv);
        RealMatrix lm = cd.getL();
        return lm.getData();
    }

    public static void main(String[] args) {
        double[][] cov = {
                {1.0, 1.0, 1.0, 1.0, 1.0},
                {1.0, 2.0, 2.0, 2.0, 2.0},
                {1.0, 2.0, 3.0, 3.0, 3.0},
                {1.0, 2.0, 3.0, 4.0, 4.0},
                {1.0, 2.0, 3.0, 4.0, 5.0},
        };
        double[] means = {10.0, 10.0, 10.0, 10.0, 10.0};

        MVNormalRV rv = new MVNormalRV(means, cov);

        for (int i = 1; i <= 5; i++) {
            double[] sample = rv.sample();
            System.out.println(JSLArrayUtil.toCSVString(sample));
        }

    }
}
