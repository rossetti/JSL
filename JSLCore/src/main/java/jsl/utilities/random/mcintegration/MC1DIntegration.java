package jsl.utilities.random.mcintegration;

import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;

import java.util.Objects;

/**
 * Provides for the integration of a 1-D function via Monte-Carlo sampling.
 * See the detailed discussion for the class MCExperiment.
 *
 * The evaluation will automatically utilize
 * antithetic sampling to reduce the variance of the estimates unless the user specifies not to do so. In the case of
 * using antithetic sampling, the micro replication sample size refers to the number of independent antithetic pairs observed. Thus, this
 * will require two function evaluations for each micro replication. The user can consider the implication of the cost of
 * function evaluation versus the variance reduction obtained.
 * The default confidence level has been set to 99 percent.
 */
public class MC1DIntegration extends MCExperiment {

    protected final FunctionIfc myFunction;
    protected final RVariableIfc mySampler;
    protected RVariableIfc myAntitheticSampler;

    /**
     *
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     */
    public MC1DIntegration(FunctionIfc function, RVariableIfc sampler) {
        this(function, sampler, true);
    }

    /**
     *
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     * @param antitheticOptionOn  true represents use of antithetic sampling
     */
    public MC1DIntegration(FunctionIfc function, RVariableIfc sampler, boolean antitheticOptionOn) {
        Objects.requireNonNull(sampler, "The RVariableIfc was null!");
        Objects.requireNonNull(function, "The FunctionIfc was null!");
        this.myFunction = function;
        this.mySampler = sampler;
        if (antitheticOptionOn) {
            myAntitheticSampler = mySampler.newAntitheticInstance();
        }
        setConfidenceLevel(0.99);
    }

    @Override
    public double runSimulation(){
        if (resetStreamOptionOn) {
            mySampler.resetStartStream();
            if(isAntitheticOptionOn()){
                myAntitheticSampler.resetStartStream();
            }
        }
        return super.runSimulation();
    }

    @Override
    protected double replication(int r) {
        if (isAntitheticOptionOn()) {
            double y1 = myFunction.fx(mySampler.sample());
            double y2 = myFunction.fx(myAntitheticSampler.sample());
            return (y1 + y2) / 2.0;
        } else {
            return myFunction.fx(mySampler.sample());
        }
    }

    /**
     *
     * @return true if the antithetic option is on
     */
    public boolean isAntitheticOptionOn() {
        return myAntitheticSampler != null;
    }

    public static void main(String[] args) {

        class SinFunc implements FunctionIfc {

            public double fx(double x) {

                return (Math.PI*Math.sin(x));
            }

        }

        SinFunc f = new SinFunc();
        MC1DIntegration mc = new MC1DIntegration(f, new UniformRV(0.0, Math.PI));

//        mc.runInitialSample();
        System.out.println(mc);
        System.out.println();
        mc.runSimulation();
        System.out.println(mc);

    }


}
