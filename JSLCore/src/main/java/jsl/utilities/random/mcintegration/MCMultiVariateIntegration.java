package jsl.utilities.random.mcintegration;

import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rvariable.MVIndependentRV;
import jsl.utilities.random.rvariable.MVRVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;

import java.util.Objects;

/**
 * Provides for the integration of a multi-dimensional function via Monte-Carlo sampling.
 * The user is responsible for providing a function that when evaluated at the
 * sample from the provided sampler will evaluate to the desired integral over
 * the range of possible values of the sampler.
 * <p>
 * The sampler must have the same range as the desired integral and the function's domain (inputs) must be consistent
 * with the range (output) of the sampler. There is no checking if the user does not supply appropriate functions or samplers.
 * <p>
 * As an example, suppose we want the evaluation of the integral of g(x) over the range from a to b.
 * If the user selects the sampler as U(a,b) then the function to supply for the integration is NOT g(x).
 * The function should be h(x) = (b-a)*g(x).
 * <p>
 * In general, if the sampler has pdf, w(x), over the range a to b. Then, the function to supply for integration
 * is h(x) = g(x)/w(x). Again, the user is responsible for providing a sampler that provides values over the interval
 * of integration.  And, the user is responsible for providing the appropriate function, h(x), that will result
 * in their desired integral.  This flexibility allows the user to specify h(x) in a factorization that supports an
 * importance sampling distribution as the sampler.
 *
 * See the detailed discussion for the class MCExperiment.
 * @see MCExperiment
 *
 * The evaluation will automatically utilize
 * antithetic sampling to reduce the variance of the estimates unless the user specifies not to do so. In the case of
 * using antithetic sampling, the sample size refers to the number of independent antithetic pairs observed. Thus, this
 * will require two function evaluations at each observation. The user can consider the implication of the cost of
 * function evaluation versus the variance reduction obtained.
 */
public class MCMultiVariateIntegration extends MCExperiment {

    protected final FunctionMVIfc myFunction; //TODO generalize to check domain/range
    protected final MVRVariableIfc mySampler; //TODO generalize to check domain/range
    protected MVRVariableIfc myAntitheticSampler;

    /**
     *
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     */
    public MCMultiVariateIntegration(FunctionMVIfc function, MVRVariableIfc sampler) {
        this(function, sampler, true);
    }

    /**
     *
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     * @param antitheticOptionOn  true represents use of antithetic sampling
     */
    public MCMultiVariateIntegration(FunctionMVIfc function, MVRVariableIfc sampler, boolean antitheticOptionOn) {
        Objects.requireNonNull(sampler, "The MVRVariableIfc was null!");
        Objects.requireNonNull(function, "The FunctionMVIfc was null!");
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

        class TestFunc implements FunctionMVIfc {

            public double fx(double[] x) {
                return (4.0*x[0]*x[0]*x[1] + x[1]*x[1]);
            }

        }

        TestFunc f = new TestFunc();
        MVIndependentRV sampler = new MVIndependentRV(2, new UniformRV(0.0, 1.0));
        MCMultiVariateIntegration mc = new MCMultiVariateIntegration(f, sampler);
        mc.setConfidenceLevel(0.99);
        mc.setDesiredHWErrorBound(0.001);

//        mc.runInitialSample();
//        System.out.println(mc);
        System.out.println();
        mc.runSimulation();
        System.out.println(mc);

    }


}
