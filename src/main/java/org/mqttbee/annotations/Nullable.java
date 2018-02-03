package org.mqttbee.annotations;

import java.lang.annotation.*;

/**
 * Documents that parameters, fields, variables and method return types may be null.
 *
 * @author Silvio Giebl
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
}
