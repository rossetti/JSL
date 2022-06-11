package jsl.controls;

import java.util.LinkedHashMap;
import java.util.Objects;


/**
 * This class extends a LinkedHashMap for ControlType Objects
 * Using a LinkedHashMap to require unique key and preserve insertion order (making it easier to read)
 * ControlType objects reference single-param setter methods with annotations
 */
public class AnnotationControlMap extends LinkedHashMap<String, ControlType> {

    /**
     * put a new control to the map
     * (single param because the key is already part of the control object)
     * @param ctrl the control to add
     */
    public ControlType put(ControlType ctrl) {
        Objects.requireNonNull(ctrl, "The control cannot be null");
        String key = ctrl.getKey();
        return super.put(key, ctrl);
    }


    /**
     * hide the super-class put to ensure the key (for the map) and
     * the key (from the control) match
     *
     * @param key the key
     * @param ctrl the control type
     */
    public ControlType put(String key, ControlType ctrl) {
        Objects.requireNonNull(ctrl, "The control cannot be null");
        if (!key.equals(ctrl.getKey())) throw new IllegalArgumentException(
                "the control keys must match");
        return put(ctrl);
    }

    /**
     * append a map of controls
     *
     * @param ctrls the map of controls
     */
    public void putAll(AnnotationControlMap ctrls) {
        ctrls.forEach((k, v) -> this.put(v));
    }

    /**
     * convenience setter for an existing control
     *
     * @param key the key
     * @param val the value
     */
    public void setControlValue(String key, Object val) {
        if (containsKey(key)) {
            get(key).setValue(val);
        } else {
            System.err.println("attempt to access invalid control " + key);
        }
    }

    /**
     * generate a "flat" map (String, Number) for communication
     * outside this class
     *
     * @return
     */
    //TODO
//    public Experiment.Controls getControlValues() {
//        Experiment.Controls res = new Experiment.Controls();
//        // we can't see the actual value of a control
//        // but we can pull the last value to be set
//        // uses getLastDoubleValue to ensure it is a Double
//        this.forEach((k, v) -> res.put(k, v.getLastDoubleValue()));
//        return res;
//    }


    /**
     * return an ArrayList of ControlRecords providing
     * additional detail on Controls (but without giving
     * direct access to the control)
     *
     * @return an ArrayList of ControlRecords
     */
    public ControlDetails getControlDetails() {
        ControlDetails res = new ControlDetails();
        this.forEach((k, v) -> res.add(v.getControlRecord()));
        return res;
    }


}
