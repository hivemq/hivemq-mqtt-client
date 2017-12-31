package org.mqttbee.mqtt5.message.subscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Subscribe implements Mqtt5Message {

//    private final int subscribePacketIdentifier; // TODO remove?
//    private final int subscriptionIdentifier; // TODO remove?
//    private final List<MqttSubscription> subscriptions;
//    private final List<Mqtt5UserProperty> userPropertyList;
//
//    public static class MqttSubscription {
//
//        private final String topicFilter;
//        private final Mqtt5QoS qos;
//        private final boolean isNoLocal;
//        private final boolean isRetainAsPublished;
//        private final Mqtt5RetainHandling retainHandling;
//
//    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getSubscribeEncoder().encode(this, channel, out);
    }

}
