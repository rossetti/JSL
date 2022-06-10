package jsl.controls;

public final class ShortDomain extends NumericDomain<Short> {
    public ShortDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    @Override
    protected Short domainMin() {
        return Short.MIN_VALUE;
    }

    @Override
    protected Short domainMax() {
        return Short.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Short cast(Number v) {
        return v.shortValue();
    }
}
