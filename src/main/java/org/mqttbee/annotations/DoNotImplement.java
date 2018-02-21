package org.mqttbee.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Documents that interfaces must not be implemented outside the library.
 *
 * @author Silvio Giebl
 */
@Documented
@Retention(CLASS)
@Target({TYPE})
public @interface DoNotImplement {
}
