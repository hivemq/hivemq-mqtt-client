package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishEncoder {

    public void encode(@NotNull final Mqtt5PublishImpl publish, @NotNull final ByteBuf out) {

    }

}
