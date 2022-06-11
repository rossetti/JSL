package jsl.controls;

import jsl.simulation.ModelElement;
import jsl.utilities.GetNameIfc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The (abstract) class ControlBuilder holds methods to extract control
 * annotations @XXXControlBuilder from JSL model code, confirm control types
 * and build a map of controls with keys compounded from ModelElement name and control name
 */
public abstract class ControlBuilder {

    private ControlBuilder() {
        // can't instantiate a static class
    }

    static Map<Class<?>, Method[]> classMethods = new HashMap<>();

    // is...Type methods
    // return true for valid control parameter types
    public static boolean isNumericType(Class<?> type) {
        return (type.isPrimitive() && type != void.class && type != boolean.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Byte.class;
    }

    public static boolean isBooleanType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    public static boolean isStringType(Class<?> type) {
        return type == String.class;
    }

    /**
     * extract Controls for a modelElement
     *
     * @param modelElement the model element to extract from
     * @return Controls
     */
    public static AnnotationControlMap extractControls(ModelElement modelElement) {
        AnnotationControlMap myControls = new AnnotationControlMap();

        // NOTE: we get structure from the class and set values with the object
        Class cls = modelElement.getClass();
        Method[] methods;
        // check whether we have this class definition already
        if (classMethods.containsKey(cls)) {
            methods = classMethods.get(cls);
        } else {
            // if not, get all methods for this class and add them to the Map
            methods = cls.getMethods();
            classMethods.put(cls, methods);
        }

        for (Method method : methods) {
            // @NumericControlType annotations
            final List<Annotation> cs = getControlSetters(method);
            if (cs.size() > 1) warnControlSetterIgnore(
                    method, "multiple annotations on a single method");
            if (cs.size() == 1 && chkSingleParamConsumer(method)) {
                if (cs.get(0) instanceof NumericControl) {
                    if (isNumericType(method.getParameterTypes()[0])) {
                        myControls.put(numericControlBuilder(
                                modelElement,
                                method,
                                (NumericControl) cs.get(0)));
                    } else {
                        warnControlSetterIgnore(
                                method,
                                "@NumericControlType on non-numeric method");
                    }
                } else if (cs.get(0) instanceof BooleanControl) {
                    if (isBooleanType(method.getParameterTypes()[0])) {
                        myControls.put(booleanControlBuilder(
                                modelElement,
                                method,
                                (BooleanControl) cs.get(0)));
                    } else {
                        warnControlSetterIgnore(
                                method,
                                "@BooleanControl on non-boolean method");
                    }
                }

            }
        }

        return myControls;
    }

    /**
     * Build a new NumericControlType
     *
     * @param obj the object to process
     * @param method the method to process
     * @param setter the stter to process
     * @return the built ControlType
     */
    private static ControlType numericControlBuilder(
            GetNameIfc obj,
            Method method,
            NumericControl setter) {

        // local vars
        NumericControlType ctrl;
        Class<?> pType = method.getParameterTypes()[0];
        String nm = setterName(method.getName(), setter.name());
        Consumer<?> csmr = getConsumer(obj, method);

        // create the right subclass of ControlType
        if (pType.equals(Double.class) || pType.equals(double.class)) {
            ctrl = new DoubleControlType((Consumer<Double>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else if (pType.equals(Float.class) || pType.equals(float.class)) {
            ctrl = new FloatControlType((Consumer<Float>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else if (pType.equals(Long.class) || pType.equals(long.class)) {
            ctrl = new LongControlType((Consumer<Long>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else if (pType.equals(Integer.class) || pType.equals(int.class)) {
            ctrl = new IntegerControlType((Consumer<Integer>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else if (pType.equals(Short.class) || pType.equals(short.class)) {
            ctrl = new ShortControlType((Consumer<Short>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else if (pType.equals(Byte.class) || pType.equals(byte.class)) {
            ctrl = new ByteControlType((Consumer<Byte>) csmr,
                    obj.getName(),
                    nm,
                    setter.comment(),
                    setter.lowerBound(),
                    setter.upperBound());
        } else {
            // if we only ever create "valid" controls, we will NEVER get here
            throw (new IllegalArgumentException(
                    "trying to create an unknown control type: "
                            + pType.getSimpleName()));
        }


        // set all fields of the control
//            ctrl.setAll(
//                    csmr,
//                    obj.getName(),
//                    nm,
//                    setter.comment(),
//                    setter.lowerBound(),
//                    setter.upperBound()
//            );

        return ctrl;
    }


    /**
     * Build a new BooleanControlType
     *
     * @param obj the object to process
     * @param method the method to process
     * @param setter the setter to process
     * @return the built ControlType
     */
    private static ControlType booleanControlBuilder(
            GetNameIfc obj,
            Method method,
            BooleanControl setter) {

        return new BooleanControlType(
                (Consumer<Boolean>) getConsumer(obj, method),
                obj.getName(),
                setterName(method.getName(), setter.name()),
                setter.comment());
    }


    /**
     * create a Consumer (functional interface) method reference
     * that invokes method with a parameter
     *
     * @param obj the object to process
     * @param method the method to process
     * @return the consumer
     */
    private static Consumer<?> getConsumer(GetNameIfc obj, Method method) {
        Consumer<?> csmr;
        csmr = v -> {
            try {
                method.invoke(obj, v);
            } catch (Exception e) {
                throw new RuntimeException("Attempting to invoke method: "
                        + method.getName()
                        + "(" + v + ")"
                        + " on " + obj.getName() + " \n"
                        + e.getMessage());
            }
        };
        return csmr;
    }

    /**
     * alias (or derive) a setter name from method and annotation names
     *
     * @param methodName the method name
     * @param setterName the setter name
     * @return the string name
     */
    private static String setterName(String methodName, String setterName) {
        if (setterName == null) setterName = "";
        if (setterName.equals("")) {
            setterName = methodName;
            if (setterName.startsWith("set")) {
                setterName = setterName.substring(3, setterName.length());
            }
            setterName = setterName.substring(0, 1).toLowerCase()
                    + setterName.substring(1, setterName.length());
        }
        return setterName;
    }


    /**
     * check whether the method is a valid single parameter consumer
     *
     * @param method the method
     * @return true if valid single parameter consumer
     */
    private static boolean chkSingleParamConsumer(Method method) {
        if (method.getParameterCount() != 1) {
            warnControlSetterIgnore(method,
                    " is not a single-parameter setter");
            return false;
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            warnControlSetterIgnore(method,
                    " must be public");
            return false;
        }

        // made it !
        return true;
    }

    private static void warnControlSetterIgnore(Method method, String msg) {
        System.err.println(
                method.getDeclaringClass().getName() + " @ExperimentRunner.ExperimentRunner " +
                        ReflectionUtilities.signatureAsString(method) + ") annotation ignored : " + msg
        );
    }

    /**
     * returns a list of control-setter annotations for a method
     *
     * @param method the method
     * @return the list
     */
    private static List<Annotation> getControlSetters(Method method) {
        List<Annotation> res = new ArrayList<>();
        Annotation ann;

        ann = method.getAnnotation(NumericControl.class);
        if (ann != null) res.add(ann);

        ann = method.getAnnotation(BooleanControl.class);
        if (ann != null) res.add(ann);

        return res;
    }

    /**
     * inspired by <a href="http://www.java2s.com/Code/Java/Reflection/Methodsignature.htm">...</a>
     * simplified and modified for purpose
     * <p>
     * methods to return signature (and components thereof) of methods as a string
     */

    public static class ReflectionUtilities {
        private ReflectionUtilities() {
            // can't instantiate this class
        }

        public static String signatureAsString(Method method) {
            return
                    modifiersAsString(method) + " "
                            + returnTypeAsString(method) + " "
                            + method.getName() + "("
                            + parametersAsString(method) + ")";
        }

        public static String modifiersAsString(Method method) {
            StringBuilder sb = new StringBuilder();
            if (Modifier.isPublic(method.getModifiers()))
                sb.append("public ");
            if (Modifier.isProtected(method.getModifiers()))
                sb.append("protected ");
            if (Modifier.isPrivate(method.getModifiers()))
                sb.append("private ");
            if (Modifier.isFinal(method.getModifiers()))
                sb.append("final ");
            if (Modifier.isStatic(method.getModifiers()))
                sb.append("static ");
            // remove the trailing blank
            if (sb.length() > 0)
                sb.delete(sb.length() - 1, sb.length());
            return sb.toString();
        }

        public static String returnTypeAsString(Method method) {
            return method.getReturnType().getSimpleName();
        }

        public static String parametersAsString(Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) return "";
            StringBuilder paramString = new StringBuilder();
            paramString.append(parameterTypes[0].getSimpleName());
            for (int i = 1; i < parameterTypes.length; i++) {
                paramString.append(",").append(parameterTypes[i].getSimpleName());
            }
            return paramString.toString();
        }
    }
}
