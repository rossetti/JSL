package jsl.utilities.random.rvariable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The set of pre-defined distribution types
 */
public enum RVType {
    Bernoulli(BernoulliRV.class), Beta(BetaRV.class), ChiSquared(ChiSquaredRV.class),
    Binomial(BernoulliRV.class), Constant(ConstantRV.class), DUniform(DUniformRV.class),
    Exponential(ExponentialRV.class), Gamma(GammaRV.class), GeneralizedBeta(GeneralizedBetaRV.class),
    Geometric(GeometricRV.class), JohnsonB(JohnsonBRV.class), Laplace(LaplaceRV.class),
    LogLogistic(LogLogisticRV.class), Lognormal(LognormalRV.class), NegativeBinomial(NegativeBinomialRV.class),

    Normal(NormalRV.class), PearsonType5(PearsonType5RV.class), PearsonType6(PearsonType6RV.class),

    Poisson(PoissonRV.class), ShiftedGeometric(ShiftedGeometricRV.class),
    Triangular(TriangularRV.class), Uniform(UniformRV.class), Weibull(WeibullRV.class),
    DEmpirical(DEmpiricalRV.class), Empirical(EmpiricalRV.class);

    private final Class<? extends AbstractRVariable> clazz;

    RVType(Class<? extends AbstractRVariable> clazz) {
        this.clazz = clazz;
    }

    /**
     *
     * @return the class associated with this type
     */
    public Class<? extends AbstractRVariable> asClass() {
        return clazz;
    }

    /**
     *
     * @return the controls associated with this type
     */
    public RVControls getRVControls(){
        return myFactories.get(this);
    }

    public static final EnumSet<RVType> RVTYPE_SET = EnumSet.of(RVType.Bernoulli, RVType.Beta,
            RVType.ChiSquared, RVType.Binomial, RVType.Constant, RVType.DUniform,
            RVType.Exponential, RVType.Gamma, RVType.GeneralizedBeta, RVType.Geometric,
            RVType.JohnsonB, RVType.Laplace, RVType.LogLogistic, RVType.Lognormal,
            RVType.NegativeBinomial, RVType.Normal, RVType.PearsonType5, RVType.PearsonType6,
            RVType.Poisson, RVType.ShiftedGeometric, RVType.Triangular, RVType.Uniform,
            RVType.Weibull, RVType.DEmpirical, RVType.Empirical);

    private static final Map<Class<? extends AbstractRVariable>, RVType> classToTypeMap = new HashMap<>();

    public static RVType getRVType(Class<? extends AbstractRVariable> clazz){
        Objects.requireNonNull(clazz, "The sub-class of AbstractRVariable must not be null");
        if (!classToTypeMap.containsKey(clazz)){
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
    }

    private static final Map<RVType, RVControls> myFactories;

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
    public static RVControls getRVControls(RVType type) {
        Objects.requireNonNull(type, "The random variable type must not be null");
        if (!myFactories.containsKey(type)){
            throw new IllegalArgumentException("The supplied RVType does not have a registered RVControls");
        }
        return myFactories.get(type);
    }

}
