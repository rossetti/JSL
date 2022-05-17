package jslx.random;

import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Gamma;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.primes.Primes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CentralMVTDistribution {

    private final double[][] covariances;
    private final double dof;
    private final double[][] cfL;
    private int maxM = 12;
    private int maxN = 10;
    private final int nDim;
    private final UniformRV uniformRV;
    private final Statistic statistic;
    private final double[] q;
    private final double[] d;
    private final double[] e;
    private final double[] f;
    private final double[] y;
    private final double[] w;
    private final NormalDistribution nd = new NormalDistribution();

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
        d = new double[nDim];
        e = new double[nDim];
        f = new double[nDim];
        y = new double[nDim];
        w = new double[nDim];
        PrintWriter pw = new PrintWriter(System.out);
        System.out.println("Covariances");
        JSLArrayUtil.write(covariances, pw);
        System.out.println();
        System.out.println("L");
        JSLArrayUtil.write(cfL, pw);
        System.out.println();
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
            b[i] = integrands.get(i).getUpperLimit();
        }
        return pmvt(a, b);
    }

    private double pmvt(double[] a, double[] b) {
        statistic.reset();
        double[] u = new double[nDim];
        for (int i = 1; i <= maxM; i++) {
            uniformRV.sample(u);
//            double result = functionEval(a, b, u);
            double result = genzFunction(a, b, u);
//            double result = 1.0;
            statistic.collect(result);
        }
        return statistic.getAverage();
    }

    private double stdNormalCDF(double z) {
//        return nd.cumulativeProbability(z);
        return Normal.stdNormalCDF(z);
        //       return Normal.stdNormalCDFAbramovitzAndStegun(z);
    }

    private double transformFunction(double[] ap, double[] bp, double[] x) {
        d[0] = stdNormalCDF(ap[0] / cfL[0][0]);
        e[0] = stdNormalCDF(bp[0] / cfL[0][0]);
        f[0] = e[0] - d[0];
        System.out.printf("cfL[0][0] = %f \t ap[0] = %f \t d[0] = %f \t bp[0] = %f \t  e[0] = %f \t f[0] = %f %n",
                cfL[0][0], ap[0], d[0], bp[0], e[0], f[0]);
        for (int m = 1; m < nDim; m++) {
            double p = d[m - 1] + x[m - 1] * (e[m - 1] - d[m - 1]);
            y[m - 1] = Normal.stdNormalInvCDF(p);
//            if (p == 0.0){
//                y[m-1] = -8.0;
//            } else if (p == 1.0) {
//                y[m-1] = 8.0;
//            } else {
//                y[m - 1] = Normal.stdNormalInvCDF(p);
//            }
            double mu = sumProdLandY(m, m - 1, y);
            System.out.printf("p = %f \t y[%d] = %f \t ap[%d] = %f \t cfl[%d][%d] = %f \t mu = %f %n", p, (m - 1), y[m - 1], m, ap[m], m, m, cfL[m][m], mu);
            double za = (ap[m] - mu) / cfL[m][m];
            double zb = (bp[m] - mu) / cfL[m][m];
            d[m] = stdNormalCDF(za);
            e[m] = stdNormalCDF(zb);
            f[m] = (e[m] - d[m]) * f[m - 1];
            System.out.printf("f[%d] = %f %n", m, f[m]);
        }
        System.out.println();
        return f[nDim - 1];
    }

    public double genzFunction(double[] a, double[] b, double[] u) {
        Objects.requireNonNull(a, "The lower limit array was null");
        Objects.requireNonNull(b, "The upper limit array was null");
        Objects.requireNonNull(u, "The U(0,1) array was null");
        double[] z = new double[nDim];
        // generate r
        double r = Gamma.invChiSquareDistribution(u[nDim - 1], dof);
        double sqrtDof = Math.sqrt(dof);
        double c = r / sqrtDof;
        double ap = c * a[0] / cfL[0][0];
        double bp = c * b[0] / cfL[0][0];
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

    private double[] generateW(int j, double[] u) {
        //w holds the transforms until completed
        System.arraycopy(q, 0, w, 0, q.length);// start with fresh q
        JSLArrayUtil.multiplyConstant(w, j); //j*q
        JSLArrayUtil.addElements(w, u);// j*q + u
        // get fractional part
        JSLArrayUtil.remainder(w, 1.0);// {j*q+u}
        // now multiply by 2
        JSLArrayUtil.multiplyConstant(w, 2.0);//2{j*q+u}
        // now subtract the ones
        JSLArrayUtil.subtractConstant(w, 1.0);//2{j*q+u} - 1
        JSLArrayUtil.abs(w); //|2{j*q+u} - 1|
        return w;
    }

    private double functionEval(double[] a, double[] b, double[] u) {
        // must deal with 0 array indexing
        double sqrtDof = Math.sqrt(dof);
        Statistic stat = new Statistic();
        double[] ap = new double[nDim];
        double[] bp = new double[nDim];
        for (int j = 1; j <= maxN; j++) { // j starts at 1 in the algorithm
            double[] w = generateW(j, u);
            // w is set up now, compute s for further transform
            double s = Gamma.invChiSquareDistribution(w[nDim - 1], dof);
            System.out.println("s = " + s);
            System.arraycopy(a, 0, ap, 0, a.length);
            System.arraycopy(b, 0, bp, 0, b.length);
            double c = s / sqrtDof;
            JSLArrayUtil.multiplyConstant(ap, c);
            JSLArrayUtil.multiplyConstant(bp, c);
            double feval = transformFunction(ap, bp, w);
            stat.collect(feval);
        }
        return stat.getAverage();
    }

    private double phiSum(int m, double[] c, double[] y) {
        double sum = 0.0;
        for (int n = 0; n < m; n++) {
            sum = sum + cfL[m][n] * y[n];
        }
        return (c[m] - sum) / cfL[m][m];
    }

    public static void main(String[] args) {
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
//        intervals.add(i1);
//        intervals.add(i2);
//        intervals.add(i3);
//        intervals.add(i4);
//        intervals.add(i5);
        intervals.add(i5);
        intervals.add(i4);
        intervals.add(i3);
        intervals.add(i2);
        intervals.add(i1);
        CentralMVTDistribution d = new CentralMVTDistribution(8.0, cov);
        double v = d.cdf(intervals);
        System.out.println("v = " + v);

//        double p = Normal.stdNormalCDF(7.99999);
//        System.out.println("v = " + p);
//        JSLMath.printParameters(System.out);
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
