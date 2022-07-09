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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;

import java.util.*;

public abstract class RVParameters {

    public enum DataType {
        DOUBLE(Double.class),
        INTEGER(Integer.class),
        DOUBLE_ARRAY(double[].class);

        private final Class<?> clazz;

        DataType(Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * @return the class associated with this type
         */
        public Class<?> asClass() {
            return clazz;
        }
    }

    /**
     * To allow setting/tracking of name of parameter
     */
    private String className;

    private RVType type;

    /**
     * The Map that hold the parameters as pairs
     * key = name of parameter
     * value = value of the parameter as a Double
     */
    private final Map<String, Double> doubleParameters;

    /**
     * The Map that hold the parameters as pairs
     * key = name of parameter
     * value = value of the parameter as an Integer
     */
    private final Map<String, Integer> integerParameters;

    /**
     * The Map that hold the parameters as pairs
     * key = name of parameter
     * value = value of the parameter as a double[]
     */
    private final Map<String, double[]> doubleArrayParameters;

    /**
     * A map to keep track of parameter names and their types
     */
    //private final transient Map<String, Class<?>> myParameterTypes;
    //TODO marked as transient to prevent JSON serialization error with Class<?> type
    // might be able to write custom adapter.
    // https://stackoverflow.com/questions/8119138/gson-not-parsing-class-variable
    // or consider if field is necessary, i.e. remove the field
    private final Map<String, DataType> dataTypes;

    public RVParameters() {
        doubleParameters = new HashMap<>();
        integerParameters = new HashMap<>();
        doubleArrayParameters = new HashMap<>();
        dataTypes = new HashMap<>();
        fillParameters();
    }

    /**
     * @return the type of the random variable
     */
    public final RVType getType() {
        return type;
    }

    /**
     * Used internally to set the type
     *
     * @param type the type
     */
    final void setRVType(RVType type) {
        Objects.requireNonNull(type, "The supplied type was null");
        this.type = type;
    }

    abstract protected void fillParameters();

    /**
     * Use for labeling, etc
     *
     * @param className the name of the parameter
     */
    protected final void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the name of the parameter
     */
    public final String getClassName() {
        return className;
    }

    private void addParameterName(String name, DataType type) {
        Objects.requireNonNull(name, "The parameter name cannot be null");
        Objects.requireNonNull(type, "The data type cannot be null");
        if (dataTypes.containsKey(name)) {
            throw new IllegalArgumentException("The parameter " + name + " already exists.");
        }
        dataTypes.put(name, type);
    }

    /**
     * @param parameterName the name of the parameter, must not be null, must not already have been added
     * @param value         the value of the parameter
     */
    protected final void addDoubleParameter(String parameterName, Double value) {
        addParameterName(parameterName, DataType.DOUBLE);
        doubleParameters.put(parameterName, value);
    }

    /**
     * @param parameterName the name of the parameter, must not be null, must not already have been added
     * @param value         the value of the parameter
     */
    protected final void addIntegerParameter(String parameterName, Integer value) {
        addParameterName(parameterName, DataType.INTEGER);
        integerParameters.put(parameterName, value);
    }

    /**
     * @param parameterName the name of the parameter, must not be null, must not already have been added
     * @param value         the value of the parameter
     */
    protected final void addDoubleArrayParameter(String parameterName, double[] value) {
        addParameterName(parameterName, DataType.DOUBLE_ARRAY);
        doubleArrayParameters.put(parameterName, value);
    }

    /**
     * Checks if the supplied key is contained in the parameters
     *
     * @param name the name of the parameter
     * @return true if is has the named parameter
     */
    public final boolean containsParameter(String name) {
        return dataTypes.containsKey(name);
    }

    /**
     * Checks if name is null or if key is not defined as a parameter
     *
     * @param key name of the parameter
     */
    protected final void checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (!containsParameter(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a parameter value");
        }
    }

    /**
     * @return an unmodifiable Set view of the parameter names
     */
    public final Set<String> getParameterNames() {
        return Collections.unmodifiableSet(dataTypes.keySet());
    }

    /**
     * Can be used to determine which of the getXParameter(String key) methods to call
     *
     * @param name the name of the parameter
     * @return the Class type of the parameter
     */
    public final DataType getParameterDataType(String name) {
        return dataTypes.get(name);
    }

    /**
     * Gets the value associated with the supplied parameterName as a double.  If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs
     *
     * @param parameterName the name of the parameter
     * @return the value of the parameter
     */
    public double getDoubleParameter(String parameterName) {
        checkKey(parameterName);
        return doubleParameters.get(parameterName);
    }

    /**
     *  A convenience method to change the named parameter to the supplied value.
     *  This will work with either double or integer parameters.
     *  Integer parameters are coerced to the rounded up value of the supplied double,
     *  provided that the integer can hold the supplied value.  If the named
     *  parameter is not associated with the parameters, then no change occurs.
     *  In other words, the action fails, silently by returning false.
     *
     * @param parameterName the name of the parameter to change
     * @param value the value to change to
     * @return true if changed, false if no change occurred
     */
    public boolean changeParameter(String parameterName, double value){
        Objects.requireNonNull(parameterName, "The supplied parameter name was null");
        if (!containsParameter(parameterName)){
            return false;
        }
        // either double, integer or double array
        // try double first, then integer
        if (doubleParameters.containsKey(parameterName)){
            changeDoubleParameter(parameterName, value);
            return true;
        } else if (integerParameters.containsKey(parameterName)){
            int iValue = JSLMath.toIntValue(value);
            changeIntegerParameter(parameterName, iValue);
            return true;
        }
        // must be double[] array, cannot do the setting, just return false
        return false;
    }
    /**
     * Changes the value associated with the parameterName to the supplied value.  If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs
     *
     * @param parameterName parameterName with which the value is to be associated
     * @param value         the value to be associated with parameterName
     * @return the previous value that was associated with the parameterName
     */
    public double changeDoubleParameter(String parameterName, Double value) {
        checkKey(parameterName);
        return doubleParameters.put(parameterName, value);
    }

    /**
     * Gets the value associated with the supplied parameterName as a double{].  If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs
     *
     * @param parameterName the name of the parameter
     * @return a copy of the associated double[] is returned
     */
    public double[] getDoubleArrayParameter(String parameterName) {
        checkKey(parameterName);
        double[] value = doubleArrayParameters.get(parameterName);
        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return tmp;
    }

    /**
     * Returns the size (array length) of the DoubleArray parameter. If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs
     *
     * @param parameterName the name of the parameter
     * @return the size of the array
     */
    public int getDoubleArrayParameterSize(String parameterName) {
        checkKey(parameterName);
        return doubleArrayParameters.get(parameterName).length;
    }

    /**
     * Changes the value associated with the parameterName to the supplied value.  If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs.
     * <p>
     * The supplied array is copied.
     *
     * @param parameterName parameterName with which the double[] value is to be associated
     * @param value         the double[] value to be associated with parameterName, cannot be null, must be same size as original double[]
     * @return the previous double[] value that was associated with the parameterName
     */
    public double[] changeDoubleArrayParameter(String parameterName, double[] value) {
        checkKey(parameterName);
        if (value == null) {
            throw new IllegalArgumentException("The supplied array cannot be null");
        }
//        int size = this.getDoubleArrayParameterSize(parameterName);
//        if (size != value.length) {
//            throw new IllegalArgumentException("The supplied array is not the same size as the original double[]");
//        }

        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return doubleArrayParameters.put(parameterName, tmp);
    }

    /**
     * Gets the value associated with the supplied parameterName. If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs.
     *
     * @param parameterName the name of the parameter
     * @return the value of the parameter
     */
    public int getIntegerParameter(String parameterName) {
        checkKey(parameterName);
        return integerParameters.get(parameterName);
    }

    /**
     * Changes the value of the parameterName to the supplied value.  If the parameterName is null
     * or there is no parameter for the supplied parameterName, then an exception occurs.
     *
     * @param parameterName the name of the parameter
     * @param value         the value of the parameter
     * @return the previous value that was associated with the parameterName
     */
    public int changeIntegerParameter(String parameterName, int value) {
        checkKey(parameterName);
        return integerParameters.put(parameterName, value);
    }

    /**
     * @return an instance of the random variable based on the current parameter parameters,
     * with a new stream
     */
    public final RVariableIfc createRVariable() {
        return createRVariable(JSLRandom.nextRNStream());
    }

    /**
     * @param streamNumber a number representing the desired stream based on the RNStreamProvider
     * @return an instance of the random variable based on the current parameter parameters using the designated
     * stream number
     */
    public final RVariableIfc createRVariable(int streamNumber) {
        return createRVariable(JSLRandom.rnStream(streamNumber));
    }

    /**
     * Returns true if at least one double[] parameter has been set
     *
     * @return true if it has at least one double[] parameter
     */
    public boolean hasDoubleArrayParameter() {
        return (!doubleArrayParameters.isEmpty());
    }

    /**
     *
     * @return true if it has at least one double parameter
     */
    public boolean hasDoubleParameters(){
        return (!doubleParameters.isEmpty());
    }

    /**
     * Returns true if at least one Integer parameter has been set
     *
     * @return true if it has at least one integer parameter
     */
    public boolean hasIntegerParameter() {
        return (!integerParameters.isEmpty());
    }

    /**
     * Returns an unmodifiable Set of the parameter's keys
     * for Double Parameters
     *
     * @return the unmodifiable set
     */
    public Set<String> getDoubleParameterKeySet() {
        return Collections.unmodifiableSet(doubleParameters.keySet());
    }

    /**
     * Returns an unmodifiable Set of the parameter's keys
     * for double[] Parameters
     *
     * @return the unmodifiable set
     */
    public Set<String> getDoubleArrayParameterKeySet() {
        return Collections.unmodifiableSet(doubleArrayParameters.keySet());
    }

    /**
     * Returns an unmodifiable Set of the parameter's keys
     * for Integer Parameters
     *
     * @return the unmodifiable set
     */
    public Set<String> getIntegerParameterKeySet() {
        return Collections.unmodifiableSet(integerParameters.keySet());
    }

    /**
     * @param rnStream the stream to use
     * @return an instance of the random variable based on the current parameter parameters
     */
    abstract public RVariableIfc createRVariable(RNStreamIfc rnStream);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("RV Type = ").append(getType());
        sb.append(System.lineSeparator());

        sb.append("Double Parameters ");
        sb.append(doubleParameters.toString());
        sb.append(System.lineSeparator());

        sb.append("Integer Parameters ");
        sb.append(integerParameters.toString());
        sb.append(System.lineSeparator());

        sb.append("Double Array Parameters ");
        sb.append("{");
        for (String key : doubleArrayParameters.keySet()) {
            sb.append(System.lineSeparator());
            sb.append(key).append(" = ").append(Arrays.toString(doubleArrayParameters.get(key)));
        }
        sb.append("}");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     * Copies from the supplied parameters into this parameters
     *
     * @param rvParameters the parameters to copy from
     */
    public void copyFrom(RVParameters rvParameters) {
        Objects.requireNonNull(rvParameters, "The supplied RVParameters was null");
        if (this.type != rvParameters.type) {
            throw new IllegalArgumentException("Cannot copy into with different parameter types");
        }
        if (this.equals(rvParameters)) {
            return;
        }
        // not equal copy over, will have same keys
        doubleParameters.putAll(rvParameters.doubleParameters);
        integerParameters.putAll(rvParameters.integerParameters);
        for (Map.Entry<String, double[]> entry : rvParameters.doubleArrayParameters.entrySet()) {
            changeDoubleArrayParameter(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RVParameters)) return false;
        RVParameters that = (RVParameters) o;
        if (!className.equals(that.className)) return false;
        if ((type != that.type)) return false;
        if (!doubleParameters.equals(that.doubleParameters)) return false;
        if (!integerParameters.equals(that.integerParameters)) return false;
        if (!dataTypes.equals(that.dataTypes)) return false;
        // need to handle doubleArrayParameters differently because it contains double[]
        // must have the same keys
        if (!doubleArrayParameters.keySet().equals(that.doubleArrayParameters.keySet())) return false;
        // ok, same keys, now check the values for each key
        for (Map.Entry<String, double[]> entry : doubleArrayParameters.entrySet()) {
            String key = entry.getKey();
            double[] thisData = entry.getValue();
            double[] thatData = that.doubleArrayParameters.get(key);
            if (!Arrays.equals(thisData, thatData)) return false;
        }
        // all keys the same, all data the same, everything is the same
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<>();
        list.add(className);
        list.add(type);
        list.add(doubleParameters);
        list.add(integerParameters);
        list.add(dataTypes);
        for (Map.Entry<String, double[]> entry : doubleArrayParameters.entrySet()) {
            String key = entry.getKey();
            double[] thisData = entry.getValue();
            list.add(key);
            list.add(thisData);
        }
        Object[] objects = list.toArray();
        return Arrays.hashCode(objects);
    }

    public static void main(String[] args) {
        RVParameters p1 = RVType.Binomial.getRVParameters();
        RVParameters p2 = RVType.Normal.getRVParameters();
        RVParameters p3 = RVType.Triangular.getRVParameters();
        RVParameters p4 = RVType.Triangular.getRVParameters();

        System.out.println(p1.toJSON());
        System.out.println();

        System.out.println(p2.toJSON());
        System.out.println();

        System.out.println(p3.toJSON());
        System.out.println();

        System.out.println(p4.toJSON());
        System.out.println();

        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        if (p1.equals(p2)) {
            System.out.println("p1 == p2");
        } else {
            System.out.println("p1 != p2");
        }

        p3.changeDoubleParameter("min", -5.0);
        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        // not copy them over so that they are back to being the same
        p4.copyFrom(p3);
        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        System.out.println(p3.toJSON());
        System.out.println();

        System.out.println(p4.toJSON());
        System.out.println();
    }

    static class WeibullRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("shape", 1.0);
            addDoubleParameter("scale", 1.0);
            setClassName(RVType.Weibull.getParametrizedRVClass().getName());
            setRVType(RVType.Weibull);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double scale = getDoubleParameter("scale");
            double shape = getDoubleParameter("shape");
            return new WeibullRV(shape, scale, rnStream);
        }
    }

    static class UniformRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("min", 0.0);
            addDoubleParameter("max", 1.0);
            setClassName(RVType.Uniform.getParametrizedRVClass().getName());
            setRVType(RVType.Uniform);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double min = getDoubleParameter("min");
            double max = getDoubleParameter("max");
            return new UniformRV(min, max, rnStream);
        }
    }

    static class TriangularRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("min", 0.0);
            addDoubleParameter("mode", 0.5);
            addDoubleParameter("max", 1.0);
            setRVType(RVType.Triangular);
            setClassName(RVType.Triangular.getParametrizedRVClass().getName());
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mode = getDoubleParameter("mode");
            double min = getDoubleParameter("min");
            double max = getDoubleParameter("max");
            return new TriangularRV(min, mode, max, rnStream);
        }
    }

    static class ShiftedGeometricRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("probOfSuccess", 0.5);
            setClassName(RVType.ShiftedGeometric.getParametrizedRVClass().getName());
            setRVType(RVType.ShiftedGeometric);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("probOfSuccess");
            return new ShiftedGeometricRV(probOfSuccess, rnStream);
        }
    }

    static class PoissonRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("mean", 1.0);
            setClassName(RVType.Poisson.getParametrizedRVClass().getName());
            setRVType(RVType.Poisson);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mean = getDoubleParameter("mean");
            return new PoissonRV(mean, rnStream);
        }
    }

    static class PearsonType6RVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("alpha1", 2.0);
            addDoubleParameter("alpha2", 3.0);
            addDoubleParameter("beta", 1.0);
            setClassName(RVType.PearsonType6.getParametrizedRVClass().getName());
            setRVType(RVType.PearsonType6);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double alpha1 = getDoubleParameter("alpha1");
            double alpha2 = getDoubleParameter("alpha2");
            double beta = getDoubleParameter("beta");
            return new PearsonType6RV(alpha1, alpha2, beta, rnStream);
        }
    }

    static class PearsonType5RVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("shape", 1.0);
            addDoubleParameter("scale", 1.0);
            setClassName(RVType.PearsonType5.getParametrizedRVClass().getName());
            setRVType(RVType.PearsonType5);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double scale = getDoubleParameter("scale");
            double shape = getDoubleParameter("shape");
            return new PearsonType5RV(shape, scale, rnStream);
        }
    }

    static class NormalRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("mean", 0.0);
            addDoubleParameter("variance", 1.0);
            setClassName(RVType.Normal.getParametrizedRVClass().getName());
            setRVType(RVType.Normal);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mean = getDoubleParameter("mean");
            double variance = getDoubleParameter("variance");
            return new NormalRV(mean, variance, rnStream);
        }
    }

    static class NegativeBinomialRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("probOfSuccess", 0.5);
            addIntegerParameter("numSuccesses", 1);
            setClassName(RVType.NegativeBinomial.getParametrizedRVClass().getName());
            setRVType(RVType.NegativeBinomial);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("probOfSuccess");
            double numSuccesses = getDoubleParameter("numSuccesses");
            return new NegativeBinomialRV(probOfSuccess, numSuccesses, rnStream);
        }
    }

    static class LognormalRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("mean", 1.0);
            addDoubleParameter("variance", 1.0);
            setClassName(RVType.Lognormal.getParametrizedRVClass().getName());
            setRVType(RVType.Lognormal);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mean = getDoubleParameter("mean");
            double variance = getDoubleParameter("variance");
            return new LognormalRV(mean, variance, rnStream);
        }
    }

    static class LogLogisticRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("shape", 1.0);
            addDoubleParameter("scale", 1.0);
            setClassName(RVType.LogLogistic.getParametrizedRVClass().getName());
            setRVType(RVType.LogLogistic);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double scale = getDoubleParameter("scale");
            double shape = getDoubleParameter("shape");
            return new LogLogisticRV(shape, scale, rnStream);
        }
    }

    static class LaplaceRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("mean", 0.0);
            addDoubleParameter("scale", 1.0);
            setClassName(RVType.Laplace.getParametrizedRVClass().getName());
            setRVType(RVType.Laplace);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double scale = getDoubleParameter("scale");
            double mean = getDoubleParameter("mean");
            return new LaplaceRV(mean, scale, rnStream);
        }
    }

    static class JohnsonBRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("alpha1", 0.0);
            addDoubleParameter("alpha2", 1.0);
            addDoubleParameter("min", 0.0);
            addDoubleParameter("max", 1.0);
            setClassName(RVType.JohnsonB.getParametrizedRVClass().getName());
            setRVType(RVType.JohnsonB);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double alpha1 = getDoubleParameter("alpha1");
            double alpha2 = getDoubleParameter("alpha2");
            double min = getDoubleParameter("min");
            double max = getDoubleParameter("max");
            return new JohnsonBRV(alpha1, alpha2, min, max, rnStream);
        }
    }

    static class GeometricRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("probOfSuccess", 0.5);
            setClassName(RVType.Geometric.getParametrizedRVClass().getName());
            setRVType(RVType.Geometric);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("probOfSuccess");
            return new GeometricRV(probOfSuccess, rnStream);
        }
    }

    static class GeneralizedBetaRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("alpha1", 1.0);
            addDoubleParameter("alpha2", 1.0);
            addDoubleParameter("min", 0.0);
            addDoubleParameter("max", 1.0);
            setClassName(RVType.GeneralizedBeta.getParametrizedRVClass().getName());
            setRVType(RVType.GeneralizedBeta);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double alpha1 = getDoubleParameter("alpha1");
            double alpha2 = getDoubleParameter("alpha2");
            double min = getDoubleParameter("min");
            double max = getDoubleParameter("max");
            return new GeneralizedBetaRV(alpha1, alpha2, min, max, rnStream);
        }
    }

    static class GammaRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("shape", 1.0);
            addDoubleParameter("scale", 1.0);
            setClassName(RVType.Gamma.getParametrizedRVClass().getName());
            setRVType(RVType.Gamma);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double scale = getDoubleParameter("scale");
            double shape = getDoubleParameter("shape");
            return new GammaRV(shape, scale, rnStream);
        }
    }

    static class ExponentialRVParameters extends RVParameters {

        @Override
        protected void fillParameters() {
            addDoubleParameter("mean", 1.0);
            setClassName(RVType.Exponential.getParametrizedRVClass().getName());
            setRVType(RVType.Exponential);
        }

        @Override
        public RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mean = getDoubleParameter("mean");
            return new ExponentialRV(mean, rnStream);
        }
    }

    static class EmpiricalRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleArrayParameter("population", new double[1]);
            setClassName(RVType.Empirical.getParametrizedRVClass().getName());
            setRVType(RVType.Empirical);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double[] population = getDoubleArrayParameter("population");
            return new EmpiricalRV(population, rnStream);
        }
    }

    static class DUniformRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addIntegerParameter("min", 0);
            addIntegerParameter("max", 1);
            setClassName(RVType.DUniform.getParametrizedRVClass().getName());
            setRVType(RVType.DUniform);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            int min = getIntegerParameter("min");
            int max = getIntegerParameter("max");
            return new DUniformRV(min, max, rnStream);
        }
    }

    static class DEmpiricalRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleArrayParameter("values", new double[]{0.0, 1.0});
            addDoubleArrayParameter("cdf", new double[]{0.5, 1.0});
            setClassName(RVType.DEmpirical.getParametrizedRVClass().getName());
            setRVType(RVType.DEmpirical);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double[] values = getDoubleArrayParameter("values");
            double[] cdf = getDoubleArrayParameter("cdf");
            return new DEmpiricalRV(values, cdf, rnStream);
        }
    }

    static class ConstantRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("value", 1.0);
            setClassName(RVType.Constant.getParametrizedRVClass().getName());
            setRVType(RVType.Constant);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double value = getDoubleParameter("value");
            return new ConstantRV(value);
        }
    }

    static class ChiSquaredRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("dof", 1.0);
            setClassName(RVType.ChiSquared.getParametrizedRVClass().getName());
            setRVType(RVType.ChiSquared);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double dof = getDoubleParameter("dof");
            return new ChiSquaredRV(dof, rnStream);
        }
    }

    static class BinomialRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("probOfSuccess", 0.5);
            addIntegerParameter("numTrials", 2);
            setClassName(RVType.Binomial.getParametrizedRVClass().getName());
            setRVType(RVType.Binomial);
        }

        @Override
        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("probOfSuccess");
            int numTrials = getIntegerParameter("numTrials");
            return new BinomialRV(probOfSuccess, numTrials, rnStream);
        }
    }

    static class BetaRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("alpha1", 1.0);
            addDoubleParameter("alpha2", 1.0);
            setClassName(RVType.Beta.getParametrizedRVClass().getName());
            setRVType(RVType.Beta);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double alpha1 = getDoubleParameter("alpha1");
            double alpha2 = getDoubleParameter("alpha2");
            return new BetaRV(alpha1, alpha2, rnStream);
        }
    }

    static class BernoulliRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("probOfSuccess", 0.5);
            setClassName(RVType.Bernoulli.getParametrizedRVClass().getName());
            setRVType(RVType.Bernoulli);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("probOfSuccess");
            return new BernoulliRV(probOfSuccess, rnStream);
        }
    }

    static class AR1NormalRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("mean", 0.0);
            addDoubleParameter("variance", 1.0);
            addDoubleParameter("correlation", 0.0);
            setClassName(RVType.AR1Normal.getParametrizedRVClass().getName());
            setRVType(RVType.AR1Normal);
        }

        public final RVariableIfc createRVariable(RNStreamIfc rnStream) {
            double mean = getDoubleParameter("mean");
            double variance = getDoubleParameter("variance");
            double correlation = getDoubleParameter("variance");
            return new AR1NormalRV(mean, variance, correlation, rnStream);
        }
    }
}
