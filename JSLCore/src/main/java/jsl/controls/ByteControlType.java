package jsl.controls;

import java.util.function.Consumer;

public class ByteControlType extends NumericControlType<Byte> {

    public ByteControlType(Consumer<Byte> setter, String elementName, String setterName, String comment,
                           Double lowerBound, Double upperBound) {
        super(setter, elementName, setterName, comment, lowerBound, upperBound);
    }

    @Override
    NumericDomain<Byte> domain(
            Double lowerBound,
            Double upperBound) {
        return new ByteDomain(lowerBound, upperBound);
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;
        if (lastValue != null) r = (double) (byte) lastValue;
        return r;
    }

}
