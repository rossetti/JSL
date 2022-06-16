package jsl.controls;

import jsl.utilities.GetNameIfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Control<T> {

    /**
     * Defines the set of valid control types
     */
    public enum ValidTypes {
        DOUBLE, INTEGER, LONG, FLOAT, SHORT, BYTE, BOOLEAN;

        public static final EnumSet<ValidTypes> typeSet = EnumSet.of(ValidTypes.DOUBLE, ValidTypes.INTEGER,
                ValidTypes.LONG, ValidTypes.FLOAT, ValidTypes.SHORT, ValidTypes.BYTE, ValidTypes.BOOLEAN);

        public static final EnumMap<ValidTypes, Class<?>> typeMap = new EnumMap<>(ValidTypes.class);

        static {
            typeMap.put(ValidTypes.DOUBLE, Double.class);
            typeMap.put(ValidTypes.INTEGER, Integer.class);
            typeMap.put(ValidTypes.LONG, Long.class);
            typeMap.put(ValidTypes.FLOAT, Float.class);
            typeMap.put(ValidTypes.SHORT, Short.class);
            typeMap.put(ValidTypes.BYTE, Byte.class);
            typeMap.put(ValidTypes.BOOLEAN, Boolean.class);
        }
    }

    /**
     * for logging
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    protected final GetNameIfc element;

    protected final Method method;

    protected String comment = "";

    protected String setterName;

    protected T lastValue = null;

    /**
     * Need to know the generic class type at run-time. This is the easiest way
     */
    private final Class<T> type;

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
        this.type = type;
        this.element = element;
        this.method = method;
        processAnnotation();
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
        Annotation annotation = getControlAnnotation();
        LOGGER.info("Processing control annotation: {} for method: {} on class {}",
                annotation.annotationType(), method.getName(), element.getName());
        //TODO the purpose of this method is to extract the relevant information
        // from the annotation to set up the control, subclasses of Control can
        // override this method to setup specific capabilities
        if (annotation instanceof NumericControl) {
            processNumericAnnotation((NumericControl) annotation);
        } else if (annotation instanceof BooleanControl) {
            processBooleanAnnotation((BooleanControl) annotation);
        }
    }

    /**
     * Returns the control annotation for a method.
     * If no control annotations exist or if more than 1 exists, then
     * IllegalArgumentExceptions are thrown
     *
     * @return the annotation
     */
    protected Annotation getControlAnnotation() {
        //TODO it would be nice to loop through possible control annotations rather than
        // check for each type
        List<Annotation> annotations = new ArrayList<>();
        Annotation ann;
        ann = method.getAnnotation(NumericControl.class);
        if (ann != null) annotations.add(ann);
        ann = method.getAnnotation(BooleanControl.class);
        if (ann != null) annotations.add(ann);
        // end TODO
        if (annotations.size() == 0) {
            LOGGER.error("Method {} for class {} was specified as a control, but does not have any control annotations",
                    method.getName(), method.getDeclaringClass().getName());
            throw new IllegalArgumentException("There was no control annotation on the supplied method");
        }
        if (annotations.size() > 1) {
            LOGGER.error("Method {} for class {} was specified as a control, but but had more than one control annotation",
                    method.getName(), method.getDeclaringClass().getName());
            throw new IllegalArgumentException("More than 1 control annotation on a method");
        }
        // must have 1 control annotation
        return annotations.get(0);
    }

    protected void processBooleanAnnotation(BooleanControl annotation) {
        comment = annotation.comment();
        setterName = makeSetterName(method.getName(), annotation.name());
    }

    protected void processNumericAnnotation(NumericControl annotation) {
        comment = annotation.comment();
        setterName = makeSetterName(method.getName(), annotation.name());
    }

    /**
     * check whether the method is a valid single parameter method
     *
     * @param method the method
     * @return true if valid single parameter method
     */
    public static boolean isValidControlMethod(Method method) {
        if (method.getParameterCount() != 1) {
            LOGGER.warn("Method {} for class {} was specified as a control, but is not a single parameter method",
                    method.getName(), method.getDeclaringClass().getName());
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            LOGGER.warn("Method {} for class {} was specified as a control, but is not a public method",
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
        //TODO can add other annotation types to this method
        if (annotation == null) {
            return false;
        }
        if (annotation instanceof NumericControl) {
            return true;
        } else if (annotation instanceof BooleanControl) {
            return true;
        } else {
            return false;
        }
    }

    //TODO will there be a need for one of these methods for every control type?
    // bummer, could also have a bunch of isNumericControl(Annotation) type methods
    public static NumericControl castTo(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        if (annotation instanceof NumericControl) {
            return (NumericControl) annotation;
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
