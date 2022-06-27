/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package jsl.utilities.random.rvariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static jsl.utilities.random.rvariable.RVariableIfc.RVType.*;

/**
 * Permits construction of random variables based on factory instances defined by
 * controls.  The controls hold the (key, value) pairs that represent distributional
 * parameters by name.  The user of the control is responsible for setting legal
 * parameter values on the controls as required by the desired random variable type.
 * The returned control can be used to make many instances of random variables based
 * on its current parameter settings.
 */
public class RVFactory {

    private static Map<RVariableIfc.RVType, RVControls> myFactories;

    static {
        myFactories = new HashMap<>();
        myFactories.put(Bernoulli, BernoulliRV.makeControls());
        myFactories.put(Beta, BetaRV.makeControls());
        myFactories.put(ChiSquared, ChiSquaredRV.makeControls());
        myFactories.put(Binomial, BinomialRV.makeControls());
        myFactories.put(Constant, ConstantRV.makeControls());
        myFactories.put(DUniform, DUniformRV.makeControls());
        myFactories.put(Exponential, ExponentialRV.makeControls());
        myFactories.put(Gamma, GammaRV.makeControls());
        myFactories.put(DEmpirical, DEmpiricalRV.makeControls());
        myFactories.put(GeneralizedBeta, GeneralizedBetaRV.makeControls());
        myFactories.put(Geometric, GeometricRV.makeControls());
        myFactories.put(JohnsonB, JohnsonBRV.makeControls());
        myFactories.put(Laplace, LaplaceRV.makeControls());
        myFactories.put(LogLogistic, LogLogisticRV.makeControls());
        myFactories.put(Lognormal, LognormalRV.makeControls());
        myFactories.put(NegativeBinomial, NegativeBinomialRV.makeControls());
        myFactories.put(Normal, NormalRV.makeControls());
        myFactories.put(PearsonType5, PearsonType5RV.makeControls());
        myFactories.put(PearsonType6, PearsonType6RV.makeControls());
        myFactories.put(Poisson, PoissonRV.makeControls());
        myFactories.put(Triangular, TriangularRV.makeControls());
        myFactories.put(Uniform, UniformRV.makeControls());
        myFactories.put(Weibull, WeibullRV.makeControls());
        myFactories.put(Empirical, EmpiricalRV.makeControls());
    }

    /**
     * @param type the type of the random variable
     * @return an optional holding the control or empty if the type was not found
     */
    public static RVControls getRVControls(RVariableIfc.RVType type) {
        Objects.requireNonNull(type, "The random variable type must not be null");
        return myFactories.get(type);
    }

    public static void main(String[] args) {
        // test making some controls
        RVControls rvControls = getRVControls(Triangular);
        RVariableIfc rv = rvControls.makeRVariable();
        System.out.println(rv.getValue());
        System.out.println();
        System.out.println(rvControls);
        System.out.println();
        System.out.println(rvControls.toJSON());
    }
}
