package jsl.utilities.random.mcintegration;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rvariable.MVIndependentRV;
import jsl.utilities.random.rvariable.MVRVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

/**
 * Provides for the integration of a multi-variate function via Monte-Carlo sampling.
 *
 * See the detailed discussion for the class MCIntegration.
 *
 * The evaluation will automatically utilize
 * antithetic sampling to reduce the variance of the estimates unless the user specifies not to do so. In the case of
 * using antithetic sampling, the sample size refers to the number of independent antithetic pairs observed. Thus, this
 * will require two function evaluations at each observation. The user can consider the implication of the cost of
 * function evaluation versus the variance reduction obtained.
 */
public class MCMultiVariateIntegration extends MCIntegration {

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
    protected double sample(int n) {
        if (resetStreamOptionOn) {
            mySampler.resetStartStream();
            if(isAntitheticOptionOn()){
                myAntitheticSampler.resetStartStream();
            }
        }
        double y;
        for (int i = 1; i <= n; i++) {
            if (isAntitheticOptionOn()) {
                double y1 = myFunction.fx(mySampler.sample());
                double y2 = myFunction.fx(myAntitheticSampler.sample());
                y = (y1 + y2) / 2.0;
            } else {
                y = myFunction.fx(mySampler.sample());
            }
            statistic.collect(y);
            if (checkStoppingCriteria()) {
                return statistic.getCount();
            }
        }
        return statistic.getCount();
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
        mc.setDesiredAbsError(0.01);

        mc.runInitialSample();
        System.out.println(mc);
        System.out.println();
        mc.evaluate();
        System.out.println(mc);

    }


}
