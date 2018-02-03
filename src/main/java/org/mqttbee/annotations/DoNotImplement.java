package org.mqttbee.annotations;

import java.lang.annotation.*;

/**
 * Documents that interfaces must not be implemented outside the library.
 *
 * @author Silvio Giebl
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface DoNotImplement {
}
