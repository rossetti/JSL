package jsl.controls.work;

import jsl.utilities.GetNameIfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static jsl.controls.work.Control.ControlType.*;

public class Control<T> {

    /**
     * Defines the set of valid control types
     */
    public enum ControlType {
        DOUBLE, INTEGER, LONG, FLOAT, SHORT, BYTE, BOOLEAN;

        public static final EnumSet<ControlType> CONTROL_TYPE_SET = EnumSet.of(ControlType.DOUBLE, ControlType.INTEGER,
                ControlType.LONG, ControlType.FLOAT, ControlType.SHORT, ControlType.BYTE, ControlType.BOOLEAN);

        public static final EnumMap<ControlType, Class<?>> validTypesToClassMap = new EnumMap<>(ControlType.class);

        public static final Map<Class<?>, ControlType> classTypesToValidTypesMap = new HashMap<>();

        static {
            validTypesToClassMap.put(ControlType.DOUBLE, Double.class);
            validTypesToClassMap.put(ControlType.INTEGER, Integer.class);
            validTypesToClassMap.put(ControlType.LONG, Long.class);
            validTypesToClassMap.put(ControlType.FLOAT, Float.class);
            validTypesToClassMap.put(ControlType.SHORT, Short.class);
            validTypesToClassMap.put(ControlType.BYTE, Byte.class);
            validTypesToClassMap.put(ControlType.BOOLEAN, Boolean.class);

            classTypesToValidTypesMap.put(Double.class, ControlType.DOUBLE);
            classTypesToValidTypesMap.put(Integer.class, ControlType.INTEGER);
            classTypesToValidTypesMap.put(Long.class, ControlType.LONG);
            classTypesToValidTypesMap.put(Float.class, ControlType.FLOAT);
            classTypesToValidTypesMap.put(Short.class, ControlType.SHORT);
            classTypesToValidTypesMap.put(Byte.class, ControlType.BYTE);
            classTypesToValidTypesMap.put(Boolean.class, ControlType.BOOLEAN);
        }
    }

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
        if (!pType.isInstance(type)) {
            LOGGER.error("Method {} for class {} was specified as a control, but its parameter type is not compatible with the control type {}",
                    method.getName(), method.getDeclaringClass().getName(), type.getName());
            throw new IllegalArgumentException("Method parameter type is not compatible with the control type!");
        }
        jslControl = getControlAnnotation(method);
        // okay, method parameter type is same as control type
        // need to check if annotation has the correct type
        if (!type.isInstance(validTypesToClassMap.get(jslControl.type()))) {
            LOGGER.error("Annotation Type {} is not compatible with the control type {}",
                    jslControl.type(), type.getName());
            throw new IllegalArgumentException("Annotation type does not match control type!");
        }
        this.type = type;
        this.element = element;
        this.method = method;
        setterName = makeSetterName(method.getName(), jslControl.name());
        LOGGER.info("Constructed control type: {} for method: {} on class {}",
                jslControl.annotationType(), method.getName(), element.getName());
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


    /** Takes a double value and translates it to a valid value for the type of control.
     *  This may involve a conversion to numeric types that have narrower numeric
     *  representations as per the Java Language specification.
     *  <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.3">...</a>
     *  The conversion varies from standard java by rounding up integral values. For example,
     *  4.99999 will be rounded to 5.0 rather than the standard truncation to 4.0.
     *  If the double value is outside the range of the numeric type, then it is coerced to the
     *  smallest or largest permissible value for the numeric type.  If the double value is
     *  outside of upper and lower limits as specified by the control annotation, then the value
     *  is coerced to the nearest limit. This method will call assignValue(T) with the appropriate
     *  type for the control.  If the control type is Boolean, a 1.0 is coerced to true and any double not
     *  equal to 1.0 is coerced to false.
     *
     * @param value the value to set
     */
    public void setValue(double value) {
        // the incoming value comes in as a double and must be converted to range of type
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

    /** Subclasses may need ot override this method if attempting to handle addition valid
     *  control types.
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
                return type.cast(toIntValue(value));
            case LONG:
                return type.cast(toLongValue(value));
            case FLOAT:
                return type.cast(toFloatValue(value));
            case SHORT:
                return type.cast(toShortValue(value));
            case BYTE:
                return type.cast(toByteValue(value));
            case BOOLEAN:
                return type.cast(toBooleanValue(value));
            default:
                LOGGER.error("The value {} could not be coerced to type {}. No available control type match!", value, type);
               throw new IllegalStateException("Unable to coerce value to type of control. See logs for details");
        }
    }

    /** Allows the direct assignment of a value of type T to the control, and thus
     *  to the element that was annotated by the control. This allows for the possibility of
     *  non-numeric types to be added in the future.  Use setValue(double) for any numeric types
     *  as well as boolean values.
     *
     * @param value the value to assign
     */
    public void assignValue(T value) {
        try {
            //TODO need to work on checking valid set for number types
            method.invoke(element, value);
            // record the value last set
            // rather than try to read it from a getter on demand
            lastValue = value;
        } catch (IllegalAccessException | InvocationTargetException e) {
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

    /** Checks if the type of control can be converted to a double
     *
     * @return true if control can be converted to a double
     */
    public boolean isDoubleCompatible(){
        switch (getAnnotationType() ) {
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

    /** Gets the last value of the control as a double. Use isDoubleCompatible() for
     *  save call
     *
     * @return   the last value of the control converted to a double
     */
    public Double getLastValueAsDouble(){
        switch (getAnnotationType() ) {
            case DOUBLE:
            case INTEGER:
            case LONG:
            case FLOAT:
            case SHORT:
            case BYTE:
                return (Double) getLastValue();
            case BOOLEAN:
                boolean b = (boolean) getLastValue();
                if (b){
                    return 1.0;
                } else return 0.0;
            default:
                LOGGER.error("Attempted to transform control type {} for method {} of element {} to double",
                        type, method, element);
                throw new IllegalStateException("Attempted to transform invalid control type to double");
        }
    }

    /**
     * class to define (and populate) a more detailed control record.
     *
     * @return the annotation control map
     */
    public final ControlRecord getControlRecord() {
        return new ControlRecord(this);
    }

    //TODO consider static <T> Control<T> create(Class<T> clazz, ...
    public static Control<?> create(ControlType controlType, GetNameIfc element, Method method) {
        Objects.requireNonNull(controlType, "The supplied class type cannot be null");
        Objects.requireNonNull(element, "The invoking model element cannot be null");
        Objects.requireNonNull(method, "The method cannot be null");
        switch (controlType) {
            case DOUBLE:
                return new Control<Double>(Double.class, element, method);
            case INTEGER:
                return new Control<Integer>(Integer.class, element, method);
            case LONG:
                return new Control<Long>(Long.class, element, method);
            case FLOAT:
                return new Control<Float>(Float.class, element, method);
            case SHORT:
                return new Control<Short>(Short.class, element, method);
            case BYTE:
                return new Control<Byte>(Byte.class, element, method);
            case BOOLEAN:
                return new Control<Boolean>(Boolean.class, element, method);
            default:
                LOGGER.error("Attempted to create a non-existing control type {} for method {} of element {}",
                        controlType, method, element);
                throw new IllegalStateException("Attempted to create a non-existing control type");
        }
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
     * @param controlType  the control type
     * @param value a double value
     * @return the value coerced to be appropriate for the type, or Double.NaN
     */
    public static double coerceValue(ControlType controlType, double value) {
        Objects.requireNonNull(controlType, "The supplied type was null");
        switch (controlType) {
            case DOUBLE:
                return value;
            case INTEGER:
                return toIntValue(value);
            case LONG:
                return toLongValue(value);
            case FLOAT:
                return toFloatValue(value);
            case SHORT:
                return toShortValue(value);
            case BYTE:
                return toByteValue(value);
            case BOOLEAN:
                if (toBooleanValue(value)) {
                    return 1.0;
                } else {
                    return 0.0;
                }
            default:
                LOGGER.warn("The value {} could not be coerced to a double and was set to Double.NaN. No available control type match!", value);
                return Double.NaN;
        }
    }

    /**
     * Converts a double to a byte. If the double is outside
     * the natural range, then the value is set to the minimum or
     * maximum of the range. If within the range, the value
     * is rounded to the nearest value. For example, 4.9999 is
     * rounded to 5.0.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Byte toByteValue(double value) {
        if (value >= Byte.MAX_VALUE) {
            LOGGER.info("{} was limited to {} in toByteValue()", value, Byte.MAX_VALUE);
            return Byte.MAX_VALUE;
        } else if (value <= Byte.MIN_VALUE) {
            LOGGER.info("{} was limited to {} in toByteValue()", value, Byte.MIN_VALUE);
            return Byte.MIN_VALUE;
        } else {
            // in the range of byte, convert to the nearest byte
            return (byte) Math.round(value);
        }
    }

    /**
     * Converts a double to a long. If the double is outside
     * the natural range, then the value is set to the minimum or
     * maximum of the range. If within the range, the value
     * is rounded to the nearest value. For example, 4.9999 is
     * rounded to 5.0.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Long toLongValue(double value) {
        if (value >= Long.MAX_VALUE) {
            LOGGER.info("{} was limited to {} in toLongValue()", value, Long.MAX_VALUE);
            return Long.MAX_VALUE;
        } else if (value <= Long.MIN_VALUE) {
            LOGGER.info("{} was limited to {} in toLongValue()", value, Long.MIN_VALUE);
            return Long.MIN_VALUE;
        } else {
            // in the range of long, convert to the nearest long
            return Math.round(value);
        }
    }

    /**
     * Converts a double to an int. If the double is outside
     * the natural range, then the value is set to the minimum or
     * maximum of the range. If within the range, the value
     * is rounded to the nearest value. For example, 4.9999 is
     * rounded to 5.0.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Integer toIntValue(double value) {
        if (value >= Integer.MAX_VALUE) {
            LOGGER.info("{} was limited to {} in toIntValue()", value, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        } else if (value <= Integer.MIN_VALUE) {
            LOGGER.info("{} was limited to {} in toIntValue()", value, Integer.MIN_VALUE);
            return Integer.MIN_VALUE;
        } else {
            // in the range of int, convert to the nearest int
            return (int) Math.round(value);
        }
    }

    /**
     * Converts a double to a short. If the double is outside
     * the natural range, then the value is set to the minimum or
     * maximum of the range. If within the range, the value
     * is rounded to the nearest value. For example, 4.9999 is
     * rounded to 5.0.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Short toShortValue(double value) {
        if (value >= Short.MAX_VALUE) {
            LOGGER.info("{} was limited to {} in toShortValue()", value, Short.MAX_VALUE);
            return Short.MAX_VALUE;
        } else if (value <= Short.MIN_VALUE) {
            LOGGER.info("{} was limited to {} in toShortValue()", value, Short.MIN_VALUE);
            return Short.MIN_VALUE;
        } else {
            // in the range of int, convert to the nearest int
            return (short) Math.round(value);
        }
    }

    /**
     * Converts a double to a boolean. 1.0 is true, any number
     * other than 1.0 is false.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Boolean toBooleanValue(double value) {
        if (value == 1.0) {
            return true;
        } else {
            if (value != 0.0) {
                LOGGER.info("{} was converted to {} in toBooleanValue()", value, false);
            }
            return false;
        }
    }

    /**
     * Converts a double to a float. Standard loss of precision
     * as noted by the Java Language Specification will occur
     * as per Double.floatValue()
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static Float toFloatValue(double value) {
        // standard loss of precision is expected
        return Double.valueOf(value).floatValue();
    }
}
