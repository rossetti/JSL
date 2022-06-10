package jsl.controls;

import java.util.function.Consumer;

//TODO not sure if type parameter should be Boolean
public class BooleanControlType extends ControlType<Boolean> {

    public BooleanControlType(
            Consumer<Boolean> setter,
            String elementName,
            String setterName,
            String comment) {
        super(setter, elementName, setterName, comment);
//        setAll(setter, elementName, setterName, comment);
    }


    @Override
    protected Boolean castValue(Object v) {
        Boolean b;
        if (Boolean.class.isAssignableFrom(v.getClass())) {
            // easy boolean
            b = (Boolean) v;
        } else if (Double.class.isAssignableFrom(v.getClass())) {
            // numerics
            //      false if 0
            //      true otherwise
            b = ((Double) v) != 0;
        } else {
            throw (new IllegalArgumentException(
                    "unable to cast Object as Boolean"));
        }
        return b;
    }

    @Override
    public String getDomainAsString() {
        return "{0,1}";
    }

    /**
     * if Object CAN be cast as Boolean, it's contained in this domain
     */
    @Override
    protected boolean contains(Object v) {
        Boolean b = castValue(v);
        return true;
    }

    @Override
    public Double getLastDoubleValue() {
        Double r = null;

        if (lastValue != null) {
            r = 0.0;
            if ((boolean) lastValue) r = 1.0;
        }

        return r;
    }
}
