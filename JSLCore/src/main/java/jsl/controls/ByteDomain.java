package jsl.controls;

public final class ByteDomain extends NumericDomain<Byte> {
    public ByteDomain(Double minValue, Double maxValue) {
        super(minValue, maxValue);
    }

    @Override
    protected Byte domainMin() {
        return Byte.MIN_VALUE;
    }

    @Override
    protected Byte domainMax() {
        return Byte.MAX_VALUE;
    }

    @Override
    protected boolean contains(Number v) {
        return isInDataTypeDomain(v) &&
                cast(v) >= minValue &&
                cast(v) <= maxValue;
    }

    @Override
    protected Byte cast(Number v) {
        return v.byteValue();
    }
}
