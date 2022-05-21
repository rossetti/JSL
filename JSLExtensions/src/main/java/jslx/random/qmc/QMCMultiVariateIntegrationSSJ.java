package jslx.random.qmc;

import jsl.utilities.random.mcintegration.MCExperiment;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.statistic.Statistic;
import umontreal.ssj.hups.*;
import umontreal.ssj.rng.MRG32k3a;

import java.util.Objects;

/**
 * The purpose of this class is to facilitate the use of quasi-Monte Carlo
 * methods when solving multi-variable integration problems. This class
 * relies on the hups package within the SSJ library
 *
 * @see <a href="https://github.com/umontreal-simul/ssj</a>
 * <p>
 * This class takes a simple approach to allowing QMC methods by
 * making some reasonable assumptions about the generation methods.
 * These assumptions may or not work well for specific integration problems.
 * The user is encouraged to investigate the hups package. See also this paper.
 * @see <a href="https://people.cs.kuleuven.be/~dirk.nuyens/taiwan/QMC-practical-guide-20161107-1up.pdf</a>
 * <p>
 * This class uses randomized QMC.  Thus, there is an inner loop that uses the deterministic
 * QMC sequence (point set) and an outer loop that averages over randomized executions of the QMC inner
 * loop results.  In general, the inner loop is over a large sequence (n) and the outer loop is
 * over a relatively smaller sequence (m).  Just as in MCMultiVariateIntegration, the outer loop
 * uses an absolute error stopping criter after an initial sample. The main user control is
 * still at this outer loop.
 * <p>
 * We assume that the supplied function has been standardized on the unit hypercube.
 * Since a sampler is not provided by the user, the dimension must be supplied by
 * the user.
 */
public class QMCMultiVariateIntegrationSSJ extends MCExperiment {

    protected final FunctionMVIfc myFunction;
    protected final RQMCPointSet myRQMCPointSet;
    protected final PointSetIterator myPointSetIterator;
    protected final double[] uniforms;
    protected final int numPoints;
    protected final int myDimension;

    /**
     * @param dimension the dimension of the function to integrate
     * @param function  the function to integrate, must not be null
     */
    public QMCMultiVariateIntegrationSSJ(int dimension, FunctionMVIfc function) {
        Objects.requireNonNull(function, "The FunctionMVIfc was null!");
        if (dimension < 1) {
            throw new IllegalArgumentException("The dimension of the function must be 1 or more");
        }
        this.myFunction = function;
//        setConfidenceLevel(0.99);
//        setMaxSampleSize(500);
        myDimension = dimension;
        DigitalNet pointSet = new SobolSequence(16, 31, dimension); // n = 2^{16} points in d dim.
        numPoints = pointSet.getNumPoints();
        PointSetRandomization rand = new LMScrambleShift(new MRG32k3a());
        myRQMCPointSet = new RQMCPointSet(pointSet, rand);
        myPointSetIterator = myRQMCPointSet.iterator();
        uniforms = new double[myDimension];
    }

    @Override
    protected void beforeMacroReplications() {
        microRepSampleSize = myRQMCPointSet.getNumPoints();
        PointSetIterator stream = myRQMCPointSet.iterator();
    }

    @Override
    protected void beforeMicroReplications() {
        myRQMCPointSet.randomize();
        myPointSetIterator.resetStartStream();
    }

    @Override
    protected double replication(int r) {
        myPointSetIterator.nextArrayOfDouble(uniforms, 0, uniforms.length);
        double y = myFunction.fx(uniforms);
        myPointSetIterator.resetNextSubstream();
        return y;
    }

//    @Override
//    protected double sample(int m) {
//        // this is the outer loop. It returns the number of samples performed
//        // evaluate invokes this function
//        int n = myRQMCPointSet.getNumPoints();
//        PointSetIterator stream = myRQMCPointSet.iterator();
//        for (int i = 1; i <= m; i++) {
//            myRQMCPointSet.randomize();
//            stream.resetStartStream();
//            double f = qmcSample(n, stream);
//            statistic.collect(f);
//            if (checkStoppingCriteria()) {
//                return statistic.getCount();
//            }
//        }
//        return statistic.getCount();
//    }

    protected double qmcSample(int n, PointSetIterator stream) {
        double[] u = new double[myDimension];
        Statistic s = new Statistic();
        for (int i = 1; i <= n; i++) {
            stream.nextArrayOfDouble(u, 0, u.length);
            double y = myFunction.fx(u);
            s.collect(y);
            stream.resetNextSubstream();
        }
        return s.getAverage();
    }
}
