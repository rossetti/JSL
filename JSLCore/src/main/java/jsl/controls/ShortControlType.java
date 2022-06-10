package jsl.controls;


import java.util.function.Consumer;

public class ShortControlType extends NumericControlType<Short> {

    public ShortControlType(Consumer<Short> setter, String elementName, String setterName, String comment,
                            Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Short> domain(
            Double lowerBound,
            Double upperBound) {
        return new ShortDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;
        if (lastValue != null) r = (double) (short) lastValue;
        return r;
    }
}
