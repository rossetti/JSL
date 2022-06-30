package jsl.utilities.random.rvariable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The set of pre-defined distribution types
 */
public enum RVType {
    Bernoulli(BernoulliRV.class, RVParameters.BernoulliRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.BernoulliRVParameters();
        }
    },
    Beta(BetaRV.class, RVParameters.BetaRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.BetaRVParameters();
        }
    },
    ChiSquared(ChiSquaredRV.class, RVParameters.ChiSquaredRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.ChiSquaredRVParameters();
        }
    },
    Binomial(BinomialRV.class, RVParameters.BinomialRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.BinomialRVParameters();
        }
    },
    Constant(ConstantRV.class, RVParameters.ConstantRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.ConstantRVParameters();
        }
    },
    DUniform(DUniformRV.class, RVParameters.DUniformRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.DUniformRVParameters();
        }
    },
    Exponential(ExponentialRV.class, RVParameters.ExponentialRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.ExponentialRVParameters();
        }
    },
    Gamma(GammaRV.class, RVParameters.GammaRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.GammaRVParameters();
        }
    },
    GeneralizedBeta(GeneralizedBetaRV.class, RVParameters.GeneralizedBetaRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.GeneralizedBetaRVParameters();
        }
    },
    Geometric(GeometricRV.class, RVParameters.GeometricRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.GeometricRVParameters();
        }
    },
    JohnsonB(JohnsonBRV.class, RVParameters.JohnsonBRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.JohnsonBRVParameters();
        }
    },
    Laplace(LaplaceRV.class, RVParameters.LaplaceRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.LaplaceRVParameters();
        }
    },
    LogLogistic(LogLogisticRV.class, RVParameters.LogLogisticRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.LogLogisticRVParameters();
        }
    },
    Lognormal(LognormalRV.class, RVParameters.LognormalRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.LognormalRVParameters();
        }
    },
    NegativeBinomial(NegativeBinomialRV.class, RVParameters.NegativeBinomialRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.NegativeBinomialRVParameters();
        }
    },
    Normal(NormalRV.class, RVParameters.NormalRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.NormalRVParameters();
        }
    },
    PearsonType5(PearsonType5RV.class, RVParameters.PearsonType5RVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.PearsonType5RVParameters();
        }
    },
    PearsonType6(PearsonType6RV.class, RVParameters.PearsonType6RVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.PearsonType6RVParameters();
        }
    },
    Poisson(PoissonRV.class, RVParameters.PoissonRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.PoissonRVParameters();
        }
    },
    ShiftedGeometric(ShiftedGeometricRV.class, RVParameters.ShiftedGeometricRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.ShiftedGeometricRVParameters();
        }
    },
    Triangular(TriangularRV.class, RVParameters.TriangularRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.TriangularRVParameters();
        }
    },
    Uniform(UniformRV.class, RVParameters.UniformRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.UniformRVParameters();
        }
    },
    Weibull(WeibullRV.class, RVParameters.WeibullRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.WeibullRVParameters();
        }
    },
    DEmpirical(DEmpiricalRV.class, RVParameters.DEmpiricalRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.DEmpiricalRVParameters();
        }
    },
    Empirical(EmpiricalRV.class, RVParameters.EmpiricalRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.EmpiricalRVParameters();
        }
    },
    AR1Normal(AR1NormalRV.class, RVParameters.AR1NormalRVParameters.class) {
        public RVParameters getRVParameters() {
            return new RVParameters.AR1NormalRVParameters();
        }
    };

    private final Class<? extends RVariable> parametrizedRVClass;

    private final Class<? extends RVParameters> rvParametersClass;

    RVType(Class<? extends RVariable> rvClass, Class<? extends RVParameters> paramClass) {
        this.parametrizedRVClass = rvClass;
        rvParametersClass = paramClass;
    }

    /**
     * @return the class associated with this type
     */
    public Class<? extends RVariable> getParametrizedRVClass() {
        return parametrizedRVClass;
    }

    public Class<? extends RVParameters> getRVParameterClass() {
        return rvParametersClass;
    }

    /**
     * @return the controls associated with this type
     */
    abstract public RVParameters getRVParameters();

    public static final EnumSet<RVType> RVTYPE_SET = EnumSet.of(RVType.Bernoulli, RVType.Beta,
            RVType.ChiSquared, RVType.Binomial, RVType.Constant, RVType.DUniform,
            RVType.Exponential, RVType.Gamma, RVType.GeneralizedBeta, RVType.Geometric,
            RVType.JohnsonB, RVType.Laplace, RVType.LogLogistic, RVType.Lognormal,
            RVType.NegativeBinomial, RVType.Normal, RVType.PearsonType5, RVType.PearsonType6,
            RVType.Poisson, RVType.ShiftedGeometric, RVType.Triangular, RVType.Uniform,
            RVType.Weibull, RVType.DEmpirical, RVType.Empirical, RVType.AR1Normal);

    private static final Map<Class<? extends RVariable>, RVType> classToTypeMap = new HashMap<>();

    public static RVType getRVType(Class<? extends RVariable> clazz) {
        Objects.requireNonNull(clazz, "The sub-class of AbstractRVariable must not be null");
        if (!classToTypeMap.containsKey(clazz)) {
            throw new IllegalArgumentException("The supplied class does not map to a valid RVType");
        }
        return classToTypeMap.get(clazz);
    }

    static {
        classToTypeMap.put(BernoulliRV.class, Bernoulli);
        classToTypeMap.put(BetaRV.class, Beta);
        classToTypeMap.put(ChiSquaredRV.class, ChiSquared);
        classToTypeMap.put(BinomialRV.class, Binomial);
        classToTypeMap.put(ConstantRV.class, Constant);
        classToTypeMap.put(DUniformRV.class, DUniform);
        classToTypeMap.put(ExponentialRV.class, Exponential);
        classToTypeMap.put(GammaRV.class, Gamma);
        classToTypeMap.put(DEmpiricalRV.class, DEmpirical);
        classToTypeMap.put(GeneralizedBetaRV.class, GeneralizedBeta);
        classToTypeMap.put(GeometricRV.class, Geometric);
        classToTypeMap.put(JohnsonBRV.class, JohnsonB);
        classToTypeMap.put(LaplaceRV.class, Laplace);
        classToTypeMap.put(LogLogisticRV.class, LogLogistic);
        classToTypeMap.put(LognormalRV.class, Lognormal);
        classToTypeMap.put(NegativeBinomialRV.class, NegativeBinomial);
        classToTypeMap.put(NormalRV.class, Normal);
        classToTypeMap.put(PearsonType5RV.class, PearsonType5);
        classToTypeMap.put(PearsonType6RV.class, PearsonType6);
        classToTypeMap.put(PoissonRV.class, Poisson);
        classToTypeMap.put(TriangularRV.class, Triangular);
        classToTypeMap.put(UniformRV.class, Uniform);
        classToTypeMap.put(WeibullRV.class, Weibull);
        classToTypeMap.put(EmpiricalRV.class, Empirical);
        classToTypeMap.put(AR1NormalRV.class, AR1Normal);
    }

    public static void main(String[] args) {
        // test making some controls
        RVParameters rvParameters = Triangular.getRVParameters();
        RVariableIfc rv = rvParameters.createRVariable();
        System.out.println(rv.getValue());
        System.out.println();
        System.out.println(rvParameters);
        System.out.println();

        System.out.println(rvParameters.toJSON());
    }
}
