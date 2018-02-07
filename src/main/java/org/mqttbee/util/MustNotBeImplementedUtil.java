package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MustNotBeImplementedUtil {

    /**
     * Checks if the given object is instance of the given implementation type.
     *
     * @param object the object to check.
     * @param type   the class of the implementation type.
     * @param <S>    the super (interface) type.
     * @param <T>    the implementation type.
     * @return the object casted to the implementation type.
     * @throws MustNotBeImplementedException if the object is not instance of the given implementation type.
     */
    @NotNull
    @SuppressWarnings("unchecked cast")
    public static <S, T extends S> T checkNotImplemented(@NotNull final S object, @NotNull final Class<T> type) {
        if (type.isInstance(object)) {
            return (T) object;
        }
        throw new MustNotBeImplementedException(type);
    }

}
