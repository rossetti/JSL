package jsl.controls.work;

import java.lang.annotation.*;

/**
 * NumericControlType setter annotations are used to flag single parameter setter methods
 * (numeric, String or boolean) as potential Controls
 * <p>
 * Note that custom annotations cannot extend/inherit from any other
 * class (including annotations) so the different annotations defined here
 * are essentially unrelated.
 * <p>
 * The NumericControlType includes the name a possible comment
 * AND upper and lower bounds with defaults at +/- infinity
 */

@Documented                             // flag inclusion in documentation
@Inherited                              // flag that it is inherited by subclasses
@Target({ElementType.METHOD})           // targets methods ONLY
@Retention(RetentionPolicy.RUNTIME)     // available at run-time
public @interface JSLControl {

    Control.Type type() default Control.Type.DOUBLE;

    String name() default "";

    double lowerBound() default Double.NEGATIVE_INFINITY;

    double upperBound() default Double.POSITIVE_INFINITY;

    String comment() default "";
}






