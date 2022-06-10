package jsl.controls;

import java.util.function.Consumer;

public class FloatControlType extends NumericControlType<Float> {

    public FloatControlType(Consumer<Float> setter, String elementName, String setterName, String comment,
                            Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Float> domain(
            Double lowerBound,
            Double upperBound) {
        return new FloatDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;
        if (lastValue != null) r = (double) (float) lastValue;
        return r;
    }
}
