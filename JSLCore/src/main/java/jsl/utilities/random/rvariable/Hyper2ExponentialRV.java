package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Two Exponentials mixed to get a hyper-exponential. For higher
 * order hyper-exponential use MixtureRV.  The mixing probability is the
 * probability of getting the first exponential distribution with mean1
 */
public class Hyper2ExponentialRV extends AbstractRVariable {

    private final double myMixingProb;
    private final double myMean1;
    private final double myMean2;

    public Hyper2ExponentialRV(double mixingProb, double mean1, double mean2) {
        this(mixingProb, mean1, mean2, JSLRandom.nextRNStream());
    }

    public Hyper2ExponentialRV(double mixingProb, double mean1, double mean2, int streamNum) {
        this(mixingProb, mean1, mean2, JSLRandom.rnStream(streamNum));
    }

    public Hyper2ExponentialRV(double mixingProb, double mean1, double mean2, RNStreamIfc stream) {
        super(stream);
        if ((mixingProb < 0.0) || (mixingProb > 1.0)) {
            throw new IllegalArgumentException("Mixing Probability must be [0,1]");
        }
        if (mean1 <= 0.0) {
            throw new IllegalArgumentException("Exponential mean1 must be > 0.0");
        }
        if (mean2 <= 0.0) {
            throw new IllegalArgumentException("Exponential mean2 must be > 0.0");
        }
        myMixingProb = mixingProb;
        myMean1 = mean1;
        myMean2 = mean2;
    }

    @Override
    protected double generate() {
        final double v = JSLRandom.rBernoulli(myMixingProb, myRNStream);
        if (v >= 1.0) {
            return JSLRandom.rExponential(myMean1, myRNStream);
        } else {
            return JSLRandom.rExponential(myMean2, myRNStream);
        }
    }

    /** Gets the mixing probability
     * @return The mixing probability
     */
    public final double getMixingProb() {
        return (myMixingProb);
    }

    /**
     *
     * @return the mean1 value
     */
    public final double getMean1() {
        return myMean1;
    }

    /**
     *
     * @return the mean1 value
     */
    public final double getMean2() {
        return myMean2;
    }

    /**
     *
     * @param stream the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    @Override
    public final Hyper2ExponentialRV newInstance(RNStreamIfc stream){
        return new Hyper2ExponentialRV(myMixingProb, myMean1, myMean2, stream);
    }

}
