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
package jsl.utilities.controls;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jsl.utilities.reporting.JSONUtil;

/**
 * This class acts holds different types of Maps to allow named controls and
 * their associated values to be viewed and set. A named control
 * must be unique across all the different types. There should be
 * a default value provided for each named control.
 * <p>
 * Implementors of ControllableIfc are responsible for making
 * instances of this class that are filled appropriately by
 * implementing the fillControls() method.
 * <p>
 * The hasXControl() methods can be used to check if the
 * control data type has been defined.
 */
abstract public class Controls {

    /**
     *  To allow setting/tracking of name of control
     */
    private String myName;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a String
     */
    private final Map<String, String> myStringControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a Double
     */
    private final Map<String, Double> myDoubleControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as an Integer
     */
    private final Map<String, Integer> myIntegerControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a Float
     */
    private final Map<String, Float> myFloatControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a Long
     */
    private final Map<String, Long> myLongControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a Boolean
     */
    private final Map<String, Boolean> myBooleanControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a double[]
     */
    private final Map<String, double[]> myDoubleArrayControls;

    /**
     * The Map that hold the controls as pairs
     * key = name of control
     * value = value of the control as a ControllableIfc
     */
    private final Map<String, ControllableIfc> myControllableIfcControls;

    /**
     *  A map to keep track of control names and their types
     */
    private final Map<String, Class<?>> myControlTypes;

    /**
     *
     *
     */
    protected Controls() {
        myStringControls = new HashMap<>();
        myDoubleControls = new HashMap<>();
        myIntegerControls = new HashMap<>();
        myFloatControls = new HashMap<>();
        myLongControls = new HashMap<>();
        myBooleanControls = new HashMap<>();
        myDoubleArrayControls = new HashMap<>();
        myControllableIfcControls = new HashMap<>();
        myControlTypes = new HashMap<>();
        fillControls();
    }

    abstract protected void fillControls();

    /** Use for labeling, etc
     *
     * @param name the name of the control
     */
    protected final void setName(String name){
        myName = name;
    }

    /**
     *
     * @return the name of the control
     */
    public final String getName(){
        return myName;
    }

    private void addControlName(String name, Class<?> type) {
        Objects.requireNonNull(name, "The control name cannot be null");
        Objects.requireNonNull(type, "The class type cannot be null");
        if (myControlTypes.containsKey(name)) {
            throw new IllegalArgumentException("The control " + name + " already exists.");
        }
        myControlTypes.put(name, type);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addStringControl(String key, String value) {
        addControlName(key, String.class);
        myStringControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addDoubleControl(String key, double value) {
        addControlName(key, double.class);
        myDoubleControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addIntegerControl(String key, int value) {
        addControlName(key, int.class);
        myIntegerControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addFloatControl(String key, float value) {
        addControlName(key, float.class);
        myFloatControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addLongControl(String key, long value) {
        addControlName(key, long.class);
        myLongControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addDoubleArrayControl(String key, double[] value) {
        addControlName(key, double[].class);
        myDoubleArrayControls.put(key, value);
    }

    /**
     * @param key   the name of the control, must not be null, must not already have been added
     * @param value the value of the control
     */
    protected final void addControllableIfcControl(String key, ControllableIfc value) {
        addControlName(key, value.getClass());
        myControllableIfcControls.put(key, value);
    }

    /**
     * Checks if the supplied key is contained in the controls
     *
     * @param name the name of the control
     * @return true if is has the named control
     */
    public final boolean containsControl(String name) {
        return myControlTypes.containsKey(name);
    }

    /**
     * Checks if name is null or if key is not defined as a control
     *
     * @param key name of the control
     */
    protected final void checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (!containsControl(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a control value");
        }
    }

    /**
     *
     * @return an unmodifiable Set view of the control names
     */
    public final Set<String> getControlNames(){
        return Collections.unmodifiableSet(myControlTypes.keySet());
    }

    /** Can be used to determine which of the getXControl(String key) methods to call
     *
     * @param name the name of the control
     * @return the Class type of the control
     */
    public final Class<?> getControlClass(String name){
        return myControlTypes.get(name);
    }

    /**
     * Gets the value associated with the supplied key as a String.  If the key is null
     * or there is no control for the supplied key, then an exception occurs
     *
     * @param key the name of the control
     * @return the value
     */
    public String getStringControl(String key) {
        checkKey(key);
        return myStringControls.get(key);
    }

    /**
     * Changes the value associated with the key to the supplied value.  The key must already
     * exist in the controls and cannot be null.
     *
     * @param key   must not be null
     * @param value the new value associated with the key
     * @return the previous value that was associated with the key
     */
    public String changeStringControl(String key, String value) {
        checkKey(key);
        return myStringControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key as a double.  If the key is null
     * or there is no control for the supplied key, then an exception occurs
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public double getDoubleControl(String key) {
        checkKey(key);
        return myDoubleControls.get(key);
    }

    /**
     * Changes the value associated with the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs
     *
     * @param key   key with which the value is to be associated
     * @param value the value to be associated with key
     * @return the previous value that was associated with the key
     */
    public double changeDoubleControl(String key, double value) {
        checkKey(key);
        return myDoubleControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key as a double{].  If the key is null
     * or there is no control for the supplied key, then an exception occurs
     *
     * @param key the name of the control
     * @return a copy of the associated double[] is returned
     */
    public double[] getDoubleArrayControl(String key) {
        checkKey(key);
        double[] value = myDoubleArrayControls.get(key);
        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return tmp;
    }

    /**
     * Returns the size (array length) of the DoubleArray control. If the key is null
     * or there is no control for the supplied key, then an exception occurs
     *
     * @param key the name of the control
     * @return the size of the array
     */
    public int getDoubleArrayControlSize(String key) {
        checkKey(key);
        return myDoubleArrayControls.get(key).length;
    }

    /**
     * Changes the value associated with the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     * <p>
     * The supplied array is copied.
     *
     * @param key   key with which the double[] value is to be associated
     * @param value the double[] value to be associated with key, cannot be null, must be same size as original double[]
     * @return the previous double[] value that was associated with the key
     */
    public double[] changeDoubleArrayControl(String key, double[] value) {
        checkKey(key);
        if (value == null) {
            throw new IllegalArgumentException("The supplied array cannot be null");
        }
        int size = this.getDoubleArrayControlSize(key);
        if (size != value.length) {
            throw new IllegalArgumentException("The supplied array is not the same size as the original double[]");
        }

        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return myDoubleArrayControls.put(key, tmp);
    }

    /**
     * Gets the value associated with the supplied key. If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public int getIntegerControl(String key) {
        checkKey(key);
        return myIntegerControls.get(key);
    }

    /**
     * Changes the value of the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @param value the value of the control
     * @return the previous value that was associated with the key
     */
    public int changeIntegerControl(String key, int value) {
        checkKey(key);
        return myIntegerControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public long getLongControl(String key) {
        checkKey(key);
        return myLongControls.get(key);
    }

    /**
     * Changes the value of the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @param value the value of the control
     * @return the previous value that was associated with the key
     */
    public long changeLongControl(String key, long value) {
        checkKey(key);
        return myLongControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public float getFloatControl(String key) {
        checkKey(key);
        return myFloatControls.get(key);
    }

    /**
     * Changes the value of the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @param value the value of the control
     * @return the previous value that was associated with the key
     */
    public float changeFloatControl(String key, float value) {
        checkKey(key);
        return myFloatControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public boolean getBooleanControl(String key) {
        checkKey(key);
        return myBooleanControls.get(key);
    }

    /**
     * Sets the value of the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @param value the value of the control
     * @return the previous value that was associated with the key
     */
    public Boolean setBooleanControl(String key, boolean value) {
        checkKey(key);
        return myBooleanControls.put(key, value);
    }

    /**
     * Gets the value associated with the supplied key as a ControllableIfc.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key the name of the control
     * @return the value of the control
     */
    public ControllableIfc getControllableControl(String key) {
        checkKey(key);
        return myControllableIfcControls.get(key);
    }

    /**
     * Sets the value associated with the key to the supplied value.  If the key is null
     * or there is no control for the supplied key, then an exception occurs.
     *
     * @param key   key with which the string form of value is to be associated
     * @param value the value to be associated with key
     * @return the previous value that was associated with the key
     */
    public ControllableIfc setControllableControl(String key, ControllableIfc value) {
        checkKey(key);
        return myControllableIfcControls.put(key, value);
    }

    /**
     * Returns true if at least one String control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasStringControl() {
        return (!myStringControls.isEmpty());
    }

    /**
     * Returns true if at least one Double control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasDoubleControl() {
        return (!myDoubleControls.isEmpty());
    }

    /**
     * Returns true if at least one RandomIfc control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasControllableIfcControl() {
        return (!myControllableIfcControls.isEmpty());
    }

    /**
     * Returns true if at least one double[] control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasDoubleArrayControl() {
        return (!myDoubleArrayControls.isEmpty());
    }

    /**
     * Returns true if at least one Integer control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasIntegerControl() {
        return (!myIntegerControls.isEmpty());
    }

    /**
     * Returns true if at least one Long control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasLongControl() {
        return (!myLongControls.isEmpty());
    }

    /**
     * Returns true if at least one Boolean control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasBooleanControl() {
        return (!myBooleanControls.isEmpty());
    }

    /**
     * Returns true if at least one Float control has been set
     *
     * @return true if is has at least one
     */
    public boolean hasFloatControl() {
        return (!myFloatControls.isEmpty());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for String Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getStringControlKeySet() {
        return Collections.unmodifiableSet(myStringControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for Double Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getDoubleControlKeySet() {
        return Collections.unmodifiableSet(myDoubleControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for double[] Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getDoubleArrayControlKeySet() {
        return Collections.unmodifiableSet(myDoubleArrayControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for Integer Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getIntegerControlKeySet() {
        return Collections.unmodifiableSet(myIntegerControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for Long Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getLongControlKeySet() {
        return Collections.unmodifiableSet(myLongControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for Float Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getFloatControlKeySet() {
        return Collections.unmodifiableSet(myFloatControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for Boolean Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getBooleanControlKeySet() {
        return Collections.unmodifiableSet(myBooleanControls.keySet());
    }

    /**
     * Returns an unmodifiable Set of the control's keys
     * for RandomIfc Controls
     *
     * @return the unmodifiable set
     */
    public Set<String> getControllableIfcControlKeySet() {
        return Collections.unmodifiableSet(myControllableIfcControls.keySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("String Controls ");
        sb.append(myStringControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Double Controls ");
        sb.append(myDoubleControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Integer Controls ");
        sb.append(myIntegerControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Long Controls ");
        sb.append(myLongControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Boolean Controls ");
        sb.append(myBooleanControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Float Controls ");
        sb.append(myFloatControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Controllable Controls ");
        sb.append(myControllableIfcControls.toString());
        sb.append(System.lineSeparator());

        sb.append("Double Array Controls ");
        sb.append("{");
        for (String key : myDoubleArrayControls.keySet()) {
            sb.append(System.lineSeparator());
            sb.append(key).append(" = ").append(Arrays.toString(myDoubleArrayControls.get(key)));
        }
        sb.append("}");
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
