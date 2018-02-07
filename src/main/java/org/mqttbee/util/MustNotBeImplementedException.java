package org.mqttbee.util;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;

/**
 * Exception that is thrown if a object implements an interface annotated with {@link DoNotImplement} is passed to the
 * library.
 *
 * @author Silvio Giebl
 */
public class MustNotBeImplementedException extends RuntimeException {

    MustNotBeImplementedException(@NotNull final Class<?> clazz) {
        super(clazz.getSimpleName() + " must not be implemented outside the library.");
    }

}
