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
package jsl.modeling.elements.variable;

import java.io.File;
import java.nio.file.Path;

import jsl.simulation.ModelElement;
import jsl.observers.variable.*;
import jsl.utilities.JSLFileUtil;
import jsl.utilities.reporting.OutputDirectory;

/**
 *
 */
public class Variable extends Aggregatable implements VariableIfc {

    /**
     * Represents the lowest possible value allowed for this variable Attempts
     * to set the value of the variable to &lt; myLowerLimit will throw an
     * exception
     */
    protected double myLowerLimit = Double.NEGATIVE_INFINITY;

    /**
     * Represents the lowest possible value allowed for this variable Attempts
     * to set the value of the variable to &gt; myUpperLimit will throw an
     * exception
     */
    protected double myUpperLimit = Double.POSITIVE_INFINITY;

    /**
     * The value of the variable.
     */
    protected double myValue;

    /**
     * Holds the initial value of the variable.
     */
    protected double myInitialValue;

    /**
     * The previous value of the variable.
     */
    protected double myPrevValue;

    /**
     * The next value of the variable.
     */
    protected double myNextValue;

    /**
     * The time that the variable changed
     */
    protected double myTimeOfChange = 0.0;

    /**
     * The previous time that the variable changed
     */
    protected double myPrevTimeOfChange = 0.0;

    /**
     * The weight associated with the change
     */
    protected double myWeight = 1.0;

    /**
     * Holds a reference to an observer that will trace the variable's changes
     */
    protected VariableTraceTextReport myVariableTraceTextReport;

    /**
     * Indicates whether or not text file tracing is on/off
     */
    private boolean myTraceFlag = false;

    /**
     * Indicates whether the variable will notify update observers when its
     * value changes. If set to false the variable will not notify update
     * observers (until it is changed back to true). The default is true
     * <p>
     */
    private boolean mySetValueUpdateObserversFlag = true;

    /**
     * Creates a Variable with the given parent with initial value 0.0 over the
     * range [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     */
    public Variable(ModelElement parent) {
        this(parent, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a Variable with the given name and initial value over the range
     * [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     * @param initialValue The initial value of the variable.
     */
    public Variable(ModelElement parent, double initialValue) {
        this(parent, initialValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a Variable with the given name and initial value, 0.0, over the
     * range [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     * @param name The name of the variable.
     */
    public Variable(ModelElement parent, String name) {
        this(parent, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a Variable with the given name and initial value over the
     * supplied range The default range is [Double.NEGATIVE_INFINITY,
     * Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param name The name of the variable.
     */
    public Variable(ModelElement parent, double initialValue, String name) {
        this(parent, initialValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a Variable with the given name and initial value over the
     * supplied range [lowerLimit, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param name The name of the variable.
     */
    public Variable(ModelElement parent, double initialValue, double lowerLimit, String name) {
        this(parent, initialValue, lowerLimit, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a Variable with the initial value over the supplied range
     * [lowerLimit, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     */
    public Variable(ModelElement parent, double initialValue, double lowerLimit) {
        this(parent, initialValue, lowerLimit, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a Variable with the initial value over the supplied range
     * [lowerLimit, upperLimit]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param upperLimit the upper limit on the range for the variable
     */
    public Variable(ModelElement parent, double initialValue, double lowerLimit, double upperLimit) {
        this(parent, initialValue, lowerLimit, upperLimit, null);
    }

    /**
     * Creates a Variable with the given name and initial value over the
     * supplied range [lowerLimit, upperLimit]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable. Must be within the
     * range.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param upperLimit the upper limit on the range for the variable
     * @param name The name of the variable.
     */
    public Variable(ModelElement parent, double initialValue, double lowerLimit, double upperLimit, String name) {
        super(parent, name);
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("Invalid argument. lower limit must be < upper limit.");
        }

        myLowerLimit = lowerLimit;
        myUpperLimit = upperLimit;

        setInitialValue(initialValue);
        myValue = getInitialValue();
        myPrevValue = getInitialValue();
    }

    /**
     * Gets the initial value of the variable
     *
     * @return The initial value.
     */
    @Override
    public final double getInitialValue() {
        return (myInitialValue);
    }

    /**
     * Sets the initial value of the variable. Only relevant prior to each
     * replication. Changing during a replication has no effect until the next
     * replication.
     *
     * @param value The initial value for the variable.
     */
    @Override
    public final void setInitialValue(double value) {
        if ((value < myLowerLimit) || (value > myUpperLimit)) {
            throw new IllegalArgumentException("Invalid argument. supplied value was not in range, [" + myLowerLimit + "," + myUpperLimit + "]");
        }

        myInitialValue = value;
        this.getModel().isRunning();
    }

    /**
     * The lower limit for the range of this random variable
     *
     * @return Returns the lowerLimit.
     */
    public final double getLowerLimit() {
        return myLowerLimit;
    }

    /**
     * The upper limit for the range of this random variable
     *
     * @return Returns the upperLimit.
     */
    public final double getUpperLimit() {
        return myUpperLimit;
    }

    /**
     * Maps true to 1.0 and false to 0.0
     *
     * @param value true to 1.0 and false to 0.0
     */
    public final void setValue(boolean value) {
        if (value) {
            setValue(1.0);
        } else {
            setValue(0.0);
        }
    }

    /**
     * Sets the value of the variable and notifies any observers of the change
     *
     * @param value The observation of the variable.
     */
    @Override
    public void setValue(double value) {
//        System.out.println(getName() + " is being set to the value --> " + myValue);
        assignValue(value);
//        System.out.println(getName() + " is notifying its aggregates");
        notifyAggregatesOfValueChange();
//        System.out.println(getName() + " is notifying its update observers");
        if (mySetValueUpdateObserversFlag == true) {
            notifyUpdateObservers();
        }
//        System.out.println(getName() + " is done setting its value");
    }

    /**
     * Every Variable must implement the getValue method. By default this method
     * simply returns the value of the variable.
     *
     * @return The value of the variable.
     */
    public double getValue() {
        return (myValue);
    }

    /**
     * Gets the previous number value that was assigned before the current value
     *
     * @return a double representing that was assigned before the current value
     */
    @Override
    public final double getPreviousValue() {
        return (myPrevValue);
    }

    /**
     * Gets the weight associated with the last value observed.
     *
     * @return The weight for the value.
     */
    public final double getWeight() {
        return (myWeight);
    }

    /**
     * Sets the weight
     */
    protected void setWeight() {
        myWeight = 1.0;
    }

    /**
     * Gets the time associated with the last value observed.
     *
     * @return The time of the last observation
     */
    public final double getTimeOfChange() {
        return (myTimeOfChange);
    }

    /**
     * Gets the time associated with previous variable change.
     *
     * @return The time of the previous change.
     */
    public final double getPreviousTimeOfChange() {
        return (myPrevTimeOfChange);
    }

    /**
     * Turns on the automatic tracing of this variable to a text file with the
     * default name and no header
     */
    public final void turnOnTrace() {
        turnOnTrace(null, false);
    }

    /**
     * Turns on the automatic tracing of this variable to a text file
     *
     * @param header if true a header will be the first line of the file
     */
    public final void turnOnTrace(boolean header) {
        turnOnTrace(null, header);
    }

    /**
     * Turns on the automatic tracing of this variable to a text file
     *
     * @param fileName the file name
     */
    public final void turnOnTrace(String fileName) {
        turnOnTrace(fileName, false);
    }

    /**
     * Turns on the automatic tracing of this variable to a text file
     *
     * @param name Used to name the text file
     * @param header if true a header will be the first line of the file
     */
    public final void turnOnTrace(String name, boolean header) {
        setTraceObserver(name, header);
        // trace report has been made, add trace report to observers only if trace had been off
        if (myTraceFlag == false) {
            addObserver(myVariableTraceTextReport);
        }
        myTraceFlag = true;
    }

    /**
     * Turns off the automatic tracing of this variable to a text file.
     */
    public final void turnOffTrace() {
        // if trace had been on, remove the report
        if (myTraceFlag == true) {
            if (myVariableTraceTextReport != null) {
                deleteObserver(myVariableTraceTextReport);
            }
        }
        myTraceFlag = false;
    }

    /**
     * Assigns the value of the variable to the supplied value. Ensures that
     * time of change is current time and previous value and previous time of
     * change are the same as the current value and current time without
     * notifying any update observers
     *
     * @param value the initial value to assign
     */
    protected void assignInitialValue(double value) {
//        System.out.println(getName() + " is assigning the initial value = " + value);
        myValue = value;
//        myTimeOfChange = getTime();
        myTimeOfChange = 0.0; //TODO
        myPrevValue = myValue;
        myPrevTimeOfChange = myTimeOfChange;
//        System.out.println(getName() + " was assigned the value --> " + myValue + " in assignInitialValue()");
    }

    /**
     * Properly assigns the value of the variable and remembers previous value
     * without notifying any update observers
     *
     * @param value the value to assign
     */
    protected final void assignValue(double value) {
        if ((value < myLowerLimit) || (value > myUpperLimit)) {
            throw new IllegalArgumentException("Invalid argument. supplied value was not in range, [" + myLowerLimit + "," + myUpperLimit + "]");
        }
        // record the new weight
        setWeight();
        // remember the old values
        myPrevValue = myValue;
        myPrevTimeOfChange = myTimeOfChange;
        // record the new value and time
        myValue = value;
        myTimeOfChange = getTime();
        // System.out.println(getName() + " was assigned the value --> " + myValue);
    }

    /**
     * Can be overridden by subclasses to set the protected variable
     * myVariableTraceTextReport for appropriate text tracing
     *
     * @param name the file name of the report
     * @param header the header, true means include the header
     */
    protected void setTraceObserver(String name, boolean header) {
        if (myVariableTraceTextReport == null) {
            if (name == null) {
                name = getName();
            }
            String theFixedName = name.replaceAll(":", "_");
            if (getSimulation() != null) {
                OutputDirectory outputDirectory = getSimulation().getOutputDirectory();
                Path pathToFile = outputDirectory.getOutDir().resolve(theFixedName + "_Trace.csv");
                myVariableTraceTextReport = new VariableTraceTextReport(pathToFile, header);
            } else {
                Path pathToFile = JSLFileUtil.getProgramLaunchDirectory().resolve(theFixedName + "_Trace.csv");
                myVariableTraceTextReport = new VariableTraceTextReport(pathToFile, header);
            }
        }
    }

    @Override
    protected void beforeExperiment() {
        // always assign the value of the variable to
        // the provided initial value prior to the start
        // of an experiment (the first of any replications)
        assignInitialValue(getInitialValue());
    }

    @Override
    protected void initialize() {
//		System.out.println("In Variable initialize() " + getName());
        // initialize may or may not get called depending on whether
        // the variable participates, if it does make sure that
        // every replication starts with the same initial value
        assignInitialValue(getInitialValue());
        //if there are aggregates let them know about the initialization
        notifyAggregatesOfInitialization();
    }

    @Override
    protected void warmUp() {
        //if there are aggregates let them know about the warmUp
        notifyAggregatesOfWarmUp();
    }

    @Override
    protected void removedFromModel() {
        super.removedFromModel();
        myVariableTraceTextReport = null;
    }

    /**
     * Gets the value of the flag that indicates whether or not observers will
     * be notified when the value changes.
     *
     * @return true means will be notified
     */
    public final boolean getSetValueUpdateNotificationFlag() {
        return mySetValueUpdateObserversFlag;
    }

    /**
     * Turns off or on the notification of update observers for changes to the
     * value of the variable. For example, this is useful to save the execution
     * time associated with the statistical collection, etc on the variable Once
     * it is off, it remains off until turned on. Thus multiple executions of
     * the same model will remember this setting
     *
     * @param flag true means on
     *
     */
    public final void setValueUpdateNotificationFlag(boolean flag) {
        mySetValueUpdateObserversFlag = flag;
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append(toString());
        sb.append("\n");
        sb.append("Time = ");
        sb.append(getTime());
        sb.append("\n");
        sb.append("Previous time = ");
        sb.append(getPreviousTimeOfChange());
        sb.append("\t");
        sb.append("Previous value = ");
        sb.append(getPreviousValue());
        sb.append("\n");
        sb.append("Current time = ");
        sb.append(getTimeOfChange());
        sb.append("\t");
        sb.append("Current value = ");
        sb.append(getValue());
        sb.append("\n");
        return sb.toString();
    }

}
