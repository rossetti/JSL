package jsl.controls;

public final class LongDomain extends NumericDomain<Long> {
    public LongDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    @Override
    protected Long domainMin() {
        return Long.MIN_VALUE;
    }

    @Override
    protected Long domainMax() {
        return Long.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Long cast(Number v) {
        return v.longValue();
    }
}
