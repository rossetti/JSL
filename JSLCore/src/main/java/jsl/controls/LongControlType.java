package jsl.controls;

import java.util.function.Consumer;

public class LongControlType extends NumericControlType<Long> {

    public LongControlType(Consumer<Long> setter, String elementName, String setterName, String comment,
                           Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Long> domain(
            Double lowerBound,
            Double upperBound) {
        return new LongDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;
        if (lastValue != null) r = (double) (long) lastValue;
        return r;
    }
}

