package jsl.controls;

public final class FloatDomain extends NumericDomain<Float> {
    public FloatDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    // note that the Float.MIN_VALUE is always positive
    // but the limits are symmetrical around 0 so use -MAX_VALUE
    @Override
    protected Float domainMin() {
        return -Float.MAX_VALUE;
    }

    @Override
    protected Float domainMax() {
        return Float.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Float cast(Number v) {
        return v.floatValue();
    }
}
