package jsl.utilities.random;

import jsl.utilities.Interval;

/**
 * Can be used to specify the range of possible values
 */
public interface RangeIfc {

    Interval getRange();

    Interval NegInfToPosInf = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    Interval ZeroToPosInf = new Interval(0.0, Double.POSITIVE_INFINITY);
    Interval NegInfToZero = new Interval(Double.NEGATIVE_INFINITY, 0);
}
