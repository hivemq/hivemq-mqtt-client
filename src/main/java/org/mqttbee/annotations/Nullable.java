package org.mqttbee.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Documents that parameters, fields, variables and method return types may be null.
 *
 * @author Silvio Giebl
 */
@Documented
@Retention(CLASS)
@Target({METHOD, FIELD, PARAMETER, LOCAL_VARIABLE})
public @interface Nullable {
}
