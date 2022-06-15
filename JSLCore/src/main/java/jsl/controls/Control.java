package jsl.controls;

import jsl.utilities.GetNameIfc;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class Control<T> {
    protected final GetNameIfc element;

    protected final Method method;

    protected String comment = "";

    protected String setterName;

    public Control(GetNameIfc element, Method method, Annotation annotation) {
        Objects.requireNonNull(element, "The invoking object cannot be null");
        Objects.requireNonNull(method, "The method cannot be null");
        Objects.requireNonNull(annotation, "The annotation cannot be null");
        if (!isValidControlAnnotation(annotation)){
            throw new IllegalArgumentException("The supplied annotation is not a control annotation!");
        }
        this.element = element;
        this.method = method;
        processAnnotation(annotation);
    }

    protected void processAnnotation(Annotation annotation){
        //TODO the purpose of this method is to extract the relevant information
        // from the annotation to set up the control, subclasses of Control can
        // override this method to setup specific capabilities
        if (annotation instanceof NumericControl){
            processNumericAnnotation((NumericControl) annotation);
        } else if (annotation instanceof BooleanControl) {
            processBooleanAnnotation((BooleanControl) annotation);
        }
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
     *
     * @param annotation the annotation to check
     * @return true if the annotation is a valid control annotation
     */
    public static boolean isValidControlAnnotation(Annotation annotation){
        //TODO can add other annotation types to this method
        if (annotation == null){
            return false;
        }
        if (annotation instanceof NumericControl){
            return true;
        } else if (annotation instanceof BooleanControl){
            return true;
        } else {
            return false;
        }
    }

    //TODO will there be a need for one of these methods for every control type?
    // bummer, could also have a bunch of isNumericControl(Annotation) type methods
    public static NumericControl castTo(Annotation annotation){
        if (annotation == null){
            return null;
        }
        if (annotation instanceof NumericControl){
            return (NumericControl) annotation;
        } else {
            return null;
        }
    }

    /**
     * Derive a setter name from method and annotation names
     *
     * @param methodName the method name
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

    public void setValue(T value){
        try {
            method.invoke(element, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
