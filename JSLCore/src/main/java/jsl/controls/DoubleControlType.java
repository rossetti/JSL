package jsl.controls;

import java.util.function.Consumer;

public class DoubleControlType extends NumericControlType<Double> {

    public DoubleControlType(Consumer<Double> setter, String elementName, String setterName, String comment,
                             Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Double> domain(
            Double lowerBound,
            Double upperBound) {
        return new DoubleDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        return (Double) lastValue;
    }
}
