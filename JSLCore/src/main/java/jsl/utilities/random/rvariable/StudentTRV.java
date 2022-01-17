package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

public class StudentTRV extends AbstractRVariable {

    private final double myDoF;

    /**
     * Constructs a StudentT distribution dof degrees of freedom
     *
     * @param dof degrees of freedom
     */
    public StudentTRV(double dof) {
        this(dof, JSLRandom.nextRNStream());
    }

    /**
     * Constructs a StudentT distribution dof degrees of freedom
     *
     * @param dof       degrees of freedom
     * @param streamNum the stream number
     */
    public StudentTRV(double dof, int streamNum) {
        this(dof, JSLRandom.rnStream(streamNum));
    }

    /**
     * Constructs a StudentT distribution dof degrees of freedom
     *
     * @param dof degrees of freedom
     * @param rng the random number generator
     */
    public StudentTRV(double dof, RNStreamIfc rng) {
        super(rng);
        if (dof < 1) {
            throw new IllegalArgumentException("The degrees of freedom must be >= 1.0");
        }
        myDoF = dof;
    }

    /**
     * @return the degrees of freedom
     */
    public final double getDegreesOfFreedom() {
        return myDoF;
    }

    @Override
    protected double generate() {
        return baileysAcceptanceRejection();
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new StudentTRV(myDoF, rng);
    }

    /**
     * Directly generate a random variate using Bailey's
     * acceptance-rejection algorithm
     *
     * @return the generated random variable
     */
    public final double baileysAcceptanceRejection() {
        double W;
        double U;
        do {
            double u = myRNStream.randU01();
            double v = myRNStream.randU01();
            U = 2.0 * u - 1.0;
            double V = 2.0 * v - 1.0;
            W = U * U + V * V;
        } while (W > 1.0);

        double tmp = myDoF * (Math.pow(W, (-2.0 / myDoF)) - 1.0) / W;
        double T = U * Math.sqrt(tmp);
        return T;
    }
}
