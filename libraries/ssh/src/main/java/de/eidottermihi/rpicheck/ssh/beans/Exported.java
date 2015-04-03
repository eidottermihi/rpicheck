package de.eidottermihi.rpicheck.ssh.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declaring getter-methods with this annotation marks them as exportable (e.g. for placeholders, sharing).
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Exported {

    /**
     * @return pretty key
     */
    String value() default "";

}
