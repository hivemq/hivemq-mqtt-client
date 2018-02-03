package org.mqttbee.annotations;

/**
 * Exception that is thrown if a object implements an interface annotated with {@link DoNotImplement} is passed to the
 * library.
 *
 * @author Silvio Giebl
 */
public class MustNotBeImplementedException extends RuntimeException {

    public MustNotBeImplementedException(@NotNull final Class<?> clazz) {
        super(clazz.getSimpleName() + " must not be implemented outside the library.");
    }

}
