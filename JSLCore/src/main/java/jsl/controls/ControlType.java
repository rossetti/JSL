package jsl.controls;


import java.util.Objects;
import java.util.function.Consumer;

/**
 * ABSTRACT Class to hold a reference to and interact with a setter of an object
 * The generic  T is the class type of the setter parameter.
 */
public abstract class ControlType<T> {
    protected Consumer<T> setter;
    private String elementName;
    private String setterName;
    protected String comment = "";
    protected T lastValue = null;

    public ControlType(Consumer<T> setter, String elementName, String setterName, String comment) {
        Objects.requireNonNull(setter, "setter cannot be null");
        Objects.requireNonNull(setter, "elementName cannot be null");
        Objects.requireNonNull(setter, "setterName cannot be null");
        this.setter = setter;
        this.elementName = elementName;
        this.setterName = setterName;
        this.comment = comment;
    }

    //TODO why is this needed?
    public void setAll(
            // the all important setter
            Consumer<T> setter,
            //identifiers
            String elementName,
            String setterName,
            // other
            String comment) {

        // basic validation
        Objects.requireNonNull(setter, "setter cannot be null");
        Objects.requireNonNull(setter, "elementName cannot be null");
        Objects.requireNonNull(setter, "setterName cannot be null");

        this.setter = setter;
        this.elementName = elementName;
        this.setterName = setterName;
        this.comment = comment;
    }

    /**
     * require a method that takes an Object, sensibly casts and
     * assigns to the control
     *
     * @param v the object to use for setting
     */
    public final void setValue(Object v) {
        // cast and assign the control value
        T r = castValue(v);
        setter.accept(r);
        // record the value last set
        // rather than try to read it from a getter on demand
        this.lastValue = r;
    }

    /**
     * return the value most recently assigned
     *
     * @return the value most recently assigned
     */
    public final T getLastValue() {
        return lastValue;
    }


    // require an explicit mapping of lastValue to Double
    public abstract Double getLastDoubleValue();

    /**
     * returns the class of value type T without issues from Type erasure
     */
    public final Class<?> getValueClass() {
        return getLastValue().getClass();
    }

    /**
     * require a method from subclasses to cast from Object to T
     *
     * @param v the value to cast from
     * @return the value to cast to
     */
    protected abstract T castValue(Object v);

    /**
     * require a method to tell us if the new value is within
     * the relevant domain
     *
     * @param v the object to check if contained
     * @return true if contained
     */
    protected abstract boolean contains(Object v);


    // require a method to return a String representation of the domain
    public abstract String getDomainAsString();

    /**
     * generate a compound key of element name and setter name delimited by '.'
     * (escape any periods in the component parts to ensure it stays unique)
     *
     * @return the key
     */
    public final String getKey() {
        String en = this.elementName.replace(".", "\\.");
        String sn = this.setterName.replace(".", "\\.");
        return en + "." + sn;
    }

    /**
     * class to define (and populate) a more detailed control record.
     *
     * @return the annotation control map
     */
//    public final AnnotationControlMap.ControlDetails.ControlDetailsRecord getControlRecord() {
//        return new AnnotationControlMap.ControlDetails.ControlDetailsRecord(this);
//    }
    public final String getComment() {
        return comment;
    }
}

