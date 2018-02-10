package org.mqttbee.util;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

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
     * @throws NullPointerException          if the object is null.
     * @throws MustNotBeImplementedException if the object is not instance of the implementation type.
     */
    @NotNull
    @SuppressWarnings("unchecked cast")
    public static <S, T extends S> T checkNotImplemented(@NotNull final S object, @NotNull final Class<T> type) {
        Preconditions.checkNotNull(object);
        if (type.isInstance(object)) {
            return (T) object;
        }
        throw new MustNotBeImplementedException(type);
    }

    /**
     * Checks if the given object is null or instance of the given implementation type.
     *
     * @param object the object to check.
     * @param type   the class of the implementation type.
     * @param <S>    the super (interface) type.
     * @param <T>    the implementation type.
     * @return the object casted to the implementation type or null.
     * @throws MustNotBeImplementedException if the object is not null and not instance of the implementation type.
     */
    @Nullable
    public static <S, T extends S> T checkNullOrNotImplemented(@Nullable final S object, @NotNull final Class<T> type) {
        return (object == null) ? null : checkNotImplemented(object, type);
    }

}
