package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

import java.util.*;

/**
 * Represents a multi-variate distribution with the specified marginals
 * The sampling of each marginal random variable is independent. That is the resulting
 * distribution has independent marginals. The supplied marginals may be the same
 * distribution or not.  If they are all the same, then use MVIndependentRV instead.
 * All the random variables will share the same stream. The sampling ensures that
 * draws are consecutive and thus independent.
 */
public class MVIndependentMarginals implements MVRVariableIfc{

    protected final List<RVariableIfc> myRVs;
    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myRNStream;

    public MVIndependentMarginals(List<RVariableIfc> marginals, RNStreamIfc stream) {
        Objects.requireNonNull(marginals, "The supplied list of marginals was null");
        myRNStream = Objects.requireNonNull(stream,"RNStreamIfc stream must be non-null" );
        myRVs = new ArrayList<>();
        for(RVariableIfc rv: marginals){
            if (rv == null){
                throw new IllegalArgumentException("The list of marginal random variables had a null member!");
            }
            myRVs.add(rv.newInstance(stream));
        }
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myRNStream;
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myRNStream = Objects.requireNonNull(stream,"RNStreamIfc stream must be non-null" );
        for(RVariableIfc rv: myRVs){
            rv.setRandomNumberStream(myRNStream);
        }
    }

    @Override
    public MVRVariableIfc newInstance(RNStreamIfc rng) {
        return new MVIndependentMarginals(myRVs, rng);
    }

    @Override
    public MVRVariableIfc newAntitheticInstance() {
        return new MVIndependentMarginals(myRVs, myRNStream.newAntitheticInstance());
    }

    @Override
    public double[] sample() {
        double[] values = new double[myRVs.size()];
        int i = 0;
        for (RVariableIfc rv: myRVs) {
            values[i] = rv.sample();
            i++;
        }
        return values;
    }

    @Override
    public void resetStartStream() {
        myRNStream.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        myRNStream.resetStartSubstream();
    }

    @Override
    public void advanceToNextSubstream() {
        myRNStream.advanceToNextSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myRNStream.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myRNStream.getAntitheticOption();
    }
}
