package jsl.controls;

import jsl.utilities.GetNameIfc;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class Control<T> {
    private final GetNameIfc obj;
    private final Method method;

    public Control(GetNameIfc obj, Method method, Annotation annotation) {
        Objects.requireNonNull(obj, "The invoking object cannot be null");
        Objects.requireNonNull(method, "The method cannot be null");
        Objects.requireNonNull(annotation, "The annotation cannot be null");
        this.obj = obj;
        this.method = method;

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

    public void setValue(T value){
        try {
            method.invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
