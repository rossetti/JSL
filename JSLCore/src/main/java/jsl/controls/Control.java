package jsl.controls;

import jsl.utilities.GetNameIfc;
import jsl.utilities.math.JSLMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static jsl.controls.ControlType.*;

public class Control<T> {

    /**
     * for logging
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final GetNameIfc element;

    protected final Method method;

    protected final String setterName;

    protected T lastValue = null;

    /**
     * Need to know the generic class type at run-time. This is the easiest way
     */
    private final Class<T> type;

    private final JSLControl jslControl;

    /**
     * @param type    this is the type of T, e.g. if T is Double, then use Double.class. This allows
     *                the generic type to be easily determined at run-time, must not be null
     * @param element the element that has the method to control, must not be null
     * @param method  the method that will be used by the control, must not be null
     */
    public Control(Class<T> type, GetNameIfc element, Method method) {
        Objects.requireNonNull(type, "The supplied class type cannot be null");
        Objects.requireNonNull(element, "The invoking model element cannot be null");
        Objects.requireNonNull(method, "The method cannot be null");
        if (!isSingleParameter(method)) {
            LOGGER.error("Method {} for class {} was specified as a control, but is not a single parameter method!",
                    method.getName(), method.getDeclaringClass().getName());
            throw new IllegalArgumentException("Method specified for control but was not single parameter method");
        }
        if (!hasControlAnnotation(method)) {
            LOGGER.error("Method {} for class {} was specified as a control, but does not have any control annotations",
                    method.getName(), method.getDeclaringClass().getName());
            throw new IllegalArgumentException("There was no control annotation on the supplied method");
        }
        if (!classTypesToValidTypesMap.containsKey(type)) {
            LOGGER.error("Type {} for the control is not a valid control type", type.getName());
            throw new IllegalArgumentException("Invalid control type!");
        }
        Class<?> pType = method.getParameterTypes()[0];
        // type could be a primitive, which means it will not be assignable from any of the wrapper
        // classes. Thus, it must be wrapped if it is a primitive to its wrapper class to be tested
        if (pType.isPrimitive()) {
            LOGGER.info("Method {} for class {} was specified as a control, but with primitive type {} " +
                            "and was wrapped for matching to control type {}",
                    method.getName(), method.getDeclaringClass().getName(), pType, type.getName());
        }
        pType = wrap(pType);
        if (!pType.isAssignableFrom(type)) {
            LOGGER.error("Method {} for class {} was specified as a control, but its parameter type {} " +
                            "is not compatible with the control type {}",
                    method.getName(), method.getDeclaringClass().getName(), pType, type.getName());
            String msg = "Method parameter type " + pType + " is not compatible with the control type "
                    + type.getName() + "!\n";
            throw new IllegalArgumentException(msg);
        }
        jslControl = getControlAnnotation(method);
        // okay, method parameter type is same as control type
        // need to check if annotation has the correct type
        if (!type.isAssignableFrom(jslControl.type().asClass())) {
            LOGGER.error("Annotation Type {} is not compatible with the control type {}",
                    jslControl.type(), type.getName());
            throw new IllegalArgumentException("Annotation type does not match control type!");
        }
        this.type = type;
        this.element = element;
        this.method = method;
        setterName = makeSetterName(method.getName(), jslControl.name());
        LOGGER.info("Constructed control : {} for method: {} on class {}",
                this, method.getName(), element.getName());
    }

    public ControlType getAnnotationType() {
        return jslControl.type();
    }

    public String getAnnotationName() {
        return jslControl.name();
    }

    public double getLowerBound() {
        return coerceValue(jslControl.type(), jslControl.lowerBound());
    }

    public double getUpperBound() {
        return coerceValue(jslControl.type(), jslControl.upperBound());
    }

    public String getComment() {
        return jslControl.comment();
    }

    /**
     * generate a compound key of element name and setter name delimited by '.'
     * (escape any periods in the component parts to ensure it stays unique)
     *
     * @return the key
     */
    public final String getKey() {
        String en = this.element.getName().replace(".", "\\.");
        String sn = this.setterName.replace(".", "\\.");
        return en + "." + sn;
    }

    /**
     * Use this method to determine the generic type at run-time
     *
     * @return the type
     */
    public final Class<T> getType() {
        return this.type;
    }


    /**
     * Takes a double value and translates it to a valid value for the type of control.
     * This may involve a conversion to numeric types that have narrower numeric
     * representations as per the Java Language specification.
     * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.3">...</a>
     * The conversion varies from standard java by rounding up integral values. For example,
     * 4.99999 will be rounded to 5.0 rather than the standard truncation to 4.0.
     * If the double value is outside the range of the numeric type, then it is coerced to the
     * smallest or largest permissible value for the numeric type.  If the double value is
     * outside of upper and lower limits as specified by the control annotation, then the value
     * is coerced to the nearest limit. This method will call assignValue(T) with the appropriate
     * type for the control.  If the control type is Boolean, a 1.0 is coerced to true and any double not
     * equal to 1.0 is coerced to false.
     *
     * @param value the value to set
     */
    public void setValue(double value) {
        // the incoming value comes in as a double and must be converted to the range of the control type
        double x = coerceValue(jslControl.type(), value);
        // coerced value is within domain for the control type
        // now ensure value is within limits for control
        x = limitToRange(x);
        // x is now valid for type and limited to control range and can be converted to appropriate type
        // before assigning it
        T v = coerce(x);
        // finally make the assignment
        assignValue(v);
    }

    /**
     * Subclasses may need ot override this method if attempting to handle additional valid
     * control types.
     *
     * @param value the value to coerce to the type of the control
     * @return the coerced value
     */
    protected T coerce(double value) {
        // These should all be safe casts
        switch (getAnnotationType()) {
            case DOUBLE:
                return type.cast(value);
            case INTEGER:
                return type.cast(JSLMath.toIntValue(value));
            case LONG:
                return type.cast(JSLMath.toLongValue(value));
            case FLOAT:
                return type.cast(JSLMath.toFloatValue(value));
            case SHORT:
                return type.cast(JSLMath.toShortValue(value));
            case BYTE:
                return type.cast(JSLMath.toByteValue(value));
            case BOOLEAN:
                return type.cast(JSLMath.toBooleanValue(value));
            default:
                LOGGER.error("The value {} could not be coerced to type {}. No available control type match!", value, type);
                throw new IllegalStateException("Unable to coerce value to type of control. See logs for details");
        }
    }

    /**
     * Allows the direct assignment of a value of type T to the control, and thus
     * to the element that was annotated by the control. This allows for the possibility of
     * non-numeric types to be added in the future.  Use setValue(double) for any numeric types
     * as well as boolean values.
     *
     * @param value the value to assign
     */
    public void assignValue(T value) {
        try {
            method.invoke(element, value);
            // record the value last set
            // rather than try to read it from a getter on demand
            lastValue = value;
            LOGGER.info("Control {} was assigned value {}", getKey(), lastValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Unsuccessful assign for Control {} with value {}", getKey(), lastValue);
            throw new RuntimeException(e);
        }
    }

    /**
     * Ensures that the supplied double is within the bounds
     * associated with the control annotation. This method does
     * not change the state of the control.
     *
     * @param value the value to limit
     * @return the limited value for future use
     */
    public double limitToRange(double value) {
        if (value <= getLowerBound()) {
            return getLowerBound();
        } else if (value >= getUpperBound()) {
            return getUpperBound();
        }
        return value;
    }

    /**
     * return the value most recently assigned
     *
     * @return the value most recently assigned
     */
    public final T getLastValue() {
        return lastValue;
    }

    /**
     * Checks if the type of control can be converted to a double
     *
     * @return true if control can be converted to a double
     */
    public boolean isDoubleCompatible() {
        switch (getAnnotationType()) {
            case DOUBLE:
            case INTEGER:
            case LONG:
            case FLOAT:
            case SHORT:
            case BYTE:
            case BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the last value of the control as a double. Use isDoubleCompatible() for
     * save call
     *
     * @return the last value of the control converted to a double
     */
    public Double getLastValueAsDouble() {
        switch (getAnnotationType()) {
            case DOUBLE:
            case INTEGER:
            case LONG:
            case FLOAT:
            case SHORT:
            case BYTE:
                return (Double) getLastValue();
            case BOOLEAN:
                if (getLastValue() == null){
                    return null;
                }
                boolean b = (boolean) getLastValue();
                if (b) {
                    return 1.0;
                } else return 0.0;
            default:
                LOGGER.error("Attempted to transform control type {} for method {} of element {} to double",
                        type, method, element);
                throw new IllegalStateException("Attempted to transform invalid control type to double");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[key = ").append(getKey());
        str.append(", control type = ").append(getAnnotationType());
        str.append(", value = ").append(getLastValue() == null ? "[null]" : getLastValue());
        str.append(", lower bound = ").append(getLowerBound());
        str.append(", upper bound = ").append(getUpperBound());
        str.append(", comment = ").append(getComment() == null ? "[null]" : getComment());
        str.append("]");
        return str.toString();
    }

    /**
     * class to define (and populate) a more detailed control record.
     *
     * @return the annotation control map
     */
    public final ControlRecord getControlRecord() {
        return new ControlRecord(this);
    }


    /**
     * Wrap a class if it is a primitive.
     * <a href="https://stackoverflow.com/questions/1704634/simple-way-to-get-wrapper-class-type-in-java">...</a>
     *
     * @param c   the class to wrap
     * @param <T> the type of the method
     * @return the wrapped class or the class itself
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrap(Class<T> c) {
        return (Class<T>) MethodType.methodType(c).wrap().returnType();
    }

    /**
     * Unwrap a class if it is a primitive.
     * <a href="https://stackoverflow.com/questions/1704634/simple-way-to-get-wrapper-class-type-in-java">...</a>
     *
     * @param c   the class to wrap
     * @param <T> the type of the method
     * @return the wrapped class or the class itself
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unwrap(Class<T> c) {
        return (Class<T>) MethodType.methodType(c).unwrap().returnType();
    }

    /**
     * @param method the method to examine
     * @return null if no JSLControl annotation was provided for the method
     */
    public static JSLControl getControlAnnotation(Method method) {
        Objects.requireNonNull(method, "The method cannot be null");
        Annotation ann = method.getAnnotation(JSLControl.class);
        return castToJSLControl(ann);
    }

    /**
     * @param method the method to check
     * @return true if the parameter type of the method is a valid control type
     */
    public static boolean validateMethodType(Method method) {
        Objects.requireNonNull(method, "The method cannot be null");
        Class<?> pType = method.getParameterTypes()[0];
        return classTypesToValidTypesMap.containsKey(pType);
    }

    /**
     * @param method the method to check
     * @return true if it has a JSLControl annotation
     */
    public static boolean hasControlAnnotation(Method method) {
        Objects.requireNonNull(method, "The method cannot be null");
        Annotation ann = method.getAnnotation(JSLControl.class);
        return ann != null;
    }

    /**
     * Check whether the method is a valid single parameter method
     *
     * @param method the method
     * @return true if valid single parameter method
     */
    public static boolean isSingleParameter(Method method) {
        Objects.requireNonNull(method, "The method cannot be null");
        if (method.getParameterCount() != 1) {
            LOGGER.info("Method {} for class {} is not a single parameter method",
                    method.getName(), method.getDeclaringClass().getName());
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            LOGGER.info("Method {} for class {} was specified as a control, but is not a public method",
                    method.getName(), method.getDeclaringClass().getName());
            return false;
        }
        LOGGER.info("{} for class {} is a valid single parameter control method",
                method.getName(), method.getDeclaringClass().getName());
        return true;
    }

    /**
     * @param annotation the annotation to check
     * @return true if the annotation is a valid control annotation
     */
    public static boolean isJSLControlAnnotation(Annotation annotation) {
        if (annotation == null) {
            return false;
        }
        return annotation instanceof JSLControl;
    }

    /**
     * @param annotation the annotation to cast
     * @return null or the annotation as a JSLControl
     */
    public static JSLControl castToJSLControl(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        if (annotation instanceof JSLControl) {
            return (JSLControl) annotation;
        } else {
            return null;
        }
    }

    /**
     * Derive a setter name from method and annotation names
     *
     * @param methodName     the method name
     * @param annotationName the name from the control annotation
     * @return the string name
     */
    public static String makeSetterName(String methodName, String annotationName) {
        if (annotationName == null) annotationName = "";
        if (annotationName.equals("")) {
            annotationName = methodName;
            if (annotationName.startsWith("set")) {
                annotationName = annotationName.substring(3);
            }
            annotationName = annotationName.substring(0, 1).toLowerCase()
                    + annotationName.substring(1);
        }
        return annotationName;
    }

    /**
     * @param controlType the control type
     * @param value       a double value
     * @return the value coerced to be appropriate for the type, or Double.NaN
     */
    public static double coerceValue(ControlType controlType, double value) {
        Objects.requireNonNull(controlType, "The supplied type was null");
        switch (controlType) {
            case DOUBLE:
                return value;
            case INTEGER:
                return JSLMath.toIntValue(value);
            case LONG:
                return JSLMath.toLongValue(value);
            case FLOAT:
                return JSLMath.toFloatValue(value);
            case SHORT:
                return JSLMath.toShortValue(value);
            case BYTE:
                return JSLMath.toByteValue(value);
            case BOOLEAN:
                if (JSLMath.toBooleanValue(value)) {
                    return 1.0;
                } else {
                    return 0.0;
                }
            default:
                LOGGER.warn("The value {} could not be coerced to a double and was set to Double.NaN. No available control type match!", value);
                return Double.NaN;
        }
    }

}
