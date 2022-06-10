package jsl.controls;

import java.util.function.Consumer;

/**
 * subclass for numeric parameter controls (Double, Float, Long etc.)
 * extends ControlType to add a domain for the parameter.
 * all domains are bounded by the min/max values of the control type
 * but can be additionally restricted by adding upper and lower bounds
 */
public abstract class NumericControlType<T extends Number> extends ControlType<T> {
    // lower and upper bounds are held in a domain field
    protected NumericDomain<T> myDomain;

    public NumericControlType(Consumer<T> setter, String elementName, String setterName, String comment,
                              Double lowerBound,
                              Double upperBound) {
        super(setter, elementName, setterName, comment);
        // set the function domain
        myDomain = domain(lowerBound, upperBound);
    }

    public void setAll(
            Consumer<T> setter,
            String elementName,
            String setterName,
            String comment,
            Double lowerBound,
            Double upperBound) {
        setAll(setter, elementName, setterName, comment);
        // set the function domain
        myDomain = domain(lowerBound, upperBound);
    }

    // require sub-classes to provide a (bounded) domain method
    abstract NumericDomain<T> domain(Double lowerBound, Double upperBound);

    /**
     * could this Object be a Number ?
     * @param v the number to check
     * @return true if a number
     */
    private boolean isNumeric(Object v){
        return Number.class.isAssignableFrom(v.getClass());
    }

    /**
     * check for a numeric value , return it if found
     * otherwise throw an error
     * @param v the number to convert
     * @return the converted object as a Number
     */
    private Number chkNumber(Object v){
        if (isNumeric(v)){
            return (Number) v;
        } else {
            throw(new IllegalArgumentException(
                    "attempted to assign a non-numeric value" +
                            " to a numeric control"));
        }
    }

    @Override
    protected final T castValue(Object v) {
        Number r = chkNumber(v);
        return myDomain.cast(r);
    }

    @Override
    protected final boolean contains(Object v) {
        Number r = chkNumber(v);
        return myDomain.contains(r);
    }

    @Override
    public String getDomainAsString() {
        return myDomain.toString();
    }
}











