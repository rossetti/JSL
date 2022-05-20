package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Objects;

/**
 * Represents a multi-variate distribution with the specified dimensions.
 * The sampling of each dimension is independent. That is the resulting
 * distribution has independent marginals that are represented by the same
 * distribution as provided by the supplied random variable
 */
public class MVIndependentRV implements MVRVariableIfc {

    protected final RVariableIfc myRV;
    protected final int myDimension;

    /**
     * @param dimension         the dimension, must be at least 1
     * @param theRandomVariable the random variable for the marginals
     */
    public MVIndependentRV(int dimension, RVariableIfc theRandomVariable) {
        Objects.requireNonNull(theRandomVariable, "The supplied random variable was null");
        if (dimension <= 0) {
            throw new IllegalArgumentException("The multi-variate dimension must be at least 1");
        }
        myDimension = dimension;
        this.myRV = theRandomVariable;
    }

    @Override
    public MVRVariableIfc newInstance(RNStreamIfc rng) {
        return new MVIndependentRV(myDimension, myRV.newInstance());
    }

    @Override
    public MVRVariableIfc newAntitheticInstance() {
        return new MVIndependentRV(myDimension, myRV.newAntitheticInstance());
    }

    @Override
    public int getDimension() {
        return myDimension;
    }

    @Override
    public void sample(double[] array) {
        Objects.requireNonNull(array, "The supplied array was null");
        if (array.length != getDimension()){
            throw new IllegalArgumentException("The size of the array to fill does not match the sampling dimension!");
        }
        myRV.sample(array);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myRV.getRandomNumberStream();
    }

    @Override
    public int getStreamNumber() {
        return myRV.getStreamNumber();
    }

    @Override
    public void resetStartStream() {
        myRV.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        myRV.resetStartSubstream();
    }

    @Override
    public void advanceToNextSubstream() {
        myRV.advanceToNextSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myRV.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myRV.getAntitheticOption();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myRV.setRandomNumberStream(stream);
    }

    @Override
    public void setRandomNumberStream(int streamNumber) {
        myRV.setRandomNumberStream(streamNumber);
    }
}
