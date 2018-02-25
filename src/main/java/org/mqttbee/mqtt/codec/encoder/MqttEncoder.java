package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Main encoder for MQTT messages which delegates to the individual {@link MqttMessageEncoder}s.
 *
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttEncoder extends MessageToByteEncoder<MqttMessage> {

    public static final String NAME = "encoder.mqtt5";

    @Inject
    MqttEncoder() {
        super(MqttMessage.class, true);
    }

    @Override
    protected ByteBuf allocateBuffer(
            final ChannelHandlerContext ctx, final MqttMessage message, final boolean preferDirect) {

        return message.getEncoder().allocateBuffer(ctx.channel());
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final MqttMessage message, final ByteBuf out) {
        message.getEncoder().encode(out, ctx.channel());
    }

}
