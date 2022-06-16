package jsl.controls.work;

import jsl.controls.NumericControl;
import jsl.utilities.GetNameIfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static jsl.controls.work.Control.ValidTypes.classTypesToValidTypesMap;
import static jsl.controls.work.Control.ValidTypes.validTypesToClassMap;

public class Control<T> {

    /**
     * Defines the set of valid control types
     */
    public enum ValidTypes {
        DOUBLE, INTEGER, LONG, FLOAT, SHORT, BYTE, BOOLEAN;

        public static final EnumSet<ValidTypes> typeSet = EnumSet.of(ValidTypes.DOUBLE, ValidTypes.INTEGER,
                ValidTypes.LONG, ValidTypes.FLOAT, ValidTypes.SHORT, ValidTypes.BYTE, ValidTypes.BOOLEAN);

        public static final EnumMap<ValidTypes, Class<?>> validTypesToClassMap = new EnumMap<>(ValidTypes.class);

        public static final Map<Class<?>, ValidTypes> classTypesToValidTypesMap = new HashMap<>();

        static {
            validTypesToClassMap.put(ValidTypes.DOUBLE, Double.class);
            validTypesToClassMap.put(ValidTypes.INTEGER, Integer.class);
            validTypesToClassMap.put(ValidTypes.LONG, Long.class);
            validTypesToClassMap.put(ValidTypes.FLOAT, Float.class);
            validTypesToClassMap.put(ValidTypes.SHORT, Short.class);
            validTypesToClassMap.put(ValidTypes.BYTE, Byte.class);
            validTypesToClassMap.put(ValidTypes.BOOLEAN, Boolean.class);

            classTypesToValidTypesMap.put(Double.class, ValidTypes.DOUBLE);
            classTypesToValidTypesMap.put(Integer.class, ValidTypes.INTEGER);
            classTypesToValidTypesMap.put(Long.class, ValidTypes.LONG);
            classTypesToValidTypesMap.put(Float.class, ValidTypes.FLOAT);
            classTypesToValidTypesMap.put(Short.class, ValidTypes.SHORT);
            classTypesToValidTypesMap.put(Byte.class, ValidTypes.BYTE);
            classTypesToValidTypesMap.put(Boolean.class, ValidTypes.BOOLEAN);
        }
    }

    /**
     *
     * @param clazz the class to check
     * @return true if the class is a Number
     */
    public static boolean isNumeric(Class<?> clazz){
        return Number.class.isAssignableFrom(clazz);
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
        if (!classTypesToValidTypesMap.containsKey(type)){
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
        if (!type.isInstance(validTypesToClassMap.get(jslControl.type()))){
            LOGGER.error("Annotation Type {} is not compatible with the control type {}",
                    jslControl.type(), type.getName());
            throw new IllegalArgumentException("Annotation type does not match control type!");
        }
        this.type = type;
        this.element = element;
        this.method = method;
        setterName = makeSetterName(method.getName(), jslControl.name());
        processAnnotation();
    }

    public ValidTypes getAnnotationType() {
        return jslControl.type();
    }

    public String getAnnotationName() {
        return jslControl.name();
    }

    public double getLowerBound() {
        return jslControl.lowerBound();
    }

    public double getUpperBound() {
        return jslControl.upperBound();
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

    protected void processAnnotation() {
        //TODO the purpose of this method is to extract the relevant information
        // from the annotation to set up the control, subclasses of Control can
        // override this method to setup specific capabilities
        LOGGER.info("Processing control annotation: {} for method: {} on class {}",
                jslControl.annotationType(), method.getName(), element.getName());
        ValidTypes t = jslControl.type();

    }

    /**
     *
     * @param method the method to examine
     * @return null if no JSLControl annotation was provided for the method
     */
    public static JSLControl getControlAnnotation(Method method){
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
    public static boolean isValidControlAnnotation(Annotation annotation) {
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
        if (annotation instanceof NumericControl) {
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

    public void setValue(T value) {
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
     * return the value most recently assigned
     *
     * @return the value most recently assigned
     */
    public final T getLastValue() {
        return lastValue;
    }
}
