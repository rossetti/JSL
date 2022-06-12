package jsl.controls;

import java.lang.annotation.*;


/**
 * NumericControlType setter annotations are used to flag single parameter setter methods
 * (numeric, String or boolean) as potential Controls
 * <p>
 * Note that custom annotations cannot extend/inherit from any other
 * class (including annotations) so the different annotations defined here
 * are essentially unrelated.
 * <p>
 * The BooleanControl is the simplest setter with only
 * name and comment elements defined
 */
@Documented                             // flag inclusion in documentation
@Inherited                              // flag that it is inherited by subclasses
@Target({ElementType.METHOD})           // targets methods ONLY
@Retention(RetentionPolicy.RUNTIME)     // available at run-time
public @interface BooleanControl {
    String name() default "";

    String comment() default "";
}
