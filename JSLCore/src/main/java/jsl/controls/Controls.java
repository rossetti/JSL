package jsl.controls;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;

import java.lang.reflect.Method;
import java.util.*;


/**
 * The purpose of this class is to hold controls that have been processed from a JSL model.
 * The user of this class can access the controls by their names and then invoke changes to
 * the controlled element.  The main method for creating the controls is to use the static methods
 * that extract controls from a Model or a ModelElement.
 */
public class Controls {

    private final LinkedHashMap<String, Control<?>> myControls = new LinkedHashMap<>();

    /**
     * Store a new control
     *
     * @param controlType the control to add
     */
    public Control<?> store(Control<?> controlType) {
        Objects.requireNonNull(controlType, "The control cannot be null");
        String key = controlType.getKey();
        return myControls.put(key, controlType);
    }

    /**
     *
     * @return the control keys as an unmodifiable set of strings
     */
    public Set<String> getControlKeys(){
        return Collections.unmodifiableSet(myControls.keySet());
    }

    /**
     * Store to ensure the key and
     * the key (from the control) match
     *
     * @param key         the key
     * @param control the control type
     */
    private Control<?> store(String key, Control<?> control) {
        Objects.requireNonNull(control, "The control cannot be null");
        if (!key.equals(control.getKey())) throw new IllegalArgumentException(
                "the control keys must match");
        return store(control);
    }

    private Set<Map.Entry<String, Control<?>>> entrySet() {
        return myControls.entrySet();
    }

    /** Causes the supplied controls to be stored/added.
     *
     * @param controls the controls to store
     */
    public void storeAll(Controls controls) {
        Objects.requireNonNull(controls, "The controls cannot be null");
        for (Map.Entry<String, Control<?>> entry : controls.entrySet()) {
            store(entry.getValue());
        }
    }

    /** The class type should be associated with a valid control type. For example,
     *  {@literal List<Control<Double>> list = getControls(Control<Double>.class)}
     *
     * @param clazz the type of control wanted, must not be null.
     * @return a list of the controls associated with the supplied type, may be empty
     */
    public <T> List<Control<T>> getControls(Class<Control<T>> clazz){
        Objects.requireNonNull(clazz, "The supplied class type was null");
        if (!ControlType.classTypesToValidTypesMap.containsKey(clazz)){
            return new ArrayList<>();
        }
        ControlType type = ControlType.classTypesToValidTypesMap.get(clazz);
        List<Control<T>> list = new ArrayList<>();
        for (Map.Entry<String, Control<?>> entry : myControls.entrySet()) {
            if (entry.getValue().getAnnotationType() == type){
                Control<?> v = entry.getValue();
                try {
                    list.add(clazz.cast(v));
                } catch(ClassCastException ignored) {
                }
            }
        }
        return list;
    }

    /** Gets a control of the name with the specific class type. For example,
     * {@literal Control<Double> getControl(name, Control<Double>.class); }
     *
     * @param controlKey the key for the control, must not be null
     * @param clazz the class type for the control
     * @return the control or null if the key does not exist as a control or if
     * the control with the name cannot be cast to T
     */
    public <T> Control<T> getControl(String controlKey, Class<Control<T>> clazz){
        Objects.requireNonNull(controlKey, "The supplied string was null");
        try {
            Control<?> v = myControls.get(controlKey);
            if (v == null){
                return null;
            } else {
                return clazz.cast(v);
            }
        } catch(ClassCastException ignored) {
        }
        return null;
    }

    /**
     *
     * @return the set of possible control types held
     */
    public Set<ControlType> getControlTypes(){
        Set<ControlType> set = new HashSet<>();
        for (Map.Entry<String, Control<?>> entry : myControls.entrySet()) {
            set.add(entry.getValue().getAnnotationType());
        }
        return set;
    }

    /**
     * Generate a "flat" map (String, Double) for communication
     * outside this class. The key is the control key and the
     * number is the last double value assigned to the control.
     * Any controls that cannot be translated to Double are ignored.
     *
     * @return the map
     */
    public Map<String, Double> getControlsAsDoubles() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map.Entry<String, Control<?>> entry : myControls.entrySet()) {
            Control<?> c = entry.getValue();
            if (c.isDoubleCompatible()){
                map.put(entry.getKey(), c.getLastValueAsDouble());
            }
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
        for (Map.Entry<String, Control<?>> entry : myControls.entrySet()) {
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

    /** Extracts all controls from every model element of the model
     * that has a control annotation.
     *
     * @param model the model for extraction
     * @return the extracted controls
     */
    public static Controls extractControls(Model model) {
        List<ModelElement> elements = model.getModelElements();
        Controls cs = new Controls();
        for (ModelElement me : elements) {
            Controls ac = Controls.extractControls(me);
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
    public static Controls extractControls(ModelElement modelElement) {
        Controls controls = new Controls();
        Class<? extends ModelElement> cls = modelElement.getClass();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (Control.hasControlAnnotation(method)){
                JSLControl jslControl = Control.getControlAnnotation(method);
                controls.store(new Control<>(jslControl.type().asClass(), modelElement, method));
            }
        }
        return controls;
    }

}
