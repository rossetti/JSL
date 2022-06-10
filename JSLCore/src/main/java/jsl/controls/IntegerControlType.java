package jsl.controls;


import java.util.function.Consumer;

public class IntegerControlType extends NumericControlType<Integer> {

    public IntegerControlType(Consumer<Integer> setter, String elementName, String setterName, String comment,
                              Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Integer> domain(
            Double lowerBound,
            Double upperBound) {
        return new IntegerDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;
        if (lastValue != null) r = (double) (int) lastValue;
        return r;
    }
}
