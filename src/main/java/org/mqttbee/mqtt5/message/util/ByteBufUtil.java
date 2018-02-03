package org.mqttbee.mqtt5.message.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class ByteBufUtil {

    public static ByteBuf wrapReadOnly(@NotNull final byte[] binary) {
        return Unpooled.wrappedBuffer(binary).asReadOnly();
    }

    public static Optional<ByteBuf> optionalReadOnly(@Nullable final byte[] binary) {
        if (binary == null) {
            return Optional.empty();
        }
        return Optional.of(wrapReadOnly(binary));
    }

}
