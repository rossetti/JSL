package jsl.utilities.random.rvariable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The set of pre-defined distribution types
 */
public enum RVType {
    Bernoulli(BernoulliRV.class) {
        public RVParameters getRVParameters() {
            return BernoulliRV.createParameters();
        }
    },
    Beta(BetaRV.class) {
        public RVParameters getRVParameters() {
            return BetaRV.createParameters();
        }
    },
    ChiSquared(ChiSquaredRV.class) {
        public RVParameters getRVParameters() {
            return ChiSquaredRV.createParameters();
        }
    },
    Binomial(BinomialRV.class) {
        public RVParameters getRVParameters() {
            return BinomialRV.createParameters();
        }
    },
    Constant(ConstantRV.class) {
        public RVParameters getRVParameters() {
            return ConstantRV.createParameters();
        }
    },
    DUniform(DUniformRV.class) {
        public RVParameters getRVParameters() {
            return DUniformRV.createParameters();
        }
    },
    Exponential(ExponentialRV.class) {
        public RVParameters getRVParameters() {
            return ExponentialRV.createParameters();
        }
    },
    Gamma(GammaRV.class) {
        public RVParameters getRVParameters() {
            return GammaRV.createParameters();
        }
    },
    GeneralizedBeta(GeneralizedBetaRV.class) {
        public RVParameters getRVParameters() {
            return GeneralizedBetaRV.createParameters();
        }
    },
    Geometric(GeometricRV.class) {
        public RVParameters getRVParameters() {
            return GeometricRV.createParameters();
        }
    },
    JohnsonB(JohnsonBRV.class) {
        public RVParameters getRVParameters() {
            return JohnsonBRV.createParameters();
        }
    },
    Laplace(LaplaceRV.class) {
        public RVParameters getRVParameters() {
            return LaplaceRV.createParameters();
        }
    },
    LogLogistic(LogLogisticRV.class) {
        public RVParameters getRVParameters() {
            return LogLogisticRV.createParameters();
        }
    },
    Lognormal(LognormalRV.class) {
        public RVParameters getRVParameters() {
            return LognormalRV.createParameters();
        }
    },
    NegativeBinomial(NegativeBinomialRV.class) {
        public RVParameters getRVParameters() {
            return NegativeBinomialRV.createParameters();
        }
    },

    Normal(NormalRV.class) {
        public RVParameters getRVParameters() {
            return NormalRV.createParameters();
        }
    },
    PearsonType5(PearsonType5RV.class) {
        public RVParameters getRVParameters() {
            return PearsonType5RV.createParameters();
        }
    },
    PearsonType6(PearsonType6RV.class) {
        public RVParameters getRVParameters() {
            return PearsonType6RV.createParameters();
        }
    },

    Poisson(PoissonRV.class) {
        public RVParameters getRVParameters() {
            return PoissonRV.createParameters();
        }
    },
    ShiftedGeometric(ShiftedGeometricRV.class) {
        public RVParameters getRVParameters() {
            return ShiftedGeometricRV.createParameters();
        }
    },
    Triangular(TriangularRV.class) {
        public RVParameters getRVParameters() {
            return TriangularRV.createParameters();
        }
    },
    Uniform(UniformRV.class) {
        public RVParameters getRVParameters() {
            return UniformRV.createParameters();
        }
    },
    Weibull(WeibullRV.class) {
        public RVParameters getRVParameters() {
            return WeibullRV.createParameters();
        }
    },
    DEmpirical(DEmpiricalRV.class) {
        public RVParameters getRVParameters() {
            return DEmpiricalRV.createParameters();
        }
    },
    Empirical(EmpiricalRV.class) {
        public RVParameters getRVParameters() {
            return EmpiricalRV.createParameters();
        }
    },
    AR1Normal(DEmpiricalRV.class) {
        public RVParameters getRVParameters() {
            return AR1NormalRV.createParameters();
        }
    };

    private final Class<? extends RVariable> clazz;

    RVType(Class<? extends RVariable> clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the class associated with this type
     */
    public Class<? extends RVariable> asClass() {
        return clazz;
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
