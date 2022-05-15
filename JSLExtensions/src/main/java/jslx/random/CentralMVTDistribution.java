package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.primes.Primes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CentralMVTDistribution {

    private final double[][] covariances;
    private final double dof;
    private final double[][] cfL;
    private int maxM = 20;
    private int maxN = 128;
    private final int nDim;
    private final UniformRV uniformRV;
    private final Statistic statistic;
    private final double[] q;

    public CentralMVTDistribution(double dof, double[][] covariances) {
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
        q = new double[nDim];
        for (int i = 0; i < nDim; i++) {
            q[i] = Math.sqrt(Primes.nextPrime(i + 2)); // uses Apache Math Commons
        }
        // end use of Apache Commons
        this.covariances = JSLArrayUtil.copy2DArray(covariances);
        this.dof = dof;
        uniformRV = new UniformRV();
        statistic = new Statistic("MVT Statistic");
    }

    public int getDimension() {
        return nDim;
    }

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
        double[] a = new double[nDim];
        double[] b = new double[nDim];
        for (int i = 0; i < nDim; i++) {
            a[i] = integrands.get(i).getLowerLimit();
            b[i] = integrands.get(i).getLowerLimit();
        }
        return pmvt(a, b);
    }

    private double pmvt(double[] a, double[] b) {
        statistic.reset();

        for (int i = 1; i <= maxM; i++) {
            double[] u = uniformRV.sample(nDim);
            double result = functionEval(a, b, u);
            statistic.collect(result);
        }
        return statistic.getAverage();
    }

    private double functionEval(double[] a, double[] b, double[] u) {
        // must deal with 0 array indexing
        double[] ones = new double[nDim];
        Arrays.fill(ones, 1.0);
        for (int j = 1; j <= maxN; j++) {
            double[] jq = JSLArrayUtil.multiplyConstant(q, j); //j*q
            double[] w = JSLArrayUtil.addElements(jq, u);
            // get fractional part
            //JSLArrayUtil.
        }

        return 0.0;
    }
}
