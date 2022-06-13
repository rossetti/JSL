package jsl.controls;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.utilities.GetNameIfc;
import jsl.utilities.reporting.JSL;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;


/**
 * This class extends a LinkedHashMap for ControlType Objects
 * Using a LinkedHashMap to require unique key and preserve insertion order (making it easier to read)
 * ControlType objects reference single-param setter methods with annotations
 */
public class AnnotationControls {

    private final LinkedHashMap<String, ControlType<?>> myControls = new LinkedHashMap<>();

    /**
     * Store a new control
     * (single param because the key is already part of the control object)
     *
     * @param controlType the control to add
     */
    public ControlType<?> store(ControlType<?> controlType) {
        Objects.requireNonNull(controlType, "The control cannot be null");
        String key = controlType.getKey();
        return myControls.put(key, controlType);
    }

    /**
     * Store to ensure the key and
     * the key (from the control) match
     *
     * @param key         the key
     * @param controlType the control type
     */
    private ControlType<?> store(String key, ControlType<?> controlType) {
        Objects.requireNonNull(controlType, "The control cannot be null");
        if (!key.equals(controlType.getKey())) throw new IllegalArgumentException(
                "the control keys must match");
        return store(controlType);
    }

    private Set<Map.Entry<String, ControlType<?>>> entrySet() {
        return myControls.entrySet();
    }

    public void storeAll(AnnotationControls controls) {
        Objects.requireNonNull(controls, "The controls cannot be null");
        for (Map.Entry<String, ControlType<?>> entry : controls.entrySet()) {
            store(entry.getValue());
        }
    }

    /**
     * convenience setter for an existing control
     *
     * @param key the key
     * @param val the value
     */
    public void setControlValue(String key, Object val) {
        if (myControls.containsKey(key)) {
            myControls.get(key).setValue(val);
        } else {
            //TODO use a logger and/or throw an error
            System.err.println("attempt to access invalid control " + key);
        }
    }

    /**
     * Generate a "flat" map (String, Number) for communication
     * outside this class. The key is the control key and the
     * number is the last double value assigned to the control
     *
     * @return the map
     */
    public Map<String, Number> getControlValues() {
        Map<String, Number> map = new LinkedHashMap<>();
        for (Map.Entry<String, ControlType<?>> entry : myControls.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getLastDoubleValue());
        }
        return map;
    }

    /**
     * Return an ArrayList of ControlDetailsRecords providing
     * additional detail on Controls (but without giving
     * direct access to the control)
     *
     * @return an ArrayList of ControlRecords
     */
    public ArrayList<ControlRecord> getControlRecords() {
        ArrayList<ControlRecord> list = new ArrayList<>();
        for (Map.Entry<String, ControlType<?>> entry : myControls.entrySet()) {
            list.add(entry.getValue().getControlRecord());
        }
        return list;
    }

    /**
     * @return the array list of getControlRecords() as a string
     */
    public String getControlRecordsAsString() {
        StringBuilder str = new StringBuilder();
        ArrayList<ControlRecord> list = getControlRecords();
        if (list.size() == 0) str.append("{empty}");
        for (ControlRecord cdr : list) {
            str.append(cdr);
            str.append(System.lineSeparator());
        }
        return str.toString();
    }

    /**
     * @param model the model for extraction
     * @return the extracted controls
     */
    public static AnnotationControls extractControls(Model model) {
        List<ModelElement> elements = model.getModelElements();
        AnnotationControls cs = new AnnotationControls();
        for (ModelElement me : elements) {
            AnnotationControls ac = AnnotationControls.extractControls(me);
            cs.storeAll(ac);
        }
        return cs;
    }

    /**
     * extract Controls for a modelElement
     *
     * @param modelElement the model element to extract from
     * @return Controls
     */
    public static AnnotationControls extractControls(ModelElement modelElement) {
        AnnotationControls controls = new AnnotationControls();

        // NOTE: we get structure from the class and set values with the object
        Class<? extends ModelElement> cls = modelElement.getClass();
        Method[] methods = cls.getMethods();

        for (Method method : methods) {
            // @NumericControlType annotations
            final List<Annotation> cs = getControlSetters(method);
            if (cs.size() > 1) warnControlSetterIgnore(method, "multiple annotations on a single method");
            if (cs.size() == 1 && chkSingleParamConsumer(method)) {
                if (cs.get(0) instanceof NumericControl) {
                    if (isNumericType(method.getParameterTypes()[0])) {
                        controls.store(numericControlBuilder(modelElement, method, (NumericControl) cs.get(0)));
                    } else {
                        warnControlSetterIgnore(method, "@NumericControlType on non-numeric method");
                    }
                } else if (cs.get(0) instanceof BooleanControl) {
                    if (isBooleanType(method.getParameterTypes()[0])) {
                        controls.store(booleanControlBuilder(modelElement, method, (BooleanControl) cs.get(0)));
                    } else {
                        warnControlSetterIgnore(method, "@BooleanControl on non-boolean method");
                    }
                }

            }
        }
        return controls;
    }

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

    //TODO since the building of a numeric control depends only on these objects
    // GetNameIfc obj, Method method, NumericControl annotation
    // the ControlType constructor should just have these as a arguments.

    /**
     * Build a new NumericControlType
     *
     * @param obj    the object to process
     * @param method the method to process
     * @param annotation the annotation to process
     * @return the built ControlType
     */
    private static ControlType<?> numericControlBuilder(GetNameIfc obj, Method method, NumericControl annotation) {
        // local vars
        Class<?> pType = method.getParameterTypes()[0];
        String nm = setterName(method.getName(), annotation.name());

        // create the right subclass of ControlType
        if (pType.equals(Double.class) || pType.equals(double.class)) {
            ControlSetter<Double> setter = new ControlSetter<>(obj, method);
            return new DoubleControlType(setter, obj.getName(), nm,
                    annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else if (pType.equals(Float.class) || pType.equals(float.class)) {
            ControlSetter<Float> setter = new ControlSetter<>(obj, method);
            return new FloatControlType(setter, obj.getName(), nm, annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else if (pType.equals(Long.class) || pType.equals(long.class)) {
            ControlSetter<Long> setter = new ControlSetter<>(obj, method);
            return new LongControlType(setter, obj.getName(), nm, annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else if (pType.equals(Integer.class) || pType.equals(int.class)) {
            ControlSetter<Integer> setter = new ControlSetter<>(obj, method);
            return new IntegerControlType(setter, obj.getName(), nm, annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else if (pType.equals(Short.class) || pType.equals(short.class)) {
            ControlSetter<Short> setter = new ControlSetter<>(obj, method);
            return new ShortControlType(setter, obj.getName(), nm, annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else if (pType.equals(Byte.class) || pType.equals(byte.class)) {
            ControlSetter<Byte> setter = new ControlSetter<>(obj, method);
            return new ByteControlType(setter, obj.getName(), nm, annotation.comment(), annotation.lowerBound(), annotation.upperBound());
        } else {
            // if we only ever create "valid" controls, we will NEVER get here
            throw new IllegalArgumentException("trying to create an unknown control type: " + pType.getSimpleName());
        }
    }

    /**
     * Build a new BooleanControlType
     *
     * @param obj    the object to process
     * @param method the method to process
     * @param annotation the annotation to process
     * @return the built ControlType
     */
    private static ControlType<?> booleanControlBuilder(GetNameIfc obj, Method method, BooleanControl annotation) {
        ControlSetter<Boolean> setter = new ControlSetter<>(obj, method);
        return new BooleanControlType(setter,
                obj.getName(), setterName(method.getName(), annotation.name()), annotation.comment());
    }

    /**
     * create a Consumer (functional interface) method reference
     * that invokes method with a parameter
     *
     * @param obj    the object to process
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

    //TODO I don't think that this has to be a Consumer
    // in fact, I think that this should be directly part of the ControlType
    // because this accept() method is just called in its setValue() method
    private static class ControlSetter<T> implements Consumer<T>{
        private final GetNameIfc obj;
        private final Method method;
        public ControlSetter(GetNameIfc obj, Method method){
            this.obj = obj;
            this.method = method;
        }
        @Override
        public void accept(T aDouble) {
            try {
                method.invoke(obj, aDouble);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
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
                setterName = setterName.substring(3);
            }
            setterName = setterName.substring(0, 1).toLowerCase()
                    + setterName.substring(1);
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
            warnControlSetterIgnore(method, " is not a single-parameter setter");
            return false;
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            warnControlSetterIgnore(method, " must be public");
            return false;
        }

        // made it !
        return true;
    }

    private static void warnControlSetterIgnore(Method method, String msg) {
        String str = method.getDeclaringClass().getName() + " " +
                ReflectionUtilities.signatureAsString(method) + ") annotation ignored : ";
        JSL.getInstance().LOGGER.warn(str + msg);
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

}
