package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderApplier;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.Mqtt5ServerConnectionDataImpl;

/**
 * Encoder for a MQTT message with a {@link MqttMessage} that is applied for encoding.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoderWithMessage<M extends MqttMessage>
        implements MqttMessageEncoder, MqttMessageEncoderApplier<M> {

    protected M message;

    @NotNull
    @Override
    public MqttMessageEncoder apply(@NotNull final M message) {
        this.message = message;
        return this;
    }

    @Override
    public abstract void encode(@NotNull ByteBuf out, @NotNull Channel channel);

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        final int maximumPacketSize = Mqtt5ServerConnectionDataImpl.getMaximumPacketSize(channel);
        final int encodedLength = encodedLength(maximumPacketSize);
        if (encodedLength < 0) {
            throw new MqttMaximumPacketSizeExceededException(message, maximumPacketSize);
        }
        return channel.alloc().ioBuffer(encodedLength, encodedLength);
    }

    @Override
    public abstract int encodedLength(final int maxPacketSize);

}
