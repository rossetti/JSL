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
         *
         * @return the class associated with this type
         */
        public Class<?> asClass() {
            return clazz;
        }
    }
    /**
     *  To allow setting/tracking of name of parameter
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
     *  A map to keep track of parameter names and their types
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

    /** Used internally to set the type
     *
     * @param type the type
     */
    final void setRVType(RVType type){
        Objects.requireNonNull(type, "The supplied type was null");
        this.type = type;
    }

    abstract protected void fillParameters();

    /** Use for labeling, etc
     *
     * @param className the name of the parameter
     */
    protected final void setClassName(String className){
        this.className = className;
    }

    /**
     *
     * @return the name of the parameter
     */
    public final String getClassName(){
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
     * @param key   the name of the parameter, must not be null, must not already have been added
     * @param value the value of the parameter
     */
    protected final void addDoubleParameter(String key, Double value) {
        addParameterName(key, DataType.DOUBLE);
        doubleParameters.put(key, value);
    }

    /**
     * @param key   the name of the parameter, must not be null, must not already have been added
     * @param value the value of the parameter
     */
    protected final void addIntegerParameter(String key, Integer value) {
        addParameterName(key, DataType.INTEGER);
        integerParameters.put(key, value);
    }

    /**
     * @param key   the name of the parameter, must not be null, must not already have been added
     * @param value the value of the parameter
     */
    protected final void addDoubleArrayParameter(String key, double[] value) {
        addParameterName(key, DataType.DOUBLE_ARRAY);
        doubleArrayParameters.put(key, value);
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
     *
     * @return an unmodifiable Set view of the parameter names
     */
    public final Set<String> getParameterNames(){
        return Collections.unmodifiableSet(dataTypes.keySet());
    }

    /** Can be used to determine which of the getXParameter(String key) methods to call
     *
     * @param name the name of the parameter
     * @return the Class type of the parameter
     */
    public final DataType getParameterClass(String name){
        return dataTypes.get(name);
    }

    /**
     * Gets the value associated with the supplied key as a double.  If the key is null
     * or there is no parameter for the supplied key, then an exception occurs
     *
     * @param key the name of the parameter
     * @return the value of the parameter
     */
    public double getDoubleParameter(String key) {
        checkKey(key);
        return doubleParameters.get(key);
    }

    /**
     * Changes the value associated with the key to the supplied value.  If the key is null
     * or there is no parameter for the supplied key, then an exception occurs
     *
     * @param key   key with which the value is to be associated
     * @param value the value to be associated with key
     * @return the previous value that was associated with the key
     */
    public double changeDoubleParameter(String key, Double value) {
        checkKey(key);
        return doubleParameters.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key as a double{].  If the key is null
     * or there is no parameter for the supplied key, then an exception occurs
     *
     * @param key the name of the parameter
     * @return a copy of the associated double[] is returned
     */
    public double[] getDoubleArrayParameter(String key) {
        checkKey(key);
        double[] value = doubleArrayParameters.get(key);
        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return tmp;
    }

    /**
     * Returns the size (array length) of the DoubleArray parameter. If the key is null
     * or there is no parameter for the supplied key, then an exception occurs
     *
     * @param key the name of the parameter
     * @return the size of the array
     */
    public int getDoubleArrayParameterSize(String key) {
        checkKey(key);
        return doubleArrayParameters.get(key).length;
    }

    /**
     * Changes the value associated with the key to the supplied value.  If the key is null
     * or there is no parameter for the supplied key, then an exception occurs.
     * <p>
     * The supplied array is copied.
     *
     * @param key   key with which the double[] value is to be associated
     * @param value the double[] value to be associated with key, cannot be null, must be same size as original double[]
     * @return the previous double[] value that was associated with the key
     */
    public double[] changeDoubleArrayParameter(String key, double[] value) {
        checkKey(key);
        if (value == null) {
            throw new IllegalArgumentException("The supplied array cannot be null");
        }
        int size = this.getDoubleArrayParameterSize(key);
        if (size != value.length) {
            throw new IllegalArgumentException("The supplied array is not the same size as the original double[]");
        }

        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return doubleArrayParameters.put(key, tmp);
    }

    /**
     * Gets the value associated with the supplied key. If the key is null
     * or there is no parameter for the supplied key, then an exception occurs.
     *
     * @param key the name of the parameter
     * @return the value of the parameter
     */
    public int getIntegerParameter(String key) {
        checkKey(key);
        return integerParameters.get(key);
    }

    /**
     * Changes the value of the key to the supplied value.  If the key is null
     * or there is no parameter for the supplied key, then an exception occurs.
     *
     * @param key the name of the parameter
     * @param value the value of the parameter
     * @return the previous value that was associated with the key
     */
    public int changeIntegerParameter(String key, int value) {
        checkKey(key);
        return integerParameters.put(key, value);
    }

    /**
     * @return an instance of the random variable based on the current parameter parameters,
     * with a new stream
     */
    public final RVariableIfc makeRVariable() {
        return makeRVariable(JSLRandom.nextRNStream());
    }

    /**
     * @param streamNumber a number representing the desired stream based on the RNStreamProvider
     * @return an instance of the random variable based on the current parameter parameters using the designated
     * stream number
     */
    public final RVariableIfc makeRVariable(int streamNumber) {
        return makeRVariable(JSLRandom.rnStream(streamNumber));
    }

    /**
     * Returns true if at least one double[] parameter has been set
     *
     * @return true if is has at least one
     */
    public boolean hasDoubleArrayParameter() {
        return (!doubleArrayParameters.isEmpty());
    }

    /**
     * Returns true if at least one Integer parameter has been set
     *
     * @return true if is has at least one
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
    abstract public RVariableIfc makeRVariable(RNStreamIfc rnStream);

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

    public String toJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /** Copies from the supplied parameters into this parameters
     *
     * @param rvParameters the parameters to copy from
     */
    public void copyFrom(RVParameters rvParameters){
        Objects.requireNonNull(rvParameters, "The supplied RVParameters was null");
        if (this.type != rvParameters.type){
            throw new IllegalArgumentException("Cannot copy into with different parameter types");
        }
        if (this.equals(rvParameters)){
            return;
        }
        // not equal copy over, will have same keys
        doubleParameters.putAll(rvParameters.doubleParameters);
        integerParameters.putAll(rvParameters.integerParameters);
        for(Map.Entry<String, double[]> entry: rvParameters.doubleArrayParameters.entrySet()){
            double[] data = Arrays.copyOf(entry.getValue(),entry.getValue().length);
            doubleArrayParameters.put(entry.getKey(), data);
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
        for(Map.Entry<String, double[]> entry: doubleArrayParameters.entrySet()){
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
        for(Map.Entry<String, double[]> entry: doubleArrayParameters.entrySet()){
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
        RVParameters p2 =RVType.Normal.getRVParameters();
        RVParameters p3 =RVType.Triangular.getRVParameters();
        RVParameters p4 =RVType.Triangular.getRVParameters();

        System.out.println(p1.toJSON());
        System.out.println();

        System.out.println(p2.toJSON());
        System.out.println();

        System.out.println(p3.toJSON());
        System.out.println();

        System.out.println(p4.toJSON());
        System.out.println();

        if (p3.equals(p4)){
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()){
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        if (p1.equals(p2)){
            System.out.println("p1 == p2");
        } else {
            System.out.println("p1 != p2");
        }

        p3.changeDoubleParameter("min", -5.0);
        if (p3.equals(p4)){
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()){
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        // not copy them over so that they are back to being the same
        p4.copyFrom(p3);
        if (p3.equals(p4)){
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()){
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        System.out.println(p3.toJSON());
        System.out.println();

        System.out.println(p4.toJSON());
        System.out.println();
    }
}
