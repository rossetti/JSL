package jsl.controls;

public final class IntegerDomain extends NumericDomain<Integer> {
    public IntegerDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    @Override
    protected Integer domainMin() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected Integer domainMax() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Integer cast(Number v) {
        return v.intValue();
    }
}
