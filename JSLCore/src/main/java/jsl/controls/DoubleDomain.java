package jsl.controls;

public final class DoubleDomain extends NumericDomain<Double> {
    public DoubleDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    // note that the Double.MIN_Value is always positive
    // but the limits are symetrical around 0 so use -MAX_VALUE
    @Override
    protected Double domainMin() {
        return -Double.MAX_VALUE;
    }

    @Override
    protected Double domainMax() {
        return Double.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Double cast(Number v) {
        return v.doubleValue();
    }
}
