package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Objects;

/**
 * Shifts the generated value of the supplied random variable by the shift amount.
 * The shift amount must be positive.
 */
public final class ShiftedRV extends AbstractRVariable {

    private final double myShift;
    private final RVariableIfc myRV;

    /**
     * Uses a stream from the default stream factory
     *
     * @param shift a non-negative value
     * @param rv    the random variable to shift
     */
    public ShiftedRV(double shift, RVariableIfc rv) {
        this(shift, rv, JSLRandom.nextRNStream());
    }

    /**
     * @param shift     a non-negative value
     * @param rv        the random variable to shift
     * @param streamNum the stream number
     */
    public ShiftedRV(double shift, RVariableIfc rv, int streamNum) {
        this(shift, rv, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param shift a non-negative value
     * @param rv    the random variable to shift
     * @param rng   the generator to use
     */
    public ShiftedRV(double shift, RVariableIfc rv, RNStreamIfc rng) {
        super(rng);
        Objects.requireNonNull(rv, "The random variable must not be null");
        if (shift < 0.0) {
            throw new IllegalArgumentException("The shift should not be < 0.0");
        }
        myRV = rv.newInstance(rng);
        myShift = shift;
    }

    @Override
    protected double generate() {
        return myShift + myRV.getValue();
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new ShiftedRV(myShift, myRV, rng);
    }
}
