package jsl.controls;

/**
 * Classes to define (optionally bounded) numeric domains by numeric type
 * <p>
 * Note that all upper/lower bound values are provided as Doubles to make it
 * easier to work with these classes.
 * <p>
 * the domains (bounded or otherwise) created are data type specific
 * <p>
 * Key functionality of a domain is to determine whether a domain CONTAINS a
 * value and to attempt to CAST from an Object to this domain's numeric type
 */
public abstract class NumericDomain<T extends Number> {
    //fields
    T domainMin = null;
    T domainMax = null;
    T minValue = null;
    T maxValue = null;

    // constructor always receives Double for bounds
    // making it easier to call from reflection based code
    protected NumericDomain(Double minValue, Double maxValue) {
        domainMin = domainMin();
        domainMax = domainMax();
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    // sub-classes must provide domain min//max for this data type
    // rather than check the types and have a big switch statement to cast
    // appropriately require these methods from sub-classes

    /**
     * @return the minimum for the domain
     */
    protected abstract T domainMin();

    /**
     * @return the maximum for the domain
     */
    protected abstract T domainMax();

    /**
     * @param v the number to check
     * @return true if contained in the domain
     */
    protected abstract boolean contains(Number v);

    /**
     * casts the number to the specified type
     *
     * @param v the number to cast
     * @return the type casted to
     */
    protected abstract T cast(Number v);

    //setters & getters

    /**
     * @return the minimum value for the domain
     */
    public final T getMinValue() {
        return cast(minValue);
    }

    /**
     * @param minV the min value to set for the domain
     */
    public final void setMinValue(Double minV) {
        if (minV != null && isInDataTypeDomain(minV)) minValue = cast(minV);
    }

    /**
     * @return the max value for the domain
     */
    public final T getMaxValue() {
        return cast(maxValue);
    }

    /**
     * @param maxV the value to set for the domain
     */
    public final void setMaxValue(Double maxV) {
        if (maxV != null && isInDataTypeDomain(maxV)) maxValue = cast(maxV);
    }

    /**
     * check (using Doubles) whether the Number v is in the domain of the
     * (otherwise unbounded) data type
     *
     * @param v the number to check
     * @return true if data is within domain
     */
    protected final boolean isInDataTypeDomain(Number v) {
        double d = v.doubleValue();
        return (d >= domainMin.doubleValue()
                && d <= domainMax.doubleValue());
    }

    //TODO: make this return appropriate Strings for both
    // Discrete and continuous types
    @Override
    public String toString() {
        return "[" + minValue + ",..., " + maxValue + "]";
    }


    // initial Test
    public static void main(String[] args) {
        final ByteDomain bdm = new ByteDomain(-100.0, 20.0);
        System.out.println(bdm);
    }
}
