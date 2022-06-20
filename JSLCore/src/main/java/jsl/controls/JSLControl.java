package jsl.controls;

import java.lang.annotation.*;

/**
 * A JSLControl annotation is used to mark single parameter methods within
 * model elements to indicate that those methods should be used to control
 * the execution of the simulation model. The annotation field type must be supplied
 * and must be one of the valid control types as specified by the enum ControlType.
 * The user is responsible for making sure that the type field matches (or is consistent with)
 * the type of the parameter of the method.  For this purpose, primitive types and their wrapper
 * types (e.g. double/Double, Integer/int, etc.) are considered the same (interchangeable) as
 * denoted by the valid control types.  Even though the optional annotation fields (lowerBound and upperBound)
 * are specified as double values, they will be converted to an appropriate value for the specified
 * type.  Current control types are the primitives and their wrappers as well as boolean/Boolean. Future
 * types may be more general object types, in which case, the bounds will be ignored.
 */

@Documented                             // flag inclusion in documentation
@Inherited                              // flag that it is inherited by subclasses
@Target({ElementType.METHOD})           // targets methods ONLY
@Retention(RetentionPolicy.RUNTIME)     // available at run-time
public @interface JSLControl {

    /**
     *
     * @return the type of the control
     */
    ControlType type();

    /**
     *
     * @return the name of the control
     */
    String name() default "";

    /** If this field is not specified, it will be translated to the smallest
     *  negative value associated with the type specified by the field type.
     *  For example, if the type is INTEGER then the default lower bound will
     *  be Integer.MIN_VALUE. The user can supply more constraining values for
     *  the bounds that are within the range of the associated type.
     *
     * @return the lower bound to be permitted by this control
     */
    double lowerBound() default Double.NEGATIVE_INFINITY;

    /** If this field is not specified, it will be translated to the largest
     *  positive value associated with the type specified by the field type.
     *  For example, if the type is INTEGER then the default lower bound will
     *  be Integer.MAX_VALUE. The user can supply more constraining values for
     *  the bounds that are within the range of the associated type.
     *
     * @return the lower bound to be permitted by this control
     */
    double upperBound() default Double.POSITIVE_INFINITY;

    /**
     *
     * @return a comment associated with the annotation
     */
    String comment() default "";
}






