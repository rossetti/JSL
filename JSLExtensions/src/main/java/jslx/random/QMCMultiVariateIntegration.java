package jslx.random;

import jsl.utilities.random.mcintegration.MCExperiment;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.Objects;

/**
 * The purpose of this class is to facilitate the use of quasi-Monte Carlo
 * methods when solving multi-variable integration problems. This class
 * relies on no outside packages.
 *
 * This class takes a simple approach to allowing QMC methods by
 * making some reasonable assumptions about the generation methods and implementing
 * a default QMC point set.
 *
 * These assumptions may or not work well for specific integration problems.
 * For further investigation of these methods the user can reference the following paper.
 * @see <a href="https://people.cs.kuleuven.be/~dirk.nuyens/taiwan/QMC-practical-guide-20161107-1up.pdf</a>
 * <p>
 * This class uses randomized QMC.  Thus, there is an inner loop that uses the deterministic
 * QMC sequence (point set) and an outer loop that averages over randomized executions of the QMC inner
 * loop results.  In general, the inner loop is over a large sequence (n) and the outer loop is
 * over a relatively smaller sequence (m).  Just as in MCMultiVariateIntegration, the outer loop
 * uses an absolute error stopping criterion after an initial sample. The main user control is
 * at this outer loop.
 * <p>
 * We assume that the supplied function has been standardized on the unit hypercube.
 * Since a sampler is not provided by the user, the dimension must be supplied by
 * the user.
 */
public class QMCMultiVariateIntegration extends MCExperiment {

    protected final FunctionMVIfc myFunction;
    protected final double[] uniforms;
    protected final int myDimension;
    protected final RNStreamIfc myStream;

    /**
     * @param dimension the dimension of the function to integrate
     * @param function  the function to integrate, must not be null
     */
    public QMCMultiVariateIntegration(int dimension, FunctionMVIfc function) {
        this(dimension, function, JSLRandom.nextRNStream());
    }

    /**
     * @param dimension the dimension of the function to integrate
     * @param function  the function to integrate, must not be null
     * @param stream the source of randomness
     */
    public QMCMultiVariateIntegration(int dimension, FunctionMVIfc function, RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The RNStreamIfc was null!");
        Objects.requireNonNull(function, "The FunctionMVIfc was null!");
        if (dimension < 1) {
            throw new IllegalArgumentException("The dimension of the function must be 1 or more");
        }
        myStream = stream;
        myFunction = function;
        myDimension = dimension;
        uniforms = new double[myDimension];
    }

    @Override
    protected void beforeMacroReplications() {

    }

    @Override
    protected void beforeMicroReplications() {

    }

    @Override
    protected double replication(int r) {

        return 0.0;
    }
}
