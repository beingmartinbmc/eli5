package io.github.beingmartinbmc.eli5.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate "Explain Like I'm 5" documentation for Java code.
 * Can be applied to methods, classes, and fields to automatically generate
 * simple explanations of what the code does.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
public @interface ExplainLikeImFive {
    /**
     * Optional custom prompt to guide the AI explanation.
     * If not provided, a default prompt will be used.
     */
    String prompt() default "";
    
    /**
     * Whether to include the method body in the explanation.
     * Default is true for methods, false for classes and fields.
     */
    boolean includeBody() default true;
}
